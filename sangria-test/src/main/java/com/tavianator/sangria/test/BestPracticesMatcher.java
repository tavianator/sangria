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
