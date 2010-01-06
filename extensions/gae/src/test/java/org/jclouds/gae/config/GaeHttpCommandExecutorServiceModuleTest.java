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
package org.jclouds.gae.config;

import static com.google.common.util.concurrent.Executors.sameThreadExecutor;

import java.util.Properties;

import org.jclouds.concurrent.config.ExecutorServiceModule;
import org.jclouds.gae.GaeHttpCommandExecutorService;
import org.jclouds.http.HttpCommandExecutorService;
import org.jclouds.logging.Logger;
import org.jclouds.logging.Logger.LoggerFactory;
import org.jclouds.util.Jsr330;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Tests the ability to configure a {@link GaeHttpCommandExecutorService}
 * 
 * @author Adrian Cole
 */
@Test
public class GaeHttpCommandExecutorServiceModuleTest {

   public void testConfigureBindsClient() {
      final Properties properties = new Properties();

      Injector i = Guice.createInjector(new ExecutorServiceModule(sameThreadExecutor()),
               new GaeHttpCommandExecutorServiceModule() {
                  @Override
                  protected void configure() {
                     Jsr330.bindProperties(binder(), properties);
                     bind(Logger.LoggerFactory.class).toInstance(new LoggerFactory() {
                        public Logger getLogger(String category) {
                           return Logger.NULL;
                        }
                     });
                     super.configure();
                  }
               });
      HttpCommandExecutorService client = i.getInstance(HttpCommandExecutorService.class);
      assert client instanceof GaeHttpCommandExecutorService;
   }
}
