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

package com.tavianator.sangria.listbinder;

import java.lang.annotation.Annotation;
import java.util.*;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ListMultimap;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.spi.Message;
import com.google.inject.util.Types;

import com.tavianator.sangria.core.PotentialAnnotation;
import com.tavianator.sangria.core.PrettyTypes;
import com.tavianator.sangria.core.Priority;
import com.tavianator.sangria.core.TypeLiterals;
import com.tavianator.sangria.core.UniqueAnnotations;

/**
 * A multi-binder with guaranteed order.
 *
 * <p>
 * {@link ListBinder} is much like {@link Multibinder}, except it provides a guaranteed iteration order, and binds a
 * {@link List} instead of a {@link Set}. For example:
 * </p>
 *
 * <pre>
 * ListBinder&lt;String&gt; listBinder = ListBinder.build(binder(), String.class)
 *         .withDefaultPriority();
 * listBinder.addBinding().toInstance("a");
 * listBinder.addBinding().toInstance("b");
 * </pre>
 *
 * <p>
 * This will create a binding for a {@code List<String>}, which contains {@code "a"} followed by {@code "b"}. It also
 * creates a binding for {@code List<Provider<String>>} &mdash; this may be useful in more advanced cases to allow list
 * elements to be lazily loaded.
 * </p>
 *
 * <p>To add an annotation to the list binding, simply write this:</p>
 *
 * <pre>
 * ListBinder&lt;String&gt; listBinder = ListBinder.build(binder(), String.class)
 *         .annotatedWith(Names.named("name"))
 *         .withDefaultPriority();
 * </pre>
 *
 * <p>
 * and the created binding will be {@code @Named("name") List<String>} instead.
 * </p>
 *
 * <p>
 * For large lists, it may be helpful to split up their specification across different modules. This is accomplished by
 * specifying <em>priorities</em> for the {@link ListBinder}s when they are created. For example:
 * </p>
 *
 * <pre>
 * // In some module
 * ListBinder&lt;String&gt; listBinder1 = ListBinder.build(binder(), String.class)
 *         .withPriority(0);
 * listBinder1.addBinding().toInstance("a");
 * listBinder1.addBinding().toInstance("b");
 *
 * // ... some other module
 * ListBinder&lt;String&gt; listBinder2 = ListBinder.build(binder(), String.class)
 *         .withPriority(1);
 * listBinder2.addBinding().toInstance("c");
 * listBinder2.addBinding().toInstance("d");
 * </pre>
 *
 * <p>
 * The generated list will contain {@code "a"}, {@code "b"}, {@code "c"}, {@code "d"}, in order. This happens because
 * the first {@link ListBinder} had a smaller priority, so its entries come first. For more information about the
 * priority system, see {@link Priority}.
 * </p>
 *
 * @param <T> The type of the list element.
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.1
 * @since 1.1
 */
public class ListBinder<T> {
    private static final Class<?>[] SKIPPED_SOURCES = {
            ListBinder.class,
            BuilderImpl.class,
    };

    private final Binder binder;
    private final Multibinder<ListElement<T>> multibinder;
    private final Multibinder<ListBinderErrors<T>> errorMultibinder;
    private final TypeLiteral<T> entryType;
    private final Key<List<T>> listKey;
    private final Key<List<Provider<T>>> listOfProvidersKey;
    private final Key<Set<ListElement<T>>> setKey;
    private final Key<Set<ListBinderErrors<T>>> errorSetKey;
    private final PotentialAnnotation potentialAnnotation;
    private final Priority initialPriority;
    private Priority priority;

    private ListBinder(
            Binder binder,
            TypeLiteral<T> entryType,
            PotentialAnnotation potentialAnnotation,
            Priority initialPriority) {
        this.binder = binder;
        this.entryType = entryType;

        TypeLiteral<ListElement<T>> elementType = listElementOf(entryType);
        TypeLiteral<ListBinderErrors<T>> errorsType = listBinderErrorsOf(entryType);
        this.listKey = potentialAnnotation.getKey(TypeLiterals.listOf(entryType));
        this.listOfProvidersKey = potentialAnnotation.getKey(TypeLiterals.listOf(TypeLiterals.providerOf(entryType)));
        this.setKey = potentialAnnotation.getKey(TypeLiterals.setOf(elementType));
        this.errorSetKey = potentialAnnotation.getKey(TypeLiterals.setOf(errorsType));
        this.multibinder = potentialAnnotation.accept(new MultibinderMaker<>(binder, elementType));
        this.errorMultibinder = potentialAnnotation.accept(new MultibinderMaker<>(binder, errorsType));

        this.potentialAnnotation = potentialAnnotation;
        this.priority = this.initialPriority = initialPriority;
    }

