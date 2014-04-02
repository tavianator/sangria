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
