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

import java.lang.annotation.Annotation;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link UniqueAnnotations}.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
@UniqueAnnotations.UniqueAnnotation(100)
public class UniqueAnnotationsTest {
    @Test
    public void testUniqueness() {
        Annotation a1 = UniqueAnnotations.create();
        Annotation a2 = UniqueAnnotations.create();

        assertThat(a1, equalTo(a1));
        assertThat(a2, equalTo(a2));

        assertThat(a1, not(equalTo(a2)));
        assertThat(a2, not(equalTo(a1)));
    }

    @Test
    public void testEquality() {
        Annotation real = getClass().getAnnotation(UniqueAnnotations.UniqueAnnotation.class);
        Annotation fake = UniqueAnnotations.create(100);

        assertThat(real, equalTo(fake));
        assertThat(fake, equalTo(real));
    }
}
