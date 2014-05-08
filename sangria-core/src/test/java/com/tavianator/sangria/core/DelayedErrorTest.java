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
 * @version 1.1
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

    @Test
    public void testCancel() {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                DelayedError error = DelayedError.create(binder(), "Message");
                error.cancel();
            }
        });
    }

    @Test
    public void testLateCancel() {
        final DelayedError[] errorHolder = new DelayedError[1];

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                errorHolder[0] = DelayedError.create(binder(), "Message");
                errorHolder[0].cancel();
            }
        });

        thrown.expect(IllegalStateException.class);
        errorHolder[0].cancel();
    }
}
