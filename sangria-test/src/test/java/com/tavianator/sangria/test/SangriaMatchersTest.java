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

package com.tavianator.sangria.test;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.junit.Test;

import static com.tavianator.sangria.test.SangriaMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link SangriaMatchers}.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.2
 * @since 1.2
 */
public class SangriaMatchersTest {
    private static class AtomicModule extends AbstractModule {
        private static final Object INSTANCE = new Object();

        @Override
        protected void configure() {
            bind(Object.class)
                    .toInstance(INSTANCE);
        }
    }

    private static class NonAtomicModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Object.class)
                    .toInstance(new Object());
        }
    }

    @Test
    public void testAtomic() {
        assertThat(new AtomicModule(), is(atomic()));
        assertThat(new NonAtomicModule(), is(not(atomic())));
    }

    private static class NoAtInjectModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class);
        }
    }

    private static class InexactBindingAnnotationModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class)
                    .annotatedWith(Named.class)
                    .toInstance("test");
        }

        @Provides
        String getString(@Named("test") String test) {
            return test;
        }
    }

    private static class Injectable {
        @Inject
        Injectable() {
        }
    }

    private static class JustInTimeModule extends AbstractModule {
        @Override
        protected void configure() {
        }

        @Provides
        String getString(Injectable injectable) {
            return "test";
        }
    }

    @Test
    public void testFollowsBestPractices() {
        assertThat(new AtomicModule(), followsBestPractices());
        assertThat(new NoAtInjectModule(), not(followsBestPractices()));
        assertThat(new InexactBindingAnnotationModule(), not(followsBestPractices()));
        assertThat(new JustInTimeModule(), not(followsBestPractices()));
    }
}
