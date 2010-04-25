/**
 *
 * Copyright (C) 2009 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package org.jclouds.nirvanix.sdn.filters;

import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;
import java.net.URI;

import javax.ws.rs.POST;

import org.jclouds.concurrent.config.ExecutorServiceModule;
import org.jclouds.date.DateService;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.config.JavaUrlHttpCommandExecutorServiceModule;
import org.jclouds.logging.Logger;
import org.jclouds.logging.Logger.LoggerFactory;
import org.jclouds.nirvanix.sdn.SDNPropertiesBuilder;
import org.jclouds.nirvanix.sdn.SessionToken;
import org.jclouds.rest.annotations.EndpointParam;
import org.jclouds.rest.config.RestModule;
import org.jclouds.rest.internal.RestAnnotationProcessor;
import org.jclouds.util.Jsr330;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

@Test(groups = "unit", testName = "sdn.AddSessionTokenToRequestTest")
public class AddSessionTokenToRequestTest {

   private Injector injector;
   private AddSessionTokenToRequest filter;

   private static interface TestService {
      @POST
      public void foo(@EndpointParam URI endpoint);
   }

   @DataProvider
   public Object[][] dataProvider() throws SecurityException, NoSuchMethodException {

      RestAnnotationProcessor<TestService> factory = injector.getInstance(Key
               .get(new TypeLiteral<RestAnnotationProcessor<TestService>>() {
               }));

      Method method = TestService.class.getMethod("foo", URI.class);
      return new Object[][] {
               { factory.createRequest(method, new Object[] { URI.create("https://host:443") }) },
               { factory.createRequest(method, new Object[] { URI.create("https://host/path") }) },
               { factory.createRequest(method, new Object[] { URI.create("https://host/?query") })

               } };
   }

   @Test(dataProvider = "dataProvider")
   public void testRequests(HttpRequest request) {
      String token = filter.getSessionToken();

      String query = request.getEndpoint().getQuery();
      filter.filter(request);
      assertEquals(request.getEndpoint().getQuery(), query == null ? "sessionToken=" + token
               : query + "&sessionToken=" + token);
   }

   @Test
   void testUpdatesOnlyOncePerSecond() throws NoSuchMethodException, InterruptedException {
      String token = filter.getSessionToken();
      for (int i = 0; i < 10; i++)
         filter.updateIfTimeOut();
      assert token.equals(filter.getSessionToken());
   }

   /**
    * before class, as we need to ensure that the filter is threadsafe.
    * 
    */
   @BeforeClass
   protected void createFilter() {
      injector = Guice.createInjector(new RestModule(), new ExecutorServiceModule(
               sameThreadExecutor(), sameThreadExecutor()),
               new JavaUrlHttpCommandExecutorServiceModule(), new AbstractModule() {

                  protected void configure() {
                     bind(DateService.class);
                     Jsr330.bindProperties(this.binder(), new SDNPropertiesBuilder("appkey",
                              "appname", "username", "password").build());
                     bind(Logger.LoggerFactory.class).toInstance(new LoggerFactory() {
                        public Logger getLogger(String category) {
                           return Logger.NULL;
                        }
                     });
                  }

                  @SuppressWarnings("unused")
                  @SessionToken
                  @Provides
                  String authTokenProvider() {
                     return System.currentTimeMillis() + "";
                  }
               });
      filter = injector.getInstance(AddSessionTokenToRequest.class);
   }

}