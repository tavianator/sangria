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

package com.tavianator.sangria.contextual;

import java.lang.annotation.Annotation;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Objects;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.DependencyAndSource;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.util.Providers;

import com.tavianator.sangria.core.DelayedError;

/**
 * A binder for {@link ContextSensitiveProvider}s.
 *
 * <p>
 * For example, to bind a custom logger provider, you can write this inside {@link AbstractModule#configure()}:
 * </p>
 *
 * <pre>
 * ContextSensitiveBinder.create(binder())
 *         .bind(CustomLogger.class)
 *         .toContextSensitiveProvider(CustomLoggerProvider.class);
 * </pre>
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
public class ContextSensitiveBinder {
    private static final Class<?>[] SKIPPED_SOURCES = {
            ContextSensitiveBinder.class,
            BindingBuilder.class,
    };

    private final Binder binder;

    /**
     * Create a {@link ContextSensitiveBinder}.
     *
     * @param binder The {@link Binder} to use.
     * @return A {@link ContextSensitiveBinder} instance.
     */
    public static ContextSensitiveBinder create(Binder binder) {
        return new ContextSensitiveBinder(binder);
    }

    private ContextSensitiveBinder(Binder binder) {
        this.binder = binder.skipSources(SKIPPED_SOURCES);
    }

    /**
     * See the EDSL examples {@link ContextSensitiveBinder here}.
     */
    public <T> AnnotatedContextSensitiveBindingBuilder<T> bind(Class<T> type) {
        return new BindingBuilder<>(Key.get(type));
    }

    /**
     * See the EDSL examples {@link ContextSensitiveBinder here}.
     */
    public <T> AnnotatedContextSensitiveBindingBuilder<T> bind(TypeLiteral<T> type) {
        return new BindingBuilder<>(Key.get(type));
    }

    /**
     * See the EDSL examples {@link ContextSensitiveBinder here}.
     */
    public <T> ContextSensitiveBindingBuilder<T> bind(Key<T> key) {
        return new BindingBuilder<>(key);
    }

    /**
     * Fluent binding builder implementation.
     */
    private class BindingBuilder<T> implements AnnotatedContextSensitiveBindingBuilder<T> {
        private final Key<T> bindingKey;
        private final DelayedError error;

        BindingBuilder(Key<T> bindingKey) {
            this.bindingKey = bindingKey;
            this.error = DelayedError.create(binder, "Missing call to toContextSensitiveProvider() for %s", bindingKey);
        }

        @Override
        public ContextSensitiveBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType) {
            error.cancel();
            return new BindingBuilder<>(Key.get(bindingKey.getTypeLiteral(), annotationType));
        }

        @Override
        public ContextSensitiveBindingBuilder<T> annotatedWith(Annotation annotation) {
            error.cancel();
            return new BindingBuilder<>(Key.get(bindingKey.getTypeLiteral(), annotation));
        }

        @Override
        public void toContextSensitiveProvider(Class<? extends ContextSensitiveProvider<? extends T>> type) {
            toContextSensitiveProvider(Key.get(type));
        }

        @Override
        public void toContextSensitiveProvider(TypeLiteral<? extends ContextSensitiveProvider<? extends T>> type) {
            toContextSensitiveProvider(Key.get(type));
        }

        @Override
        public void toContextSensitiveProvider(Key<? extends ContextSensitiveProvider<? extends T>> key) {
            error.cancel();

            binder.bind(bindingKey).toProvider(new ProviderAdapter<>(key));
            binder.bindListener(new BindingMatcher(bindingKey), new Trigger(bindingKey));
        }

        @Override
        public void toContextSensitiveProvider(ContextSensitiveProvider<? extends T> provider) {
            error.cancel();

            binder.bind(bindingKey).toProvider(new ProviderAdapter<>(provider));
            binder.bindListener(new BindingMatcher(bindingKey), new Trigger(bindingKey));
            // Match the behaviour of LinkedBindingBuilder#toProvider(Provider)
            binder.requestInjection(provider);
        }
    }

    /**
     * Adapter from {@link ContextSensitiveProvider} to {@link Provider}.
     */
    private static class ProviderAdapter<T> implements Provider<T> {
        private static final ThreadLocal<InjectionPoint> CURRENT_CONTEXT = new ThreadLocal<>();

        private final Object equalityKey;
        private final @Nullable Key<? extends ContextSensitiveProvider<? extends T>> providerKey;
        private Provider<? extends ContextSensitiveProvider<? extends T>> provider;

        ProviderAdapter(Key<? extends ContextSensitiveProvider<? extends T>> providerKey) {
            this.equalityKey = providerKey;
            this.providerKey = providerKey;
        }

        ProviderAdapter(ContextSensitiveProvider<T> provider) {
            this.equalityKey = provider;
            this.providerKey = null;
            this.provider = Providers.of(provider);
        }

        @Inject
        void inject(Injector injector) {
            if (provider == null) {
                provider = injector.getProvider(providerKey);
            }
        }

        static void pushContext(InjectionPoint ip) {
            CURRENT_CONTEXT.set(ip);
        }

        static void popContext() {
            CURRENT_CONTEXT.remove();
        }

        @Override
        public T get() {
            ContextSensitiveProvider<? extends T> delegate = provider.get();
            InjectionPoint ip = CURRENT_CONTEXT.get();
            if (ip != null) {
                return delegate.getInContext(ip);
            } else {
                return delegate.getInUnknownContext();
            }
        }

        // Have to implement equals()/hashCode() here to support binding de-duplication
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (!(obj instanceof ProviderAdapter)) {
                return false;
            }

            ProviderAdapter<?> other = (ProviderAdapter<?>)obj;
            return equalityKey.equals(other.equalityKey);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(equalityKey);
        }
    }

    /**
     * {@link Matcher} for {@link Binding}s for specific {@link Key}s.
     */
    private static class BindingMatcher extends AbstractMatcher<Binding<?>> {
        private final Key<?> key;

        BindingMatcher(Key<?> key) {
            this.key = key;
        }

        @Override
        public boolean matches(Binding<?> binding) {
            return key.equals(binding.getKey());
        }
    }

    /**
     * {@link ProvisionListener} that sets up the current {@link InjectionPoint}.
     */
    private static class Trigger implements ProvisionListener {
        private final Key<?> key;

        Trigger(Key<?> key) {
            this.key = key;
        }

        @Override
        public <T> void onProvision(ProvisionInvocation<T> provision) {
            for (DependencyAndSource dependencyAndSource : provision.getDependencyChain()) {
                Dependency<?> dependency = dependencyAndSource.getDependency();
                if (dependency != null && key.equals(dependency.getKey())) {
                    try {
                        ProviderAdapter.pushContext(dependency.getInjectionPoint());
                        provision.provision();
                    } finally {
                        ProviderAdapter.popContext();
                    }

                    break;
                }
            }
        }

        // Allow listeners to be de-duplicated
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (!(obj instanceof Trigger)) {
                return false;
            }

            Trigger other = (Trigger)obj;
            return key.equals(other.key);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key);
        }
    }
}
