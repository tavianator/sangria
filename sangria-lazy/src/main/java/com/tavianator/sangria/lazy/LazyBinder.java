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

package com.tavianator.sangria.lazy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import javax.inject.Provider;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderWithExtensionVisitor;
import com.google.inject.util.Types;

import com.tavianator.sangria.core.PotentialAnnotation;

/**
 * Binder for {@link Lazy} instances.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.2
 * @since 1.2
 */
public class LazyBinder {
    private static final Class<?>[] SKIPPED_SOURCES = {
            LazyBinder.class,
            BindingAnnotator.class,
            LazyBindingBuilder.class,
    };

    private final Binder binder;

    private LazyBinder(Binder binder) {
        this.binder = binder;
    }

    /**
     * Create a {@link LazyBinder}.
     *
     * @param binder The {@link Binder} to use.
     * @return A {@link LazyBinder} instance.
     */
    public static LazyBinder create(Binder binder) {
        return new LazyBinder(binder.skipSources(SKIPPED_SOURCES));
    }

    @SuppressWarnings("unchecked")
    private static <T> TypeLiteral<Lazy<T>> lazyOf(TypeLiteral<T> type) {
        return (TypeLiteral<Lazy<T>>)TypeLiteral.get(Types.newParameterizedType(Lazy.class, type.getType()));
    }

    /**
     * See the EDSL examples at {@link Lazy}.
     */
    public <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
        return bind(TypeLiteral.get(type));
    }

    /**
     * See the EDSL examples at {@link Lazy}.
     */
    public <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> type) {
        AnnotatedBindingBuilder<Lazy<T>> lazyBinding = binder.bind(lazyOf(type));
        return new LazyBindingBuilder<>(binder, type, lazyBinding, PotentialAnnotation.none());
    }

    /**
     * Applies an annotation to an {@link AnnotatedBindingBuilder}.
     */
    private static class BindingAnnotator<T> implements PotentialAnnotation.Visitor<LinkedBindingBuilder<T>> {
        private final AnnotatedBindingBuilder<T> builder;

        BindingAnnotator(AnnotatedBindingBuilder<T> builder) {
            this.builder = builder;
        }

        @Override
        public LinkedBindingBuilder<T> visitNoAnnotation() {
            return builder;
        }

        @Override
        public LinkedBindingBuilder<T> visitAnnotationType(Class<? extends Annotation> annotationType) {
            return builder.annotatedWith(annotationType);
        }

        @Override
        public LinkedBindingBuilder<T> visitAnnotationInstance(Annotation annotation) {
            return builder.annotatedWith(annotation);
        }
    }

    /**
     * See the EDSL examples at {@link Lazy}.
     */
    public <T> LinkedBindingBuilder<T> bind(Key<T> key) {
        TypeLiteral<T> type = key.getTypeLiteral();
        PotentialAnnotation potentialAnnotation = PotentialAnnotation.from(key);
        return potentialAnnotation.accept(new BindingAnnotator<>(bind(type)));
    }

    /**
     * Actual binder implementation.
     */
    private static class LazyBindingBuilder<T> implements AnnotatedBindingBuilder<T> {
        private final Binder binder;
        private final TypeLiteral<T> type;
        private final AnnotatedBindingBuilder<Lazy<T>> lazyBinding;
        private final PotentialAnnotation potentialAnnotation;

        LazyBindingBuilder(
                Binder binder,
                TypeLiteral<T> type,
                AnnotatedBindingBuilder<Lazy<T>> lazyBinding,
                PotentialAnnotation potentialAnnotation) {
            this.binder = binder;
            this.type = type;
            this.lazyBinding = lazyBinding;
            this.potentialAnnotation = potentialAnnotation;
        }

        @Override
        public LinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType) {
            PotentialAnnotation newAnnotation = potentialAnnotation.annotatedWith(annotationType);
            Key<T> key = newAnnotation.getKey(type);

            lazyBinding.annotatedWith(annotationType)
                    .toProvider(new LazyProvider<>(binder.getProvider(key), key));

            return new LazyBindingBuilder<>(binder, type, null, newAnnotation);
        }

        @Override
        public LinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
            PotentialAnnotation newAnnotation = potentialAnnotation.annotatedWith(annotation);
            Key<T> key = newAnnotation.getKey(type);

            lazyBinding.annotatedWith(annotation)
                    .toProvider(new LazyProvider<>(binder.getProvider(key), key));

            return new LazyBindingBuilder<>(binder, type, null, newAnnotation);
        }

        /**
         * @return A binding builder for the underlying binding.
         */
        private LinkedBindingBuilder<T> makeBinder() {
            return binder.bind(potentialAnnotation.getKey(type));
        }

        @Override
        public ScopedBindingBuilder to(Class<? extends T> implementation) {
            return makeBinder().to(implementation);
        }

        @Override
        public ScopedBindingBuilder to(TypeLiteral<? extends T> implementation) {
            return makeBinder().to(implementation);
        }

        @Override
        public ScopedBindingBuilder to(Key<? extends T> targetKey) {
            return makeBinder().to(targetKey);
        }

        @Override
        public void toInstance(T instance) {
            makeBinder().toInstance(instance);
        }

        @Override
        public ScopedBindingBuilder toProvider(com.google.inject.Provider<? extends T> provider) {
            return makeBinder().toProvider(provider);
        }

        @Override
        public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {
            return makeBinder().toProvider(provider);
        }

        @Override
        public ScopedBindingBuilder toProvider(Class<? extends Provider<? extends T>> providerType) {
            return makeBinder().toProvider(providerType);
        }

        @Override
        public ScopedBindingBuilder toProvider(TypeLiteral<? extends Provider<? extends T>> providerType) {
            return makeBinder().toProvider(providerType);
        }

        @Override
        public ScopedBindingBuilder toProvider(Key<? extends Provider<? extends T>> providerKey) {
            return makeBinder().toProvider(providerKey);
        }

        @Override
        public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor) {
            return makeBinder().toConstructor(constructor);
        }

        @Override
        public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor, TypeLiteral<? extends S> type) {
            return makeBinder().toConstructor(constructor, type);
        }

        @Override
        public void in(Class<? extends Annotation> scopeAnnotation) {
            makeBinder().in(scopeAnnotation);
        }

        @Override
        public void in(Scope scope) {
            makeBinder().in(scope);
        }

        @Override
        public void asEagerSingleton() {
            makeBinder().asEagerSingleton();
        }
    }

    private static class LazyProvider<T> implements LazyBinding<T>, ProviderWithExtensionVisitor<Lazy<T>> {
        private final Provider<T> provider;
        private final Key<T> key;

        LazyProvider(Provider<T> provider, Key<T> key) {
            this.provider = provider;
            this.key = key;
        }

        @Override
        public Lazy<T> get() {
            return new Lazy<>(provider);
        }

        @Override
        public Key<T> getTargetKey() {
            return key;
        }

        @SuppressWarnings("unchecked") // B must be Lazy<T>
        @Override
        public <B, V> V acceptExtensionVisitor(BindingTargetVisitor<B, V> visitor, ProviderInstanceBinding<? extends B> binding) {
            if (visitor instanceof LazyBindingVisitor) {
                return ((LazyBindingVisitor<T, V>)visitor).visit(this);
            } else {
                return visitor.visit(binding);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (!(obj instanceof LazyProvider)) {
                return false;
            }

            LazyProvider<?> other = (LazyProvider<?>) obj;
            return key.equals(other.key);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }
}
