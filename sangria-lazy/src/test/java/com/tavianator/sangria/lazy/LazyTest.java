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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import org.junit.Test;

import static com.tavianator.sangria.test.SangriaMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link Lazy} injection.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.3
 * @since 1.2
 */
public class LazyTest {
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    private @interface Simple {
    }

    @ProvidedBy(CountingProvider.class)
    private interface Abstract {
    }

    @Singleton
    private static class CountingProvider implements Provider<Abstract> {
        int count = 0;

        @Inject
        CountingProvider() {
        }

        @Override
        public Abstract get() {
            ++count;
            return new Abstract() { };
        }
    }

    private static class HasLazy {
        final Lazy<Abstract> lazy;

        @Inject
        HasLazy(Lazy<Abstract> lazy) {
            this.lazy = lazy;
        }
    }

    private static class HasSimpleLazy extends HasLazy {
        @Inject
        HasSimpleLazy(@Simple Lazy<Abstract> lazy) {
            super(lazy);
        }
    }

    private void test(Injector injector, Class<? extends HasLazy> type) {
        CountingProvider provider = injector.getInstance(CountingProvider.class);
        HasLazy hasLazy = injector.getInstance(type);
        assertThat(provider.count, equalTo(0));

        Abstract a = hasLazy.lazy.get();
        assertThat(provider.count, equalTo(1));

        assertThat(hasLazy.lazy.get(), sameInstance(a));
        assertThat(provider.count, equalTo(1));

        hasLazy = injector.getInstance(type);
        assertThat(provider.count, equalTo(1));

        a = hasLazy.lazy.get();
        assertThat(provider.count, equalTo(2));

        assertThat(hasLazy.lazy.get(), sameInstance(a));
        assertThat(provider.count, equalTo(2));
    }

    @Test
    public void testJustInTime() {
        test(Guice.createInjector(), HasLazy.class);
    }

    private static final Module EXPLICIT_MODULE = new AbstractModule() {
        @Override
        protected void configure() {
            bind(Abstract.class)
                    .toProvider(CountingProvider.class);

            LazyBinder.create(binder())
                    .bind(Abstract.class);

            bind(HasLazy.class);
        }
    };

    @Test
    public void testExplicitBindings() {
        test(Guice.createInjector(EXPLICIT_MODULE), HasLazy.class);
    }

    private static final Module BIND_SEPARATELY_MODULE = new AbstractModule() {
        @Override
        protected void configure() {
            bind(Abstract.class)
                    .annotatedWith(Simple.class)
                    .toProvider(CountingProvider.class);

            LazyBinder.create(binder())
                    .bind(Abstract.class)
                    .annotatedWith(Simple.class);

            bind(HasSimpleLazy.class);
        }
    };

    @Test
    public void testBindSeparately() {
        test(Guice.createInjector(BIND_SEPARATELY_MODULE), HasSimpleLazy.class);
    }

    private static final Module BIND_TOGETHER_MODULE = new AbstractModule() {
        @Override
        protected void configure() {
            LazyBinder.create(binder())
                    .bind(Abstract.class)
                    .annotatedWith(Simple.class)
                    .toProvider(CountingProvider.class);

            bind(HasSimpleLazy.class);
        }
    };

    @Test
    public void testBindTogether() {
        test(Guice.createInjector(BIND_TOGETHER_MODULE), HasSimpleLazy.class);
    }

    @Test
    public void testBestPractices() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                install(EXPLICIT_MODULE);
                install(BIND_SEPARATELY_MODULE);
                install(BIND_TOGETHER_MODULE);
            }
        };

        assertThat(module, is(atomic()));
        assertThat(module, followsBestPractices());
    }

    @Test(expected = CreationException.class)
    public void testMissingBinding() {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                LazyBinder.create(binder())
                        .bind(Abstract.class)
                        .annotatedWith(Simple.class);
            }
        });
    }

    private static class TestVisitor<T> extends DefaultBindingTargetVisitor<T, Boolean> implements LazyBindingVisitor<T, Boolean> {
        @Override
        public Boolean visit(LazyBinding<? extends T> binding) {
            assertThat(binding.getTargetKey().equals(new Key<Abstract>(Simple.class) { }), is(true));
            return true;
        }

        @Override
        protected Boolean visitOther(Binding<? extends T> binding) {
            return false;
        }
    }

    private <T> boolean visit(Binding<T> binding) {
        return binding.acceptTargetVisitor(new TestVisitor<T>());
    }

    @Test
    public void testExtensionSpi() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                // TODO: Expose the SPI for unqualified bindings
                LazyBinder.create(binder())
                        .bind(Abstract.class)
                        .annotatedWith(Simple.class)
                        .toProvider(CountingProvider.class);
            }
        };

        List<Element> elements = Elements.getElements(module);

        int passed = 0;
        for (Element element : elements) {
            if (element instanceof Binding) {
                if (visit((Binding<?>)element)) {
                    ++passed;
                }
            }
        }
        assertThat(passed, equalTo(1));

        Injector injector = Guice.createInjector(Elements.getModule(elements));
        assertThat(visit(injector.getBinding(new Key<Lazy<Abstract>>(Simple.class) { })), is(true));
        assertThat(visit(injector.getBinding(new Key<Abstract>(Simple.class) { })), is(false));
    }
}
