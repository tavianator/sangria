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
