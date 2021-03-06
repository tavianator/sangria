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

package com.tavianator.sangria.contextual;

import java.util.*;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.InjectionPoint;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.tavianator.sangria.test.SangriaMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link ContextSensitiveBinder}.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
public class ContextSensitiveBinderTest {
    public @Rule ExpectedException thrown = ExpectedException.none();

    private static class SelfProvider implements ContextSensitiveProvider<String> {
        @Inject
        SelfProvider() {
        }

        // For testing getInjectionPoints() in the SPI below
        @SuppressWarnings("unused")
        @Inject Injector injector;

        @Override
        public String getInContext(InjectionPoint injectionPoint) {
            return injectionPoint.getDeclaringType().getRawType().getSimpleName();
        }

        @Override
        public String getInUnknownContext() {
            return "<unknown>";
        }
    }

    private static class HasSelf {
        @Inject @Named("self") String self;
        @Inject @Named("self") Provider<String> selfProvider;
    }

    @Test
    public void testProviderClass() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ContextSensitiveBinder.create(binder())
                        .bind(String.class)
                        .annotatedWith(Names.named("self"))
                        .toContextSensitiveProvider(SelfProvider.class);
            }
        });

        HasSelf hasSelf = injector.getInstance(HasSelf.class);
        assertThat(hasSelf.self, equalTo("HasSelf"));
        assertThat(hasSelf.selfProvider.get(), equalTo("<unknown>"));
    }

    @Test
    public void testProviderTypeLiteral() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ContextSensitiveBinder.create(binder())
                        .bind(String.class)
                        .annotatedWith(Names.named("self"))
                        .toContextSensitiveProvider(new TypeLiteral<SelfProvider>() { });
            }
        });

        HasSelf hasSelf = injector.getInstance(HasSelf.class);
        assertThat(hasSelf.self, equalTo("HasSelf"));
        assertThat(hasSelf.selfProvider.get(), equalTo("<unknown>"));
    }

    @Test
    public void testProviderKey() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(SelfProvider.class)
                        .annotatedWith(Names.named("self"))
                        .to(SelfProvider.class);

                ContextSensitiveBinder.create(binder())
                        .bind(String.class)
                        .annotatedWith(Names.named("self"))
                        .toContextSensitiveProvider(new Key<SelfProvider>(Names.named("self")) { });
            }
        });

        HasSelf hasSelf = injector.getInstance(HasSelf.class);
        assertThat(hasSelf.self, equalTo("HasSelf"));
        assertThat(hasSelf.selfProvider.get(), equalTo("<unknown>"));
    }

    @Test
    public void testProviderInstance() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ContextSensitiveBinder.create(binder())
                        .bind(String.class)
                        .annotatedWith(Names.named("self"))
                        .toContextSensitiveProvider(new SelfProvider());
            }
        });

        HasSelf hasSelf = injector.getInstance(HasSelf.class);
        assertThat(hasSelf.self, equalTo("HasSelf"));
        assertThat(hasSelf.selfProvider.get(), equalTo("<unknown>"));
    }

    @Test
    public void testKeyDeDuplication() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ContextSensitiveBinder contextualBinder = ContextSensitiveBinder.create(binder());
                contextualBinder
                        .bind(String.class)
                        .annotatedWith(Names.named("self"))
                        .toContextSensitiveProvider(SelfProvider.class);
                contextualBinder
                        .bind(String.class)
                        .annotatedWith(Names.named("self"))
                        .toContextSensitiveProvider(SelfProvider.class);
            }
        });

        HasSelf hasSelf = injector.getInstance(HasSelf.class);
        assertThat(hasSelf.self, equalTo("HasSelf"));
        assertThat(hasSelf.selfProvider.get(), equalTo("<unknown>"));
    }

    @Test
    public void testInstanceDeDuplication() {
        final SelfProvider selfProvider = new SelfProvider();
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ContextSensitiveBinder contextualBinder = ContextSensitiveBinder.create(binder());
                contextualBinder
                        .bind(String.class)
                        .annotatedWith(Names.named("self"))
                        .toContextSensitiveProvider(selfProvider);
                contextualBinder
                        .bind(String.class)
                        .annotatedWith(Names.named("self"))
                        .toContextSensitiveProvider(selfProvider);
            }
        });

        HasSelf hasSelf = injector.getInstance(HasSelf.class);
        assertThat(hasSelf.self, equalTo("HasSelf"));
        assertThat(hasSelf.selfProvider.get(), equalTo("<unknown>"));
    }

    @Test
    public void testBestPractices() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                ContextSensitiveBinder.create(binder())
                        .bind(String.class)
                        .annotatedWith(Names.named("self"))
                        .toContextSensitiveProvider(SelfProvider.class);
            }
        };
        assertThat(module, is(atomic()));
        assertThat(module, followsBestPractices());
    }

    private static class RequiredContextProvider implements ContextSensitiveProvider<String> {
        @Override
        public String getInContext(InjectionPoint injectionPoint) {
            return injectionPoint.getDeclaringType().getRawType().getSimpleName();
        }

        @Override
        public String getInUnknownContext() {
            throw new IllegalStateException("@Named(\"self\") injection not supported here");
        }
    }

    @Test
    public void testContextRequired() {
        thrown.expect(ProvisionException.class);

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ContextSensitiveBinder.create(binder())
                        .bind(String.class)
                        .annotatedWith(Names.named("self"))
                        .toContextSensitiveProvider(RequiredContextProvider.class);
            }
        });

        HasSelf hasSelf = injector.getInstance(HasSelf.class);
        assertThat(hasSelf.self, equalTo("HasSelf"));
        hasSelf.selfProvider.get();
    }

    private static class Recursive {
        @Inject HasSelf hasSelf;
        String self;
    }

    private static class RecursiveProvider implements ContextSensitiveProvider<Recursive> {
        @Inject MembersInjector<Recursive> membersInjector;

        @Override
        public Recursive getInContext(InjectionPoint injectionPoint) {
            Recursive result = new Recursive();
            membersInjector.injectMembers(result);
            result.self = injectionPoint.getDeclaringType().getRawType().getSimpleName();
            return result;
        }

        @Override
        public Recursive getInUnknownContext() {
            Recursive result = new Recursive();
            membersInjector.injectMembers(result);
            result.self = "<unknown>";
            return result;
        }
    }

    private static class HasRecursive {
        @Inject Recursive recursive;
    }

    @Test
    public void testRecursiveProvision() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ContextSensitiveBinder contextualBinder = ContextSensitiveBinder.create(binder());
                contextualBinder
                        .bind(String.class)
                        .annotatedWith(Names.named("self"))
                        .toContextSensitiveProvider(SelfProvider.class);
                contextualBinder
                        .bind(Recursive.class)
                        .toContextSensitiveProvider(new RecursiveProvider());
            }
        });

        HasRecursive hasRecursive = injector.getInstance(HasRecursive.class);
        assertThat(hasRecursive.recursive.self, equalTo("HasRecursive"));
        assertThat(hasRecursive.recursive.hasSelf.self, equalTo("HasSelf"));

        Recursive recursive = injector.getInstance(Recursive.class);
        assertThat(recursive.self, equalTo("<unknown>"));
        assertThat(recursive.hasSelf.self, equalTo("HasSelf"));
    }

    @Test
    public void testIncompleteEdsl1() {
        thrown.expect(CreationException.class);
        thrown.expectMessage("Missing call to toContextSensitiveProvider() for java.lang.String");

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ContextSensitiveBinder.create(binder())
                        .bind(String.class);
            }
        });
    }

    @Test
    public void testIncompleteEdsl2() {
        thrown.expect(CreationException.class);
        thrown.expectMessage("Missing call to toContextSensitiveProvider() for java.lang.String annotated with "
                + "@com.google.inject.name.Named(value=self)");

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ContextSensitiveBinder.create(binder())
                        .bind(String.class)
                        .annotatedWith(Names.named("self"));
            }
        });
    }

    private static class TestVisitor<T> extends DefaultBindingTargetVisitor<T, Boolean> implements ContextSensitiveBindingVisitor<T, Boolean> {
        @Override
        public Boolean visit(ContextSensitiveProviderKeyBinding<? extends T> binding) {
            assertThat(binding.getContextSensitiveProviderKey().equals(new Key<SelfProvider>() { }), is(true));
            return true;
        }

        @Override
        public Boolean visit(ContextSensitiveProviderInstanceBinding<? extends T> binding) {
            assertThat(binding.getContextSensitiveProviderInstance(), instanceOf(SelfProvider.class));
            assertThat(binding.getInjectionPoints(), hasSize(1));
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
        List<Element> elements = Elements.getElements(new AbstractModule() {
            @Override
            protected void configure() {
                ContextSensitiveBinder contextualBinder = ContextSensitiveBinder.create(binder());
                contextualBinder
                        .bind(String.class)
                        .annotatedWith(Names.named("key"))
                        .toContextSensitiveProvider(SelfProvider.class);
                contextualBinder
                        .bind(String.class)
                        .annotatedWith(Names.named("instance"))
                        .toContextSensitiveProvider(new SelfProvider());
            }
        });

        int passed = 0;
        for (Element element : elements) {
            if (element instanceof Binding) {
                if (visit(((Binding<?>)element))) {
                    ++passed;
                }
            }
        }
        assertThat(passed, equalTo(2));

        Injector injector = Guice.createInjector(Elements.getModule(elements));
        assertThat(visit(injector.getBinding(new Key<String>(Names.named("key")) { })), is(true));
        assertThat(visit(injector.getBinding(new Key<String>(Names.named("instance")) { })), is(true));
        assertThat(visit(injector.getBinding(SelfProvider.class)), is(false));
    }
}
