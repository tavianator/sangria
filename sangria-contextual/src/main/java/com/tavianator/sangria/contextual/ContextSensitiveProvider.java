/*********************************************************************
 * Sangria                                                           *
 * Copyright (C) 2014 Tavian Barnes <tavianator@tavianator.com>      *
 *                                                                   *
 * This library is free software. It comes without any warranty, to  *
 * the extent permitted by applicable law. You can redistribute it   *
 * and/or modify it under the terms of the Do What The Fuck You Want *
 * To Public License, Version 2, as published by Sam Hocevar. See    *
 * the COPYING file or http://www.wtfpl.net/ for more details.       *
 *********************************************************************/

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
