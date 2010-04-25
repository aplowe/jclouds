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
#set( $lcaseClientName = ${clientName.toLowerCase()} )
#set( $ucaseClientName = ${clientName.toUpperCase()} )
#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/**
 *
 * Copyright (C) 2009 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 */
package ${package}.config;

import static org.testng.Assert.assertEquals;
import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;

import org.jclouds.concurrent.config.ExecutorServiceModule;
import org.jclouds.http.HttpRetryHandler;
import org.jclouds.http.config.JavaUrlHttpCommandExecutorServiceModule;
import org.jclouds.http.functions.config.ParserModule;
import org.jclouds.http.functions.config.ParserModule.CDateAdapter;
import org.jclouds.http.functions.config.ParserModule.DateAdapter;
import org.jclouds.http.handlers.CloseContentAndSetExceptionErrorHandler;
import org.jclouds.http.handlers.DelegatingErrorHandler;
import org.jclouds.http.handlers.DelegatingRetryHandler;
import org.jclouds.http.handlers.RedirectionRetryHandler;
import org.jclouds.logging.Logger;
import org.jclouds.logging.Logger.LoggerFactory;
import ${package}.reference.${clientName}Constants;
import org.jclouds.util.Jsr330;
import org.jclouds.Constants;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author ${author}
 */
@Test(groups = "unit", testName = "${lcaseClientName}.${clientName}ContextModule")
public class ${clientName}ContextModuleTest {

   Injector createInjector() {
      return Guice.createInjector(new ${clientName}RestClientModule(), new ${clientName}ContextModule() {
         @Override
         protected void configure() {
            bindConstant().annotatedWith(Jsr330.named(${clientName}Constants.PROPERTY_${ucaseClientName}_USER)).to(
                     "user");
            bindConstant().annotatedWith(Jsr330.named(${clientName}Constants.PROPERTY_${ucaseClientName}_PASSWORD))
                     .to("password");
            bindConstant().annotatedWith(Jsr330.named(${clientName}Constants.PROPERTY_${ucaseClientName}_ENDPOINT))
                     .to("http://localhost");
            bindConstant().annotatedWith(Jsr330.named(Constants.PROPERTY_MAX_CONNECTIONS_PER_HOST))
				     .to("1");
			bindConstant().annotatedWith(Jsr330.named(Constants.PROPERTY_MAX_CONNECTIONS_PER_CONTEXT))
				     .to("0");
			bindConstant().annotatedWith(Jsr330.named(Constants.PROPERTY_IO_WORKER_THREADS))
				     .to("1");
			bindConstant().annotatedWith(Jsr330.named(Constants.PROPERTY_USER_THREADS))
			      	 .to("1");
            bind(Logger.LoggerFactory.class).toInstance(new LoggerFactory() {
               public Logger getLogger(String category) {
                  return Logger.NULL;
               }
            });
            super.configure();
         }
      }, new ParserModule(), new JavaUrlHttpCommandExecutorServiceModule(),
          new ExecutorServiceModule(sameThreadExecutor(), sameThreadExecutor()));
   }

   @Test
   void testServerErrorHandler() {
      DelegatingErrorHandler handler = createInjector().getInstance(DelegatingErrorHandler.class);
      assertEquals(handler.getServerErrorHandler().getClass(),
               CloseContentAndSetExceptionErrorHandler.class);
   }

   @Test
   void testDateTimeAdapter() {
      assertEquals(this.createInjector().getInstance(DateAdapter.class).getClass(),
               CDateAdapter.class);
   }

   @Test
   void testClientErrorHandler() {
      DelegatingErrorHandler handler = createInjector().getInstance(DelegatingErrorHandler.class);
      assertEquals(handler.getClientErrorHandler().getClass(),
               CloseContentAndSetExceptionErrorHandler.class);
   }

   @Test
   void testClientRetryHandler() {
      DelegatingRetryHandler handler = createInjector().getInstance(DelegatingRetryHandler.class);
      assertEquals(handler.getClientErrorRetryHandler(), HttpRetryHandler.NEVER_RETRY);
   }

   @Test
   void testRedirectionRetryHandler() {
      DelegatingRetryHandler handler = createInjector().getInstance(DelegatingRetryHandler.class);
      assertEquals(handler.getRedirectionRetryHandler().getClass(), RedirectionRetryHandler.class);
   }

}