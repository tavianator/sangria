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

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.spi.Message;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link DelayedError}.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
public class DelayedErrorTest {
    public @Rule ExpectedException thrown = ExpectedException.none();

    @Test
    public void testFormatString() {
        thrown.expect(CreationException.class);
        thrown.expectMessage("Test java.lang.String");

        // We want the messages from our CreationException to get absorbed into the top-level exception
        thrown.expectMessage(not(containsString("CreationException")));

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                DelayedError.create(binder(), "Test %s", new Key<String>() { });
            }
        });
    }

    @Test
    public void testMessage() {
        thrown.expect(CreationException.class);
        thrown.expectMessage("the message");
        thrown.expectMessage("at the source");
        thrown.expectMessage(not(containsString("CreationException")));

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                DelayedError.create(binder(), new Message("the source", "the message"));
            }
        });
    }

    @Test
    public void testThrowable() {
        final Throwable cause = new IllegalStateException();

        thrown.expect(CreationException.class);
        thrown.expectCause(is(cause));

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                DelayedError.create(binder(), cause);
            }
        });
    }
}
