package com.tavianator.sangria.contextual;

import java.util.*;

import com.google.inject.spi.InjectionPoint;

/**
 * SPI for {@link ContextSensitiveProvider} key bindings.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @see ContextSensitiveBindingBuilder#toContextSensitiveProvider(ContextSensitiveProvider)
 * @since 1.0
 */
public interface ContextSensitiveProviderInstanceBinding<T> {
    /**
     * @return The {@link ContextSensitiveProvider} instance for this binding.
     */
    ContextSensitiveProvider<? extends T> getContextSensitiveProviderInstance();

    /**
     * @return The field and method {@link InjectionPoint}s of the {@link ContextSensitiveProvider} instance.
     */
    Set<InjectionPoint> getInjectionPoints();
}
