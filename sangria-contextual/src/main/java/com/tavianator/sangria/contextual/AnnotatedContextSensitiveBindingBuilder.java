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

import java.lang.annotation.Annotation;

/**
 * See the EDSL examples {@link ContextSensitiveBinder here}.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
public interface AnnotatedContextSensitiveBindingBuilder<T> extends ContextSensitiveBindingBuilder<T> {
    /**
     * See the EDSL examples {@link ContextSensitiveBinder here}.
     */
    ContextSensitiveBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType);

    /**
     * See the EDSL examples {@link ContextSensitiveBinder here}.
     */
    ContextSensitiveBindingBuilder<T> annotatedWith(Annotation annotation);
}
