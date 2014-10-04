package com.tavianator.sangria.test;

import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.spi.Elements;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matcher that checks whether a {@link Module} can be installed multiple times.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.2
 * @since 1.2
 */
final class AtomicMatcher extends TypeSafeDiagnosingMatcher<Module> {
    @Override
    protected boolean matchesSafely(Module item, Description mismatchDescription) {
        // Pass through the SPI to make sure the Module is atomic regardless of its equals() implementation
        // This ensures atomicity even through Modules.override(), for example
        Module copy1 = Elements.getModule(Elements.getElements(item));
        Module copy2 = Elements.getModule(Elements.getElements(item));

        try {
            Guice.createInjector(copy1, copy2);
            return true;
        } catch (CreationException e) {
            mismatchDescription.appendValue(e);
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an atomic Module");
    }
}
