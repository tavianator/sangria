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

package com.tavianator.sangria.slf4j;

import com.google.inject.AbstractModule;
import org.slf4j.Logger;

import com.tavianator.sangria.contextual.ContextSensitiveBinder;

/**
 * Module for SLF4J {@link Logger} injection.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
public class SangriaSlf4jModule extends AbstractModule {
    @Override
    protected void configure() {
        ContextSensitiveBinder.create(binder())
                .bind(Logger.class)
                .toContextSensitiveProvider(Slf4jLoggerProvider.class);
    }
}
