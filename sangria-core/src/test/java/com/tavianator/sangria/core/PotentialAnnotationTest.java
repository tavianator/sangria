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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

import com.google.inject.CreationException;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link PotentialAnnotation}s.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.2
 * @since 1.1
 */
public class PotentialAnnotationTest {
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    private @interface Simple {
    }

    private final PotentialAnnotation none = PotentialAnnotation.none();
    private final Annotation nameAnnotation = Names.named("name");

    @Test
    public void testHasAnnotation() {
        assertThat(none.hasAnnotation(), is(false));
        assertThat(none.annotatedWith(Simple.class).hasAnnotation(), is(true));
        assertThat(none.annotatedWith(nameAnnotation).hasAnnotation(), is(true));
    }

    @Test(expected = CreationException.class)
    public void testInvalidAnnotatedWithType() {
        none.annotatedWith(Simple.class)
                .annotatedWith(Simple.class);
    }

    @Test(expected = CreationException.class)
    public void testInvalidAnnotatedWithInstance() {
        none.annotatedWith(nameAnnotation)
                .annotatedWith(nameAnnotation);
    }

    @Test
    public void testGetKey() {
        assertThat(none.getKey(String.class),
                equalTo(new Key<String>() { }));
        assertThat(none.annotatedWith(Simple.class).getKey(String.class),
                equalTo(new Key<String>(Simple.class) { }));
        assertThat(none.annotatedWith(nameAnnotation).getKey(String.class),
                equalTo(new Key<String>(nameAnnotation) { }));
    }

    @Test
    public void testFromKey() {
        assertThat(PotentialAnnotation.from(new Key<String>() { }),
                equalTo(none));
        assertThat(PotentialAnnotation.from(new Key<String>(Simple.class) { }),
                equalTo(none.annotatedWith(Simple.class)));
        assertThat(PotentialAnnotation.from(new Key<String>(nameAnnotation) { }),
                equalTo(none.annotatedWith(nameAnnotation)));
    }

    @Test
    public void testVisitor() {
        PotentialAnnotation.Visitor<String> visitor = new PotentialAnnotation.Visitor<String>() {
            @Override
            public String visitNoAnnotation() {
                return "none";
            }

            @Override
            public String visitAnnotationType(Class<? extends Annotation> annotationType) {
                assertThat((Object)annotationType, equalTo((Object)Simple.class));
                return "type";
            }

            @Override
            public String visitAnnotationInstance(Annotation annotation) {
                assertThat(annotation, equalTo(nameAnnotation));
                return "instance";
            }
        };

        assertThat(none.accept(visitor), equalTo("none"));
        assertThat(none.annotatedWith(Simple.class).accept(visitor), equalTo("type"));
        assertThat(none.annotatedWith(nameAnnotation).accept(visitor), equalTo("instance"));
    }

    @Test
    public void testToString() {
        assertThat(none.toString(),
                equalTo("[no annotation]"));
        assertThat(none.annotatedWith(Simple.class).toString(),
                equalTo("@com.tavianator.sangria.core.PotentialAnnotationTest.Simple"));
        assertThat(none.annotatedWith(nameAnnotation).toString(),
                equalTo("@com.google.inject.name.Named(value=name)"));
    }

    /**
     * Needed to avoid compilation error due to inferred type being anonymous class.
     */
    private static <T> Matcher<Key<T>> equalTo(Key<T> key) {
        return Matchers.equalTo(key);
    }

    private static <T> Matcher<T> equalTo(T object) {
        return Matchers.equalTo(object);
    }
}
