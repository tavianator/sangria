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

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.google.inject.spi.Message;

/**
 * Similar to {@link Binder#addError(String, Object...)}, but can be canceled later. Useful for enforcing correct usage
 * of fluent APIs.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
public class DelayedError {
    private Throwable error;

    /**
     * Create a {@link DelayedError}.
     *
     * @param binder  The binder to attach the error to.
     * @param message The format string for the message.
     * @param args    Arguments that will be passed to the format string.
     * @return A {@link DelayedError} token that can be canceled later.
     * @see Binder#addError(String, Object...)
     */
    public static DelayedError create(Binder binder, String message, Object... args) {
        return create(binder, new Message(PrettyTypes.format(message, args)));
    }

    /**
     * Create a {@link DelayedError}.
     *
     * @param binder The binder to attach the error to.
     * @param t      The {@link Throwable} that caused this potential error.
     * @return A {@link DelayedError} token that can be canceled later.
     * @see Binder#addError(Throwable)
     */
    public static DelayedError create(Binder binder, Throwable t) {
        DelayedError error = new DelayedError(t);
        binder.skipSources(DelayedError.class)
                .requestInjection(error);
        return error;
    }

    /**
     * Create a {@link DelayedError}.
     *
     * @param binder  The binder to attach the error to.
     * @param message The error message.
     * @return A {@link DelayedError} token that can be canceled later.
     * @see Binder#addError(Message)
     */
    public static DelayedError create(Binder binder, Message message) {
        // Using CreationException allows Guice to extract the Message and format it nicely
        return create(binder, new CreationException(ImmutableList.of(message)));
    }

    private DelayedError(Throwable error) {
        this.error = error;
    }

    /**
     * Cancel this error.
     */
    public void cancel() {
        this.error = null;
    }

    @Inject
    void inject(Injector injector) throws Throwable {
        if (error != null) {
            throw error;
        }
    }
}
