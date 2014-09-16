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
import java.util.*;

import com.google.inject.CreationException;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.Message;

/**
 * A record of stored annotations, perfect for builders with {@code annotatedWith()} methods.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.2
 * @since 1.1
 */
public abstract class PotentialAnnotation {
    /**
     * A visitor interface to examine a {@link PotentialAnnotation}'s annotation, if it exists.
     *
     * @param <T> The type to return.
     * @author Tavian Barnes (tavianator@tavianator.com)
     * @version 1.1
     * @since 1.1
     */
    public interface Visitor<T> {
        /**
         * Called when there is no annotation.
         *
         * @return Any value.
         */
        T visitNoAnnotation();

        /**
         * Called when an annotation type is stored.
         *
         * @param annotationType The annotation type.
         * @return Any value.
         */
        T visitAnnotationType(Class<? extends Annotation> annotationType);

        /**
         * Called when an annotation instance is stored.
         *
         * @param annotation The annotation instance.
         * @return Any value.
         */
        T visitAnnotationInstance(Annotation annotation);
    }

    private static final PotentialAnnotation NONE = new NoAnnotation();

    /**
     * @return A {@link PotentialAnnotation} with no annotation.
     */
    public static PotentialAnnotation none() {
        return NONE;
    }

    /**
     * @return A {@link PotentialAnnotation} with the annotation from a {@link Key}.
     * @since 1.2
     */
    public static PotentialAnnotation from(Key<?> key) {
        Annotation instance = key.getAnnotation();
        if (instance != null) {
            return none().annotatedWith(instance);
        }

        Class<? extends Annotation> type = key.getAnnotationType();
        if (type != null) {
            return none().annotatedWith(type);
        }

        return none();
    }

    private PotentialAnnotation() {
    }

    /**
     * Add an annotation.
     *
     * @param annotationType The annotation type to add.
     * @return A new {@link PotentialAnnotation} associated with the given annotation type.
     * @throws CreationException If an annotation is already present.
     */
    public PotentialAnnotation annotatedWith(Class<? extends Annotation> annotationType) {
        throw annotationAlreadyPresent();
    }

    /**
     * Add an annotation.
     *
     * @param annotation The annotation instance to add.
     * @return A new {@link PotentialAnnotation} associated with the given annotation instance.
     * @throws CreationException If an annotation is already present.
     */
    public PotentialAnnotation annotatedWith(Annotation annotation) {
        throw annotationAlreadyPresent();
    }

    private CreationException annotationAlreadyPresent() {
        Message message = new Message("An annotation was already present");
        return new CreationException(Collections.singletonList(message));
    }

    /**
     * @return Whether an annotation is present.
     */
    public abstract boolean hasAnnotation();

    /**
     * Create a {@link Key} with the given type and the stored annotation.
     *
     * @param type The type of the key to create.
     * @param <T>  The type of the key to create.
     * @return A {@link Key}.
     */
    public <T> Key<T> getKey(Class<T> type) {
        return getKey(TypeLiteral.get(type));
    }

    /**
     * Create a {@link Key} with the given type and the stored annotation.
     *
     * @param type The type of the key to create.
     * @param <T>  The type of the key to create.
     * @return A {@link Key}.
     */
    public abstract <T> Key<T> getKey(TypeLiteral<T> type);

    /**
     * Accept a {@link Visitor}.
     *
     * @param visitor The visitor to accept.
     * @param <T> The type for the visitor to return.
     * @return The value produced by the visitor.
     */
    public abstract <T> T accept(Visitor<T> visitor);

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();

    /**
     * Implementation of {@link #none()}.
     */
    private static class NoAnnotation extends PotentialAnnotation {
        @Override
        public PotentialAnnotation annotatedWith(Class<? extends Annotation> annotationType) {
            return new AnnotationType(annotationType);
        }

        @Override
        public PotentialAnnotation annotatedWith(Annotation annotation) {
            return new AnnotationInstance(annotation);
        }

        @Override
        public boolean hasAnnotation() {
            return false;
        }

        @Override
        public <T> Key<T> getKey(TypeLiteral<T> type) {
            return Key.get(type);
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitNoAnnotation();
        }

        @Override
        public boolean equals(Object o) {
            return o == this || o instanceof NoAnnotation;
        }

        @Override
        public int hashCode() {
            return NoAnnotation.class.hashCode();
        }

        @Override
        public String toString() {
            return "[no annotation]";
        }
    }

    /**
     * Implementation of {@link #annotatedWith(Class)}.
     */
    private static class AnnotationType extends PotentialAnnotation {
        private final Class<? extends Annotation> annotationType;

        AnnotationType(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        @Override
        public boolean hasAnnotation() {
            return true;
        }

        @Override
        public <T> Key<T> getKey(TypeLiteral<T> type) {
            return Key.get(type, annotationType);
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitAnnotationType(annotationType);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof AnnotationType)) {
                return false;
            }

            AnnotationType other = (AnnotationType)o;
            return annotationType.equals(other.annotationType);
        }

        @Override
        public int hashCode() {
            return annotationType.hashCode();
        }

        @Override
        public String toString() {
            return "@" + annotationType.getCanonicalName();
        }
    }

    /**
     * Implementation of {@link #annotatedWith(Annotation)}.
     */
    private static class AnnotationInstance extends PotentialAnnotation {
        private final Annotation annotation;

        AnnotationInstance(Annotation annotation) {
            this.annotation = annotation;
        }

        @Override
        public boolean hasAnnotation() {
            return true;
        }

        @Override
        public <T> Key<T> getKey(TypeLiteral<T> type) {
            return Key.get(type, annotation);
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitAnnotationInstance(annotation);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof AnnotationInstance)) {
                return false;
            }

            AnnotationInstance other = (AnnotationInstance)o;
            return annotation.equals(other.annotation);
        }

        @Override
        public int hashCode() {
            return annotation.hashCode();
        }

        @Override
        public String toString() {
            return annotation.toString();
        }
    }
}
