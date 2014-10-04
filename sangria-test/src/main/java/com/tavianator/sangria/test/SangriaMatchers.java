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

package com.tavianator.sangria.test;

import com.google.inject.Module;
import org.hamcrest.Matcher;

/**
 * Guice-related Hamcrest matchers.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.2
 * @since 1.2
 */
public final class SangriaMatchers {
    private SangriaMatchers() {
        // Not for instantiating
    }

    /**
     * @return A {@link Matcher} that checks whether a {@link Module}'s bindings can be de-duplicated successfully.
     */
    public static Matcher<Module> atomic() {
        return new AtomicMatcher();
    }

    /**
     * @return A {@link Matcher} that checks whether a {@link Module} follows Guice best practices.
     */
    public static Matcher<Module> followsBestPractices() {
        return new BestPracticesMatcher();
    }
}