    @SuppressWarnings("unchecked")
    private static <T> TypeLiteral<ListElement<T>> listElementOf(TypeLiteral<T> type) {
        return (TypeLiteral<ListElement<T>>)TypeLiteral.get(Types.newParameterizedType(ListElement.class, type.getType()));
    }

    @SuppressWarnings("unchecked")
    private static <T> TypeLiteral<ListBinderErrors<T>> listBinderErrorsOf(TypeLiteral<T> type) {
        return (TypeLiteral<ListBinderErrors<T>>)TypeLiteral.get(Types.newParameterizedType(ListBinderErrors.class, type.getType()));
    }

    /**
     * {@link PotentialAnnotation.Visitor} that makes {@link Multibinder}s with the given annotation.
     */
    private static class MultibinderMaker<T> implements PotentialAnnotation.Visitor<Multibinder<T>> {
        private final Binder binder;
        private final TypeLiteral<T> type;

        MultibinderMaker(Binder binder, TypeLiteral<T> type) {
            this.binder = binder;
            this.type = type;
        }

        @Override
        public Multibinder<T> visitNoAnnotation() {
            return Multibinder.newSetBinder(binder, type);
        }

        @Override
        public Multibinder<T> visitAnnotationType(Class<? extends Annotation> annotationType) {
            return Multibinder.newSetBinder(binder, type, annotationType);
        }

        @Override
        public Multibinder<T> visitAnnotationInstance(Annotation annotation) {
            return Multibinder.newSetBinder(binder, type, annotation);
        }
    }

    /**
     * Start building a {@link ListBinder}.
     *
     * @param binder The current binder, usually {@link AbstractModule#binder()}.
     * @param type   The type of the list element.
     * @param <T>    The type of the list element.
     * @return A fluent builder.
     */
    public static <T> AnnotatedListBinderBuilder<T> build(Binder binder, Class<T> type) {
        return build(binder, TypeLiteral.get(type));
    }

    /**
     * Start building a {@link ListBinder}.
     *
     * @param binder The current binder, usually {@link AbstractModule#binder()}.
     * @param type   The type of the list element.
     * @param <T>    The type of the list element.
     * @return A fluent builder.
     */
    public static <T> AnnotatedListBinderBuilder<T> build(Binder binder, TypeLiteral<T> type) {
        return new BuilderImpl<>(binder.skipSources(SKIPPED_SOURCES), type, PotentialAnnotation.none());
    }

    private static class BuilderImpl<T> implements AnnotatedListBinderBuilder<T> {
        private final Binder binder;
        private final TypeLiteral<T> entryType;
        private final PotentialAnnotation potentialAnnotation;

        BuilderImpl(Binder binder, TypeLiteral<T> type, PotentialAnnotation potentialAnnotation) {
            this.binder = binder;
            this.entryType = type;
            this.potentialAnnotation = potentialAnnotation;
        }

        @Override
        public ListBinderBuilder<T> annotatedWith(Class<? extends Annotation> annotationType) {
            return new BuilderImpl<>(binder, entryType, potentialAnnotation.annotatedWith(annotationType));
        }

        @Override
        public ListBinderBuilder<T> annotatedWith(Annotation annotation) {
            return new BuilderImpl<>(binder, entryType, potentialAnnotation.annotatedWith(annotation));
        }

        @Override
        public ListBinder<T> withDefaultPriority() {
            return create(Priority.getDefault());
        }

        @Override
        public ListBinder<T> withPriority(int weight, int... weights) {
            return create(Priority.create(weight, weights));
        }

        private ListBinder<T> create(Priority priority) {
            ListBinder<T> listBinder = new ListBinder<>(binder, entryType, potentialAnnotation, priority);

            // Add the delayed errors
            Message duplicateBindersError = new Message(PrettyTypes.format("Duplicate %s", listBinder));
            Message conflictingDefaultExplicitError;
            if (priority.isDefault()) {
                conflictingDefaultExplicitError = new Message(PrettyTypes.format("%s conflicts with ListBinder with explicit priority", listBinder));
            } else {
                conflictingDefaultExplicitError = new Message(PrettyTypes.format("%s conflicts with ListBinder with default priority", listBinder));
            }
            listBinder.errorMultibinder.addBinding().toInstance(new ListBinderErrors<T>(
                    priority,
                    duplicateBindersError,
                    conflictingDefaultExplicitError));

            // Set up the exposed bindings
            binder.bind(listBinder.listOfProvidersKey)
                    .toProvider(new ListOfProvidersProvider<>(listBinder));
            binder.bind(listBinder.listKey)
                    .toProvider(new ListOfProvidersAdapter<>(listBinder.listOfProvidersKey));

            return listBinder;
        }
    }

