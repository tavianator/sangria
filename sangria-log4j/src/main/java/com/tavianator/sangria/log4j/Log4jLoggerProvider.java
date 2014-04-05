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

package com.tavianator.sangria.log4j;

import javax.inject.Singleton;

import com.google.inject.spi.InjectionPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tavianator.sangria.contextual.ContextSensitiveProvider;

/**
 * Actual {@link Logger} provider implementation.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
@Singleton
class Log4jLoggerProvider implements ContextSensitiveProvider<Logger> {
    @Override
    public Logger getInContext(InjectionPoint injectionPoint) {
        return LogManager.getLogger(injectionPoint.getDeclaringType().getRawType());
    }

    @Override
    public Logger getInUnknownContext() {
        return LogManager.getRootLogger();
    }
}
