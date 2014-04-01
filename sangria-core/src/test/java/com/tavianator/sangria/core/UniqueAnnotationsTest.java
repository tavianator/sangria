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
