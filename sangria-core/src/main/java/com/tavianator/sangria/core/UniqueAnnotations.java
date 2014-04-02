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
