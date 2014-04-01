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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Qualifier;

import com.google.common.annotations.VisibleForTesting;

/**
 * Re-implementation of Guice's internal UniqueAnnotations utility.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
public class UniqueAnnotations {
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private UniqueAnnotations() {
        // Not for instantiating
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    @VisibleForTesting
    @interface UniqueAnnotation {
        long value();
    }

    /**
     * Actual implementation of {@link UniqueAnnotation}.
     */
    @SuppressWarnings("ClassExplicitlyAnnotation")
    private static class UniqueAnnotationImpl implements UniqueAnnotation {
        private final long value;

        UniqueAnnotationImpl(long value) {
            this.value = value;
        }

        @Override
        public long value() {
            return value;
        }

        public Class<? extends Annotation> annotationType() {
            return UniqueAnnotation.class;
        }

        @Override
        public String toString() {
            return "@" + UniqueAnnotation.class.getName() + "(value=" + value + ")";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (!(obj instanceof UniqueAnnotation)) {
                return false;
            }

            UniqueAnnotation other = (UniqueAnnotation)obj;
            return value == other.value();
        }

        @Override
        public int hashCode() {
            return (127*"value".hashCode()) ^ Long.valueOf(value).hashCode();
        }
    }

    /**
     * @return An {@link Annotation} that will be unequal to every other annotation.
     */
    public static Annotation create() {
        return create(SEQUENCE.getAndIncrement());
    }

    @VisibleForTesting
    static Annotation create(long value) {
        return new UniqueAnnotationImpl(value);
    }
}
