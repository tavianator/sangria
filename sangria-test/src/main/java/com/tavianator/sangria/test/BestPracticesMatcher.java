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

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Module;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matcher that checks whether a {@link Module} follows Guice best practices.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.2
 * @since 1.2
 */
final class BestPracticesMatcher extends TypeSafeDiagnosingMatcher<Module> {
    private static final class EnforcerModule extends AbstractModule {
        @Override
        protected void configure() {
            binder().requireAtInjectOnConstructors();
            binder().requireExactBindingAnnotations();
            binder().requireExplicitBindings();
        }
    }

    @Override
    protected boolean matchesSafely(Module item, Description mismatchDescription) {
        try {
            Guice.createInjector(item, new EnforcerModule());
            return true;
        } catch (CreationException e) {
            mismatchDescription.appendValue(e);
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a Module following Guice best practices");
    }
}
