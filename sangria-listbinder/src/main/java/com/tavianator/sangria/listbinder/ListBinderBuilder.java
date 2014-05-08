package com.tavianator.sangria.listbinder;

import com.tavianator.sangria.core.Priority;

/**
 * Fluent builder interface.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.1
 * @since 1.1
 */
public interface ListBinderBuilder<T> {
    /**
     * @return A {@link ListBinder} with the default priority.
     * @see Priority
     */
    ListBinder<T> withDefaultPriority();

    /**
     * @return A {@link ListBinder} with the given priority.
     * @see Priority
     */
    ListBinder<T> withPriority(int weight, int... weights);
}
