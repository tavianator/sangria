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

/**
 * A lazily-loaded dependency. Like a {@link Provider}, calling {@link #get()} will produce an instance of {@code T}.
 * Unlike a {@link Provider}, the same instance will be returned for every future call to {@link #get()}. Different
 * {@code Lazy} instances are independent and will return different instances from {@link #get()}.
 *
 * <p>
 * {@link Lazy} works automatically for unqualified bindings, as long as just-in-time bindings are enabled. For
 * qualified bindings, or if explicit bindings are requred, use {@link LazyBinder}:
 * </p>
 *
 * <pre>
 * // Either separately...
 * bind(Dependency.class)
 *         .annotatedWith(Names.named("name"))
 *         .to(RealDependency.class);
 *
 * LazyBinder.create(binder())
 *         .bind(Dependency.class)
 *         .annotatedWith(Names.named("name"));
 *
 * // ... or in one go
 * LazyBinder.create(binder())
 *         .bind(Dependency.class)
 *         .annotatedWith(Names.named("name"))
 *         .to(RealDependency.class);
 *
 * ...
 *
 * {@literal @}Inject {@literal @}Named("name") Lazy&lt;Dependency&gt; lazy;
 * </pre>
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.2
 * @since 1.2
 */
public final class Lazy<T> {
    private static final Object SENTINEL = new Object();

    private final Provider<T> provider;
    private volatile Object instance = SENTINEL;

    @Inject
    Lazy(Provider<T> provider) {
        this.provider = provider;
    }

    /**
     * @return A lazily-produced value of type {@code T}.
     */
    @SuppressWarnings("unchecked")
    public T get() {
        // Double-checked locking
        if (instance == SENTINEL) {
            synchronized (this) {
                if (instance == SENTINEL) {
                    instance = provider.get();
                }
            }
        }
        return (T) instance;
    }
}
