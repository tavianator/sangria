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

import java.util.*;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link Priority}s.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.1
 * @since 1.1
 */
public class PriorityTest {
    private final Priority defaultPriority = Priority.getDefault();
    private final Priority one = Priority.create(1);
    private final Priority oneTwo = Priority.create(1, 2);
    private final Priority two = Priority.create(2);

    @Test
    public void testOrdering() {
        List<Priority> list = Arrays.asList(
                defaultPriority.next(),
                two,
                oneTwo.next(),
                oneTwo,
                two.next(),
                defaultPriority,
                defaultPriority.next().next(),
                one,
                one.next());
        Collections.sort(list);
        assertThat(list, contains(
                defaultPriority,
                defaultPriority.next(),
                defaultPriority.next().next(),
                one,
                one.next(),
                oneTwo,
                oneTwo.next(),
                two,
                two.next()));

        assertThat(defaultPriority, equalTo(Priority.getDefault()));
        assertThat(defaultPriority.next(), equalTo(Priority.getDefault().next()));
        assertThat(defaultPriority, not(equalTo(Priority.getDefault().next())));

        assertThat(one, equalTo(Priority.create(1)));
        assertThat(oneTwo, equalTo(Priority.create(1, 2)));
        assertThat(two, equalTo(Priority.create(2)));

        assertThat(oneTwo.hashCode(), equalTo(Priority.create(1, 2).hashCode()));
    }

    @Test
    public void testIsDefault() {
        assertThat(defaultPriority.isDefault(), is(true));
        assertThat(defaultPriority.next().isDefault(), is(true));

        assertThat(one.isDefault(), is(false));
        assertThat(oneTwo.isDefault(), is(false));
        assertThat(two.isDefault(), is(false));
        assertThat(two.next().isDefault(), is(false));
    }

    @Test
    public void testToString() {
        assertThat(Priority.getDefault().toString(), equalTo("Priority.getDefault()"));
        assertThat(Priority.getDefault().next().toString(), equalTo("Priority.getDefault().next(1)"));
        assertThat(Priority.getDefault().next().next().toString(), equalTo("Priority.getDefault().next(2)"));

        assertThat(Priority.create(1).toString(), equalTo("Priority.create(1)"));
        assertThat(Priority.create(1).next().toString(), equalTo("Priority.create(1).next(1)"));
        assertThat(Priority.create(1).next().next().toString(), equalTo("Priority.create(1).next(2)"));

        assertThat(Priority.create(1, 2).toString(), equalTo("Priority.create(1, 2)"));
        assertThat(Priority.create(1, 2).next().toString(), equalTo("Priority.create(1, 2).next(1)"));
        assertThat(Priority.create(1, 2).next().next().toString(), equalTo("Priority.create(1, 2).next(2)"));
    }
}
