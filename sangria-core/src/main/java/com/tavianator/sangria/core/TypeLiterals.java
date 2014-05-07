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

import javax.inject.Provider;

import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

/**
 * Static utility functions for working with {@link TypeLiteral}s.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.1
 * @since 1.1
 */
public class TypeLiterals {
    private TypeLiterals() {
        // Not for instantiating
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<List<T>> listOf(Class<T> type) {
        return (TypeLiteral<List<T>>)TypeLiteral.get(Types.listOf(type));
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<List<T>> listOf(TypeLiteral<T> type) {
        return (TypeLiteral<List<T>>)TypeLiteral.get(Types.listOf(type.getType()));
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<Set<T>> setOf(Class<T> type) {
        return (TypeLiteral<Set<T>>)TypeLiteral.get(Types.setOf(type));
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<Set<T>> setOf(TypeLiteral<T> type) {
        return (TypeLiteral<Set<T>>)TypeLiteral.get(Types.setOf(type.getType()));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> TypeLiteral<Map<K, V>> mapOf(Class<K> keyType, Class<V> valueType) {
        return (TypeLiteral<Map<K, V>>)TypeLiteral.get(Types.mapOf(keyType, valueType));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> TypeLiteral<Map<K, V>> mapOf(Class<K> keyType, TypeLiteral<V> valueType) {
        return (TypeLiteral<Map<K, V>>)TypeLiteral.get(Types.mapOf(keyType, valueType.getType()));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> TypeLiteral<Map<K, V>> mapOf(TypeLiteral<K> keyType, Class<V> valueType) {
        return (TypeLiteral<Map<K, V>>)TypeLiteral.get(Types.mapOf(keyType.getType(), valueType));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> TypeLiteral<Map<K, V>> mapOf(TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
        return (TypeLiteral<Map<K, V>>)TypeLiteral.get(Types.mapOf(keyType.getType(), valueType.getType()));
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<Provider<T>> providerOf(Class<T> type) {
        // Can't use Types.providerOf() because we want to stick to JSR-330 Providers
        return (TypeLiteral<Provider<T>>)TypeLiteral.get(Types.newParameterizedType(Provider.class, type));
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<Provider<T>> providerOf(TypeLiteral<T> type) {
        // Can't use Types.providerOf() because we want to stick to JSR-330 Providers
        return (TypeLiteral<Provider<T>>)TypeLiteral.get(Types.newParameterizedType(Provider.class, type.getType()));
    }
}
