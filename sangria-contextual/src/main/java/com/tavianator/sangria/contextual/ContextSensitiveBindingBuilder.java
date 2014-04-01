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

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * See the EDSL examples {@link ContextSensitiveBinder here}.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
public interface ContextSensitiveBindingBuilder<T> {
    /**
     * See the EDSL examples {@link ContextSensitiveBinder here}.
     */
    void toContextSensitiveProvider(Class<? extends ContextSensitiveProvider<T>> type);

    /**
     * See the EDSL examples {@link ContextSensitiveBinder here}.
     */
    void toContextSensitiveProvider(TypeLiteral<? extends ContextSensitiveProvider<T>> type);

    /**
     * See the EDSL examples {@link ContextSensitiveBinder here}.
     */
    void toContextSensitiveProvider(Key<? extends ContextSensitiveProvider<T>> type);

    /**
     * See the EDSL examples {@link ContextSensitiveBinder here}.
     */
    void toContextSensitiveProvider(ContextSensitiveProvider<T> provider);
}
