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
        if (key.getAnnotationType() != null) {
            builder.append(" annotated with ");
            if (key.getAnnotation() != null) {
                builder.append(key.getAnnotation());
            } else {
                builder.append("@")
                        .append(format(key.getAnnotationType()));
            }
        }
        return builder.toString();
    }
}
