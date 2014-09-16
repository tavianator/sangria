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

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Scopes;

/**
 * Lazy scope implementations.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.2
 * @since 1.2
 */
public class LazyScopes {
    /**
     * Lazy version of {@link Scopes#SINGLETON}.
     *
     * @see LazySingleton
     */
    public static final Scope LAZY_SINGLETON = new Scope() {
        public <T> Provider<T> scope(final Key<T> key, final Provider<T> creator) {
            final Provider<T> singleton = Scopes.SINGLETON.scope(key, creator);

            return new Provider<T>() {
                public T get() {
                    return singleton.get();
                }

                @Override
                public String toString() {
                    return String.format("%s[%s]", creator, LAZY_SINGLETON);
                }
            };
        }

        @Override
        public String toString() {
            return "LazyScopes.LAZY_SINGLETON";
        }
    };
}
