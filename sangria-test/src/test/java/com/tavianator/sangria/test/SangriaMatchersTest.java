package com.tavianator.sangria.test;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.junit.Test;

import static com.tavianator.sangria.test.SangriaMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link SangriaMatchers}.
 *
 * @author Tavian Barnes (tavianator@tavianator.com)
 * @version 1.2
 * @since 1.2
 */
public class SangriaMatchersTest {
    private static class AtomicModule extends AbstractModule {
        private static final Object INSTANCE = new Object();

        @Override
        protected void configure() {
            bind(Object.class)
                    .toInstance(INSTANCE);
        }
    }

    private static class NonAtomicModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Object.class)
                    .toInstance(new Object());
        }
    }

    @Test
    public void testAtomic() {
        assertThat(new AtomicModule(), is(atomic()));
        assertThat(new NonAtomicModule(), is(not(atomic())));
    }

    private static class NoAtInjectModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class);
        }
    }

    private static class InexactBindingAnnotationModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class)
                    .annotatedWith(Named.class)
                    .toInstance("test");
        }

        @Provides
        String getString(@Named("test") String test) {
            return test;
        }
    }

    private static class Injectable {
        @Inject
        Injectable() {
        }
    }

    private static class JustInTimeModule extends AbstractModule {
        @Override
        protected void configure() {
        }

        @Provides
        String getString(Injectable injectable) {
            return "test";
        }
    }

    @Test
    public void testFollowsBestPractices() {
        assertThat(new AtomicModule(), followsBestPractices());
        assertThat(new NoAtInjectModule(), not(followsBestPractices()));
        assertThat(new InexactBindingAnnotationModule(), not(followsBestPractices()));
        assertThat(new JustInTimeModule(), not(followsBestPractices()));
    }
}