    /**
     * Provider implementation for {@code List&lt;Provider&lt;T&gt;&gt;}.
     */
    private static class ListOfProvidersProvider<T> implements Provider<List<Provider<T>>> {
        private final Key<Set<ListElement<T>>> setKey;
        private final Key<Set<ListBinderErrors<T>>> errorSetKey;
        private final Priority priority;
        private List<Provider<T>> providers;

        ListOfProvidersProvider(ListBinder<T> listBinder) {
            this.setKey = listBinder.setKey;
            this.errorSetKey = listBinder.errorSetKey;
            this.priority = listBinder.initialPriority;
        }

        @Inject
        void inject(Injector injector) {
            validate(injector);
            initialize(injector);
        }

        private void validate(Injector injector) {
            // Note that here we don't report all errors at once, correctness relies on Guice injecting even providers
            // that get de-duplicated. This way, all errors are attached to the right source.

            List<Message> messages = new ArrayList<>();

            // Get the errors into a multimap by priority
            Set<ListBinderErrors<T>> errorSet = injector.getInstance(errorSetKey);
            ListMultimap<Priority, ListBinderErrors<T>> errorMap = ArrayListMultimap.create();
            for (ListBinderErrors<T> errors : errorSet) {
                errorMap.put(errors.priority, errors);
            }

            // Check for duplicate priorities
            List<ListBinderErrors<T>> ourPriorityErrors = errorMap.get(priority);
            ListBinderErrors<T> ourErrors = ourPriorityErrors.get(0);
            if (ourPriorityErrors.size() > 1) {
                messages.add(ourErrors.duplicateBindersError);
            }

            // Check for default and non-default priorities
            if (errorMap.containsKey(Priority.getDefault()) && errorMap.keySet().size() > 1) {
                messages.add(ourErrors.conflictingDefaultExplicitError);
            }

            if (!messages.isEmpty()) {
                throw new CreationException(messages);
            }
        }

        private void initialize(final Injector injector) {
            Set<ListElement<T>> set = injector.getInstance(setKey);
            List<ListElement<T>> elements = new ArrayList<>(set);
            Collections.sort(elements);

            this.providers = FluentIterable.from(elements)
                    .transform(new Function<ListElement<T>, Provider<T>>() {
                        @Override
                        public Provider<T> apply(ListElement<T> input) {
                            return injector.getProvider(input.key);
                        }
                    })
                    .toList();
        }

        @Override
        public List<Provider<T>> get() {
            return providers;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (!(obj instanceof ListOfProvidersProvider)) {
                return false;
            }

            ListOfProvidersProvider<?> other = (ListOfProvidersProvider<?>)obj;
            return setKey.equals(other.setKey);
        }

        @Override
        public int hashCode() {
            return setKey.hashCode();
        }
    }

    /**
     * Provider implementation for {@code List&lt;T&gt;}, in terms of {@code List&lt;Provider&lt;T&gt;&gt;}.
     */
    private static class ListOfProvidersAdapter<T> implements Provider<List<T>> {
        private final Key<List<Provider<T>>> providerListKey;
        private Provider<List<Provider<T>>> provider;

        ListOfProvidersAdapter(Key<List<Provider<T>>> providerListKey) {
            this.providerListKey = providerListKey;
        }

        @Inject
        void inject(final Injector injector) {
            this.provider = injector.getProvider(providerListKey);
        }

        @Override
        public List<T> get() {
            return FluentIterable.from(provider.get())
                    .transform(new Function<Provider<T>, T>() {
                        @Override
                        public T apply(Provider<T> input) {
                            return input.get();
                        }
                    })
                    .toList();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (!(obj instanceof ListOfProvidersAdapter)) {
                return false;
            }

            ListOfProvidersAdapter<?> other = (ListOfProvidersAdapter<?>)obj;
            return providerListKey.equals(other.providerListKey);
        }

        @Override
        public int hashCode() {
            return providerListKey.hashCode();
        }
    }

    /**
     * Add an entry to the list.
     *
     * <p>
     * The entry will be added in order for this {@link ListBinder} instance. Between different {@link ListBinder}s, the
     * order is determined by the {@link ListBinder}'s {@link Priority}.
     * </p>
     *
     * @return A fluent binding builder.
     */
    public LinkedBindingBuilder<T> addBinding() {
        Key<T> key = Key.get(entryType, UniqueAnnotations.create());
        multibinder.addBinding().toInstance(new ListElement<>(key, priority));
        priority = priority.next();
        return binder.bind(key);
    }

    @Override
    public String toString() {
        return PrettyTypes.format("ListBinder<%s>%s with %s",
                entryType,
                (potentialAnnotation.hasAnnotation() ? " annotated with " + potentialAnnotation : ""),
                initialPriority);
    }
}
