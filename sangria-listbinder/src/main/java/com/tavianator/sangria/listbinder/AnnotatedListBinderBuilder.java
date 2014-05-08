package com.tavianator.sangria.listbinder;

import java.lang.annotation.Annotation;

/**
 * Fluent builder interface.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.1
 * @since 1.1
 */
public interface AnnotatedListBinderBuilder<T> extends ListBinderBuilder<T> {
    /**
     * Make a binder for an annotated list type.
     *
     * @param annotationType The annotation type for the list.
     * @return A fluent builder.
     */
    ListBinderBuilder<T> annotatedWith(Class<? extends Annotation> annotationType);

    /**
     * Make a binder for an annotated list type.
     *
     * @param annotation The annotation instance for the list.
     * @return A fluent builder.
     */
    ListBinderBuilder<T> annotatedWith(Annotation annotation);
}
