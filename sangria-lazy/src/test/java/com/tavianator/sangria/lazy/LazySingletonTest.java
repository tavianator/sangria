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

package com.tavianator.sangria.lazy;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.junit.Test;

import static com.tavianator.sangria.test.SangriaMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for the {@link LazySingleton} scope.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.2
 * @since 1.2
 */
public class LazySingletonTest {
    @LazySingleton
    private static class Scoped {
        static final ThreadLocal<Integer> INSTANCES = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };

        @Inject
        Scoped() {
            INSTANCES.set(INSTANCES.get() + 1);
        }
    }

    @Test
    public void testDevelopment() {
        test(Stage.DEVELOPMENT, new SangriaLazyModule());
    }

    @Test
    public void testProduction() {
        test(Stage.PRODUCTION, new SangriaLazyModule());
    }

    private void test(Stage stage, Module... modules) {
        int before = Scoped.INSTANCES.get();

        Injector injector = Guice.createInjector(stage, modules);
        Provider<Scoped> provider = injector.getProvider(Scoped.class);
        assertThat(Scoped.INSTANCES.get(), equalTo(before));

        Scoped instance = provider.get();
        assertThat(Scoped.INSTANCES.get(), equalTo(before + 1));

        assertThat(provider.get(), sameInstance(instance));
        assertThat(Scoped.INSTANCES.get(), equalTo(before + 1));
    }

    @Test
    public void testBestPractices() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                install(new SangriaLazyModule());
                bind(Scoped.class);
            }
        };

        assertThat(module, is(atomic()));
        assertThat(module, followsBestPractices());
    }
}
