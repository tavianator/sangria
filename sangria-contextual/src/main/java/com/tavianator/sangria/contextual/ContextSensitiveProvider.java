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

import com.google.inject.Provider;
import com.google.inject.spi.InjectionPoint;

/**
 * Like a {@link Provider}, but with knowledge of the target {@link InjectionPoint}.
 *
 * <p>
 * This interface, along with {@link ContextSensitiveBinder}, is useful for injecting custom logger types, among other
 * things. However, context-sensitive injections can make maintenance and debugging more difficult.
 * </p>
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
public interface ContextSensitiveProvider<T> {
    /**
     * Provide an instance of {@code T} for the given context.
     *
     * @param injectionPoint The {@link InjectionPoint} for this provision.
     * @return An instance of {@code T}.
     */
    T getInContext(InjectionPoint injectionPoint);

    /**
     * Provide an instance of {@code T} for an unknown context.
     * <p>
     * The {@link InjectionPoint} may not be known in all cases, for example if a {@code Provider<T>} is used instead
     * of
     * a bare {@code T}. This method will be called in those cases.
     * </p>
     * <p>
     * One reasonable implementation is to return a generically applicable instance, such as an anonymous logger.
     * Another valid implementation is to throw an unchecked exception; in that case, {@code Provider<T>} injections
     * will fail.
     * </p>
     *
     * @return An instance of {@code T}
     * @throws RuntimeException If injection without a context is not supported.
     */
    T getInUnknownContext();
}
