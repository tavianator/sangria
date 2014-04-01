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

import javax.inject.Singleton;

import com.google.inject.spi.InjectionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tavianator.sangria.contextual.ContextSensitiveProvider;

/**
 * Actual {@link Logger} provider implementation.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
@Singleton
class Slf4jLoggerProvider implements ContextSensitiveProvider<Logger> {
    @Override
    public Logger getInContext(InjectionPoint injectionPoint) {
        return LoggerFactory.getLogger(injectionPoint.getDeclaringType().getRawType());
    }

    @Override
    public Logger getInUnknownContext() {
        return LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    }
}
