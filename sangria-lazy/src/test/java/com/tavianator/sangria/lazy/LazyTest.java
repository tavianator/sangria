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

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
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
 * @version 1.2
 * @since 1.2
 */
public class LazyTest {
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    private @interface Simple {
    }

    private interface Abstract {
    }

    private static class Concrete implements Abstract {
        static final ThreadLocal<Integer> INSTANCES = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };

        @Inject
        Concrete() {
            INSTANCES.set(INSTANCES.get() + 1);
        }
    }

    private static class HasConcrete {
        final Lazy<Concrete> lazy;

        @Inject
        HasConcrete(Lazy<Concrete> lazy) {
            this.lazy = lazy;
        }
    }

    private static class HasQualifiedAbstract {
        @Inject @Simple Lazy<Abstract> lazy;
    }

    @Test
    public void testJustInTime() {
        testHasConcrete(Guice.createInjector());
    }

    @Test
    public void testExplicitBindings() {
        testHasConcrete(Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                binder().requireExplicitBindings();

                bind(HasConcrete.class);

                bind(Concrete.class);
                LazyBinder.create(binder())
                        .bind(Concrete.class);
            }
        }));
    }

    @Test
    public void testBestPractices() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(HasConcrete.class);

                bind(Concrete.class);
                LazyBinder.create(binder())
                        .bind(Concrete.class);
            }
        };
        assertThat(module, is(atomic()));
        assertThat(module, followsBestPractices());
    }

    private void testHasConcrete(Injector injector) {
        int before = Concrete.INSTANCES.get();

        HasConcrete hasConcrete = injector.getInstance(HasConcrete.class);
        assertThat(Concrete.INSTANCES.get(), equalTo(before));

        Concrete instance = hasConcrete.lazy.get();
        assertThat(Concrete.INSTANCES.get(), equalTo(before + 1));

        Concrete instance2 = hasConcrete.lazy.get();
        assertThat(instance2, sameInstance(instance));
        assertThat(Concrete.INSTANCES.get(), equalTo(before + 1));

        HasConcrete hasConcrete2 = injector.getInstance(HasConcrete.class);
        assertThat(Concrete.INSTANCES.get(), equalTo(before + 1));

        Concrete instance3 = hasConcrete2.lazy.get();
        assertThat(instance3, not(sameInstance(instance)));
        assertThat(Concrete.INSTANCES.get(), equalTo(before + 2));
    }

    @Test
    public void testBindSeparately() {
        testQualifiedAbstract(Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Abstract.class)
                        .annotatedWith(Simple.class)
                        .to(Concrete.class);

                LazyBinder.create(binder())
                        .bind(Abstract.class)
                        .annotatedWith(Simple.class);
            }
        }));
    }

    @Test
    public void testBindTogether() {
        testQualifiedAbstract(Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                LazyBinder.create(binder())
                        .bind(Abstract.class)
                        .annotatedWith(Simple.class)
                        .to(Concrete.class);
            }
        }));
    }

    private void testQualifiedAbstract(Injector injector) {
        int before = Concrete.INSTANCES.get();

        HasQualifiedAbstract hasQualifiedAbstract = injector.getInstance(HasQualifiedAbstract.class);
        assertThat(Concrete.INSTANCES.get(), equalTo(before));

        Abstract instance = hasQualifiedAbstract.lazy.get();
        assertThat(Concrete.INSTANCES.get(), equalTo(before + 1));

        Abstract instance2 = hasQualifiedAbstract.lazy.get();
        assertThat(instance2, sameInstance(instance2));
        assertThat(Concrete.INSTANCES.get(), equalTo(before + 1));

        HasQualifiedAbstract hasQualifiedAbstract2 = injector.getInstance(HasQualifiedAbstract.class);
        assertThat(Concrete.INSTANCES.get(), equalTo(before + 1));

        Abstract instance3 = hasQualifiedAbstract2.lazy.get();
        assertThat(instance3, not(sameInstance(instance)));
        assertThat(Concrete.INSTANCES.get(), equalTo(before + 2));
    }

    @Test(expected = CreationException.class)
    public void testMissingBinding() {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                LazyBinder.create(binder())
                        .bind(Abstract.class);
            }
        });
    }

    @Test(expected = CreationException.class)
    public void testMissingQualifiedBinding() {
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
                        .to(Concrete.class);
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
        assertThat(visit(injector.getBinding(new Key<Concrete>() { })), is(false));
    }
}
