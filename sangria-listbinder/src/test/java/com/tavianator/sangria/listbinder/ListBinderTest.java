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

package com.tavianator.sangria.listbinder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;
import javax.inject.Provider;
import javax.inject.Qualifier;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.tavianator.sangria.core.TypeLiterals;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link ListBinder}.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.1
 * @since 1.1
 */
public class ListBinderTest {
    public @Rule ExpectedException thrown = ExpectedException.none();

    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    private @interface Simple {
    }

    private static final TypeLiteral<List<String>> LIST_OF_STRINGS = TypeLiterals.listOf(String.class);
    private static final TypeLiteral<List<Provider<String>>> LIST_OF_STRING_PROVIDERS = TypeLiterals.listOf(TypeLiterals.providerOf(String.class));

    @Test
    public void testBasicLists() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ListBinder<String> listBinder = ListBinder.build(binder(), String.class)
                        .withDefaultPriority();
                listBinder.addBinding().toInstance("a");
                listBinder.addBinding().toInstance("b");
                listBinder.addBinding().toInstance("c");

                listBinder = ListBinder.build(binder(), String.class)
                        .annotatedWith(Simple.class)
                        .withDefaultPriority();
                listBinder.addBinding().toInstance("d");
                listBinder.addBinding().toInstance("e");
                listBinder.addBinding().toInstance("f");

                listBinder = ListBinder.build(binder(), String.class)
                        .annotatedWith(Names.named("name"))
                        .withDefaultPriority();
                listBinder.addBinding().toInstance("g");
                listBinder.addBinding().toInstance("h");
                listBinder.addBinding().toInstance("i");
            }
        });
        List<String> list = injector.getInstance(Key.get(LIST_OF_STRINGS));
        assertThat(list, contains("a", "b", "c"));

        List<Provider<String>> providers = injector.getInstance(Key.get(LIST_OF_STRING_PROVIDERS));
        assertThat(providers, hasSize(3));
        assertThat(providers.get(0).get(), equalTo("a"));
        assertThat(providers.get(1).get(), equalTo("b"));
        assertThat(providers.get(2).get(), equalTo("c"));

        list = injector.getInstance(Key.get(LIST_OF_STRINGS, Simple.class));
        assertThat(list, contains("d", "e", "f"));

        list = injector.getInstance(Key.get(LIST_OF_STRINGS, Names.named("name")));
        assertThat(list, contains("g", "h", "i"));
    }

    @Test
    public void testSplitBinders() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ListBinder<String> listBinder = ListBinder.build(binder(), String.class)
                        .withPriority(1);
                listBinder.addBinding().toInstance("c");
                listBinder.addBinding().toInstance("d");
            }
        }, new AbstractModule() {
            @Override
            protected void configure() {
                ListBinder<String> listBinder = ListBinder.build(binder(), String.class)
                        .withPriority(0);
                listBinder.addBinding().toInstance("a");
                listBinder.addBinding().toInstance("b");
            }
        });
        List<String> list = injector.getInstance(Key.get(LIST_OF_STRINGS));
        assertThat(list, contains("a", "b", "c", "d"));
    }

    @Test
    public void testConflictingDefaultPriorities() {
        thrown.expect(CreationException.class);
        thrown.expectMessage(containsString("2 errors"));
        thrown.expectMessage(containsString("1) Duplicate ListBinder<java.lang.String> with default priority"));
        thrown.expectMessage(containsString("2) Duplicate ListBinder<java.lang.String> with default priority"));
        thrown.expectMessage(containsString("at com.tavianator.sangria.listbinder.ListBinderTest"));

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ListBinder.build(binder(), String.class)
                        .withDefaultPriority();
            }
        }, new AbstractModule() {
            @Override
            protected void configure() {
                ListBinder.build(binder(), String.class)
                        .withDefaultPriority();
            }
        });
    }

    @Test
    public void testConflictingExplicitPriorities() {
        thrown.expect(CreationException.class);
        thrown.expectMessage(containsString("2 errors"));
        thrown.expectMessage(containsString("1) Duplicate ListBinder<java.lang.String> with priority [1]"));
        thrown.expectMessage(containsString("2) Duplicate ListBinder<java.lang.String> with priority [1]"));
        thrown.expectMessage(containsString("at com.tavianator.sangria.listbinder.ListBinderTest"));

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ListBinder.build(binder(), String.class)
                        .withPriority(1);
            }
        }, new AbstractModule() {
            @Override
            protected void configure() {
                ListBinder.build(binder(), String.class)
                        .withPriority(1);
            }
        });
    }

    @Test
    public void testConflictingDefaultAndExplicitPriorities() {
        thrown.expect(CreationException.class);
        thrown.expectMessage(containsString("2 errors"));
        thrown.expectMessage(containsString(") ListBinder<java.lang.String> with default priority conflicts with ListBinder with explicit priority"));
        thrown.expectMessage(containsString(") ListBinder<java.lang.String> with priority [1] conflicts with ListBinder with default priority"));
        thrown.expectMessage(containsString("at com.tavianator.sangria.listbinder.ListBinderTest"));

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ListBinder.build(binder(), String.class)
                        .withDefaultPriority();
            }
        }, new AbstractModule() {
            @Override
            protected void configure() {
                ListBinder.build(binder(), String.class)
                        .withPriority(1);
            }
        });
    }

    @Test
    public void testToString() {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ListBinder<String> listBinder = ListBinder.build(binder(), String.class)
                        .annotatedWith(Names.named("name"))
                        .withDefaultPriority();
                assertThat(listBinder.toString(), equalTo("ListBinder<java.lang.String> annotated with @com.google.inject.name.Named(value=name) with default priority"));

                ListBinder<Object> objectListBinder = ListBinder.build(binder(), Object.class)
                        .withPriority(1, 2);
                assertThat(objectListBinder.toString(), equalTo("ListBinder<java.lang.Object> with priority [1, 2]"));
            }
        });
    }
}
