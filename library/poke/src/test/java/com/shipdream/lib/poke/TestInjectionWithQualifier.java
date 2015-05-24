/*
 * Copyright 2015 Kejun Xia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shipdream.lib.poke;

import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;

import org.junit.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Named;
import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class TestInjectionWithQualifier extends BaseTestCases {
    @Qualifier
    @Documented
    @Retention(RUNTIME)
    @interface Google {
    }

    @Qualifier
    @Documented
    @Retention(RUNTIME)
    @interface Microsoft {
    }

    interface Os {
    }

    static class iOs implements Os {
    }

    @Google
    static class Android implements Os {
    }

    @Microsoft
    static class Windows implements Os {
    }

    @Test(expected = ProviderConflictException.class)
    public void shouldDetectConflictProviderException() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        SimpleGraph graph = new SimpleGraph();
        graph.register(Os.class, iOs.class);
        graph.register(Os.class, Android.class);
        graph.register(Os.class, Android.class);
    }

    private static class Container {
        @MyInject
        private Os ios;

        @Google
        @MyInject
        private Os android;

        @Microsoft
        @MyInject
        private Os windows;
    }

    @Test
    public void shouldInjectQualifiedWithDifferentInstances() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        SimpleGraph graph = new SimpleGraph();
        graph.register(Os.class, iOs.class);
        graph.register(Os.class, Android.class);
        graph.register(Os.class, Windows.class);

        Container container = new Container();
        graph.inject(container, MyInject.class);

        Container container2 = new Container();
        graph.inject(container2, MyInject.class);

        Assert.assertEquals(container.ios.getClass(), iOs.class);
        Assert.assertEquals(container.android.getClass(), Android.class);
        Assert.assertEquals(container.windows.getClass(), Windows.class);

        Assert.assertTrue(container.ios != container2.ios);
        Assert.assertTrue(container.android != container2.android);
        Assert.assertTrue(container.windows != container2.windows);
    }

    @Test
    public void shouldInjectQualifiedSingletonInstance() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        SimpleGraph graph = new SimpleGraph();
        ScopeCache scopeCache = new ScopeCache();
        graph.register(Os.class, iOs.class, scopeCache);
        graph.register(Os.class, Android.class, scopeCache);
        graph.register(Os.class, Windows.class, scopeCache);

        Container container = new Container();
        graph.inject(container, MyInject.class);

        Container container2 = new Container();
        graph.inject(container2, MyInject.class);

        Assert.assertEquals(container.ios.getClass(), iOs.class);
        Assert.assertEquals(container.android.getClass(), Android.class);
        Assert.assertEquals(container.windows.getClass(), Windows.class);

        Assert.assertTrue(container.ios == container2.ios);
        Assert.assertTrue(container.android == container2.android);
        Assert.assertTrue(container.windows == container2.windows);
    }

    static class ContainerComponent extends Component {
        @Provides
        public Os providesOs() {
            return new iOs();
        }

        //Mismatch os intentionally to test if provides qualifier overrides qualifier of impl class
        @Microsoft
        @Provides
        public Os providesOs1() {
            return new Android();
        }

        //Mismatch os intentionally to test if provides qualifier overrides qualifier of impl class
        @Google
        @Provides
        public Os providesOs2() {
            return new Windows();
        }
    }

    @Test
    public void componentProvidesQualifierShouldOverrideImplClassQualifier() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        SimpleGraph graph = new SimpleGraph();
        graph.register(new ContainerComponent());

        Container container = new Container();
        graph.inject(container, MyInject.class);

        Container container2 = new Container();
        graph.inject(container2, MyInject.class);

        Assert.assertEquals(container.ios.getClass(), iOs.class);
        Assert.assertEquals(container.android.getClass(), Windows.class);
        Assert.assertEquals(container.windows.getClass(), Android.class);

        Assert.assertTrue(container.ios != container2.ios);
        Assert.assertTrue(container.android != container2.android);
        Assert.assertTrue(container.windows != container2.windows);
    }

    interface Book {
    }

    @Named("A")
    static class BookA implements Book {
    }

    @Named("B")
    static class BookB implements Book {
    }

    @Test
    public void namedQualifierShouldBeRecognized() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        class Library {
            @MyInject
            @Named("A")
            private Book b1;

            @MyInject
            @Named("B")
            private Book b2;
        }

        SimpleGraph graph = new SimpleGraph();
        graph.register(Book.class, BookA.class);
        graph.register(Book.class, BookB.class);

        Library library = new Library();
        graph.inject(library, MyInject.class);

        Assert.assertEquals(library.b1.getClass(), BookA.class);
        Assert.assertEquals(library.b2.getClass(), BookB.class);
    }

    @Test
    public void incorrectNamedQualifierShouldBeRecognized() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        class Library {
            @MyInject
            @Named("B")
            private Book b1;
        }

        SimpleGraph graph = new SimpleGraph();
        graph.register(Book.class, BookA.class);
        graph.register(Book.class, BookB.class);

        Library library = new Library();
        graph.inject(library, MyInject.class);

        Assert.assertFalse(library.b1.getClass() == BookA.class);
    }

    @Test(expected = ProviderMissingException.class)
    public void badNamedQualifierShouldBeTreatedAsMissing() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        class Library {
            @MyInject
            @Named("C")
            private Book b1;
        }

        SimpleGraph graph = new SimpleGraph();
        graph.register(Book.class, BookA.class);
        graph.register(Book.class, BookB.class);

        Library library = new Library();
        graph.inject(library, MyInject.class);

        Assert.assertEquals(library.b1.getClass(), BookA.class);
    }

    @Test(expected = ProviderMissingException.class)
    public void badEmptyNamedQualifierShouldBeTreatedAsMissing() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        class Library {
            //Empty named qualifier is allowed but will be different with any non empty string
            //Named qualifier
            @MyInject
            @Named
            private Book b1;
        }

        SimpleGraph graph = new SimpleGraph();
        graph.register(Book.class, BookA.class);
        graph.register(Book.class, BookB.class);

        Library library = new Library();
        graph.inject(library, MyInject.class);

        Assert.assertEquals(library.b1.getClass(), BookA.class);
    }

    interface Food {
    }

    @Named
    static class Rice implements Food {
    }

    static class Wheat implements Food {
    }

    @Test
    public void emptyNamedQualifierShouldBeTreatedAsNormalQualifier() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        class Basket {
            @MyInject
            @Named
            private Food r;

            @MyInject
            private Food w;
        }

        SimpleGraph graph = new SimpleGraph();
        graph.register(Food.class, Rice.class);
        graph.register(Food.class, Wheat.class);

        Basket basket = new Basket();
        graph.inject(basket, MyInject.class);

        Assert.assertEquals(basket.r.getClass(), Rice.class);
        Assert.assertEquals(basket.w.getClass(), Wheat.class);
    }
}
