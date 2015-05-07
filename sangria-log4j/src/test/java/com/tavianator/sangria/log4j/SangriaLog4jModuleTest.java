/****************************************************************************
 * Sangria                                                                  *
 * Copyright (C) 2014 Tavian Barnes <tavianator@tavianator.com>             *
 *                                                                          *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 * http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************/

package com.tavianator.sangria.log4j;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static com.tavianator.sangria.test.SangriaMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link SangriaLog4jModule}.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
public class SangriaLog4jModuleTest {
    @Inject Logger logger;
    @Inject Provider<Logger> provider;

    @Before
    public void setUp() {
        Guice.createInjector(new SangriaLog4jModule()).injectMembers(this);
    }

    @Test
    public void testLogger() {
        assertThat(logger.getName(), equalTo(SangriaLog4jModuleTest.class.getName()));
    }

    @Test
    public void testProvider() {
        assertThat(provider.get().getName(), equalTo(LogManager.ROOT_LOGGER_NAME));
    }

    static class HasProviderMethod extends AbstractModule {
        @Override
        protected void configure() {
            install(new SangriaLog4jModule());
        }

        @Provides
        String getLoggerName(Logger logger) {
            return logger.getName();
        }
    }

    @Test
    public void testProviderMethod() {
        Injector injector = Guice.createInjector(new HasProviderMethod());

        assertThat(injector.getInstance(String.class), equalTo(HasProviderMethod.class.getName()));
    }

    @Test
    public void testBestPractices() {
        assertThat(new SangriaLog4jModule(), is(atomic()));
        assertThat(new SangriaLog4jModule(), followsBestPractices());
    }
}
