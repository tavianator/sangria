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

package com.tavianator.sangria.core;

import com.google.inject.Key;

/**
 * Utility class for pretty-printing messages containing types.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
public class PrettyTypes {
    private PrettyTypes() {
        // Not for instantiating
    }

    /**
     * Format a message.
     *
     * @param message The format string.
     * @param args    The format arguments, possibly containing {@link Class} or {@link Key} instances to be
     *                pretty-printed.
     * @return A formatted message.
     * @see String#format(String, Object...)
     */
    public static String format(String message, Object... args) {
        // This is like Guice's internal Errors.format()
        Object[] prettyArgs = new Object[args.length];
        for (int i = 0; i < args.length; ++i) {
            Object arg = args[i];
            Object prettyArg;

            if (arg instanceof Class) {
                prettyArg = format((Class<?>)arg);
            } else if (arg instanceof Key) {
                prettyArg = format((Key<?>)arg);
            } else {
                prettyArg = arg;
            }

            prettyArgs[i] = prettyArg;
        }

        return String.format(message, prettyArgs);
    }

    private static String format(Class<?> type) {
        return type.getCanonicalName();
    }

    private static String format(Key<?> key) {
        StringBuilder builder = new StringBuilder(key.getTypeLiteral().toString());
        PotentialAnnotation annotation = PotentialAnnotation.from(key);
        if (annotation.hasAnnotation()) {
            builder.append(" annotated with ")
                    .append(annotation);
        }
        return builder.toString();
    }
}
