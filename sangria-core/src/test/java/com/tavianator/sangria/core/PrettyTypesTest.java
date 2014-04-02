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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;
import javax.inject.Qualifier;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link PrettyTypes}.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.0
 * @since 1.0
 */
public class PrettyTypesTest {
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    private @interface Simple {
    }

    @Test
    public void testClasses() {
        assertThat(PrettyTypes.format("Class is %s", PrettyTypesTest.class),
                equalTo("Class is com.tavianator.sangria.core.PrettyTypesTest"));

        assertThat(PrettyTypes.format("Class is %s", Simple.class),
                equalTo("Class is com.tavianator.sangria.core.PrettyTypesTest.Simple"));
    }

    @Test
    public void testTypeLiterals() {
        assertThat(PrettyTypes.format("TypeLiteral is %s", new TypeLiteral<List<String>>() { }),
                equalTo("TypeLiteral is java.util.List<java.lang.String>"));
    }

    @Test
    public void testKeys() {
        assertThat(PrettyTypes.format("Key is %s", new Key<List<String>>() { }),
                equalTo("Key is java.util.List<java.lang.String>"));

        assertThat(PrettyTypes.format("Key is %s", new Key<List<String>>(Names.named("test")) { }),
                equalTo("Key is java.util.List<java.lang.String> annotated with @com.google.inject.name.Named(value=test)"));

        assertThat(PrettyTypes.format("Key is %s", new Key<List<String>>(Simple.class) { }),
                equalTo("Key is java.util.List<java.lang.String> annotated with @com.tavianator.sangria.core.PrettyTypesTest.Simple"));
    }
}
