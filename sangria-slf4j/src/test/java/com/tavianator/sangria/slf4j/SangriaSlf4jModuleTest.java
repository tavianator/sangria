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

package com.tavianator.sangria.slf4j;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link SangriaSlf4jModule}.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
public class SangriaSlf4jModuleTest {
    @Inject Logger logger;
    @Inject Provider<Logger> provider;

    @Before
    public void setUp() {
        Guice.createInjector(new SangriaSlf4jModule()).injectMembers(this);
    }

    @Test
    public void testLogger() {
        assertThat(logger.getName(), equalTo("com.tavianator.sangria.slf4j.SangriaSlf4jModuleTest"));
    }

    @Test
    public void testProvider() {
        assertThat(provider.get().getName(), equalTo(Logger.ROOT_LOGGER_NAME));
    }

    @Test
    public void testProviderMethod() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new SangriaSlf4jModule());
            }

            @Provides
            String getLoggerName(Logger logger) {
                return logger.getName();
            }
        });

        assertThat(injector.getInstance(String.class), equalTo(Logger.ROOT_LOGGER_NAME));
    }

    @Test
    public void testDeDuplication() {
        Guice.createInjector(new SangriaSlf4jModule(), new SangriaSlf4jModule());
    }
}
