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

package com.tavianator.sangria.core;

import java.util.*;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link TypeLiterals}.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.1
 * @since 1.1
 */
public class TypeLiteralsTest {
    @Test
    public void testListOf() {
        assertThat(TypeLiterals.listOf(String.class),
                equalTo(new TypeLiteral<List<String>>() { }));
        assertThat(TypeLiterals.listOf(new TypeLiteral<Class<?>>() { }),
                equalTo(new TypeLiteral<List<Class<?>>>() { }));
    }

    @Test
    public void testSetOf() {
        assertThat(TypeLiterals.setOf(String.class),
                equalTo(new TypeLiteral<Set<String>>() { }));
        assertThat(TypeLiterals.setOf(new TypeLiteral<Class<?>>() { }),
                equalTo(new TypeLiteral<Set<Class<?>>>() { }));
    }

    @Test
    public void testMapOf() {
        assertThat(TypeLiterals.mapOf(String.class, String.class),
                equalTo(new TypeLiteral<Map<String, String>>() { }));
        assertThat(TypeLiterals.mapOf(String.class, new TypeLiteral<Class<?>>() { }),
                equalTo(new TypeLiteral<Map<String, Class<?>>>() { }));
        assertThat(TypeLiterals.mapOf(new TypeLiteral<Class<?>>() { }, String.class),
                equalTo(new TypeLiteral<Map<Class<?>, String>>() { }));
        assertThat(TypeLiterals.mapOf(new TypeLiteral<Class<?>>() { }, new TypeLiteral<Class<?>>() { }),
                equalTo(new TypeLiteral<Map<Class<?>, Class<?>>>() { }));
    }

    @Test
    public void testProviderOf() {
        assertThat(TypeLiterals.providerOf(String.class),
                equalTo(new TypeLiteral<Provider<String>>() { }));
        assertThat(TypeLiterals.providerOf(new TypeLiteral<Class<?>>() { }),
                equalTo(new TypeLiteral<Provider<Class<?>>>() { }));
    }

    /**
     * Needed to avoid compilation error to to inferred type being anonymous class.
     */
    private static <T> Matcher<TypeLiteral<T>> equalTo(TypeLiteral<T> type) {
        return Matchers.equalTo(type);
    }
}
