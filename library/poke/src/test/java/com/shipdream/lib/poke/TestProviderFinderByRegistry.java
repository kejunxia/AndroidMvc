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
import com.shipdream.lib.poke.util.ReflectUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public class TestProviderFinderByRegistry extends BaseTestCases {
    private Graph graph;
    private ProviderFinderByRegistry providerFinder;

    @Before
    public void setUp() throws Exception {
        providerFinder = new ProviderFinderByRegistry();
        graph = new Graph() {
            {
                addProviderFinders(providerFinder);
            }
        };
    }

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
        providerFinder.register(Os.class, iOs.class);
        providerFinder.register(Os.class, Android.class);
        providerFinder.register(Os.class, Android.class);
    }

    @Test(expected = NullPointerException.class)
    public void should_detect_unregister_null_implementationClassName_error() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException, ClassNotFoundException {
        String impl = null;
        providerFinder.unregister(Os.class, impl);
    }

    @Test(expected = NullPointerException.class)
    public void should_detect_unregister_null_implementationClass_error() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException, ClassNotFoundException {
        Class impl = null;
        providerFinder.unregister(Os.class, impl);
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

        providerFinder.register(Os.class, iOs.class);
        providerFinder.register(Os.class, Android.class);
        providerFinder.register(Os.class, Windows.class);

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

        ScopeCache scopeCache = new ScopeCache();
        providerFinder.register(Os.class, iOs.class, scopeCache);
        providerFinder.register(Os.class, Android.class, scopeCache);
        providerFinder.register(Os.class, Windows.class, scopeCache);

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

        providerFinder.register(new ContainerComponent());

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


        providerFinder.register(Book.class, BookA.class);
        providerFinder.register(Book.class, BookB.class);

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


        providerFinder.register(Book.class, BookA.class);
        providerFinder.register(Book.class, BookB.class);

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


        providerFinder.register(Book.class, BookA.class);
        providerFinder.register(Book.class, BookB.class);

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


        providerFinder.register(Book.class, BookA.class);
        providerFinder.register(Book.class, BookB.class);

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

        providerFinder.register(Food.class, Rice.class);
        providerFinder.register(Food.class, Wheat.class);

        Basket basket = new Basket();
        graph.inject(basket, MyInject.class);

        Assert.assertEquals(basket.r.getClass(), Rice.class);
        Assert.assertEquals(basket.w.getClass(), Wheat.class);
    }

    @Named
    static class Noodle implements Food {
    }

    static class Bread implements Food {
    }

    @Named
    static class Chicken implements Food {
    }

    static class Beef implements Food {
    }

    @Test
    public void overridingClassNameRegisteringShouldWorkAsExpected() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException, ClassNotFoundException {
        class Basket {
            @MyInject
            @Named
            private Food r;

            @MyInject
            private Food w;
        }

        Basket basket = new Basket();

        ScopeCache scopeCache = new ScopeCache();

        providerFinder.register(Food.class, Rice.class.getName(), scopeCache);
        providerFinder.register(Food.class, Wheat.class.getName());
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Rice.class);
        Assert.assertEquals(basket.w.getClass(), Wheat.class);

        boolean conflicted = false;
        try {
            providerFinder.register(Food.class, Noodle.class.getName());
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        conflicted = false;
        try {
            providerFinder.register(Food.class, Bread.class.getName());
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        providerFinder.register(Food.class, Noodle.class.getName(), null, true);
        providerFinder.register(Food.class, Bread.class.getName(), null, true);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Noodle.class);
        Assert.assertEquals(basket.w.getClass(), Bread.class);

        providerFinder.register(Food.class, Chicken.class.getName(), null, true);
        providerFinder.register(Food.class, Beef.class.getName(), null, true);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Chicken.class);
        Assert.assertEquals(basket.w.getClass(), Beef.class);

        providerFinder.unregister(Food.class, Chicken.class.getName());
        providerFinder.unregister(Food.class, Bread.class.getName());
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Rice.class);
        Assert.assertEquals(basket.w.getClass(), Wheat.class);

        providerFinder.unregister(Food.class, Chicken.class.getName());
        providerFinder.unregister(Food.class, Bread.class.getName());
        basket = new Basket();
        boolean shouldCatchProviderMissingException = false;
        try {
            graph.inject(basket, MyInject.class);
        } catch (ProviderMissingException e) {
            shouldCatchProviderMissingException = true;
        }
        Assert.assertTrue(shouldCatchProviderMissingException);
    }

    static class BadComponent extends Component{
        @Provides
        void provideNothing() {
            return;
        }
    }

    @Test
    public void should_throw_exception_when_there_is_void_function_in_component()
            throws ProvideException, ProviderConflictException {
        BadComponent badComponent = new BadComponent();

        try {
            providerFinder.register(badComponent);
        } catch (ProvideException e) {
            Assert.assertTrue(e.getMessage().contains("must not return void"));
            return;
        }
        Assert.fail("Should raise ProvideException for provider returning void");
    }

    @Qualifier @Retention(RUNTIME) @interface Qualifier1 {}

    @Qualifier @Retention(RUNTIME) @interface Qualifier2 {}

    static class DuplicateComponent extends Component{
        @Provides @Qualifier1 @Qualifier2
        String provideText() {
            return "123";
        }
    }

    @Test
    public void should_throw_exception_when_provider_has_more_than_one_qualifier()
            throws ProvideException, ProviderConflictException {
        DuplicateComponent duplicateComponent = new DuplicateComponent();
        try {
            providerFinder.register(duplicateComponent);
        } catch (ProvideException e) {
            Assert.assertTrue(e.getMessage().contains("Only one Qualifier"));
            return;
        }
        Assert.fail("Should raise ProvideException for provider with multiple qualifier");
    }

    @Test
    public void should_throw_exception_if_provider_returns_null()
            throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        Provider<String> provider = new Provider(String.class) {
            @Override
            protected String createInstance() throws ProvideException {
                return null;
            }
        };
        provider.setScopeCache(new ScopeCache());
        providerFinder.register(provider);

        class Container {
            @MyInject
            @Singleton
            private String name;
        }

        Container container = new Container();

        try {
            graph.inject(container, MyInject.class);
        } catch (ProvideException e) {
            Assert.assertTrue(e.getMessage().contains("should not provide NULL as instance"));
            return;
        }
        Assert.fail("Should raise ProvideException for provider returns null");
    }

    @Test
    public void overridingClassRegisteringShouldWorkAsExpected() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        class Basket {
            @MyInject
            @Named
            private Food r;

            @MyInject
            private Food w;
        }

        Basket basket = new Basket();

        providerFinder.register(Food.class, Rice.class);
        providerFinder.register(Food.class, Wheat.class);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Rice.class);
        Assert.assertEquals(basket.w.getClass(), Wheat.class);

        boolean conflicted = false;
        try {
            providerFinder.register(Food.class, Noodle.class);
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        conflicted = false;
        try {
            providerFinder.register(Food.class, Bread.class);
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        providerFinder.register(Food.class, Noodle.class, null, true);
        providerFinder.register(Food.class, Bread.class, null, true);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Noodle.class);
        Assert.assertEquals(basket.w.getClass(), Bread.class);

        providerFinder.register(Food.class, Chicken.class, null, true);
        providerFinder.register(Food.class, Beef.class, null, true);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Chicken.class);
        Assert.assertEquals(basket.w.getClass(), Beef.class);

        providerFinder.unregister(Food.class, Noodle.class);
        providerFinder.unregister(Food.class, Bread.class);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Rice.class);
        Assert.assertEquals(basket.w.getClass(), Wheat.class);

        providerFinder.unregister(Food.class, Chicken.class);
        providerFinder.unregister(Food.class, Bread.class);
        basket = new Basket();
        boolean shouldCatchProviderMissingException = false;
        try {
            graph.inject(basket, MyInject.class);
        } catch (ProviderMissingException e) {
            shouldCatchProviderMissingException = true;
        }
        Assert.assertTrue(shouldCatchProviderMissingException);
    }

    @Test
    public void overridingProviderRegisteringShouldWorkAsExpected() throws
            ProviderConflictException, ProvideException, CircularDependenciesException, ProviderMissingException {
        Provider<Food> providerRice = new Provider<Food>(Food.class) {
            @Override
            protected Food createInstance() throws ProvideException {
                return new Rice();
            }
            @Override
            public Annotation getQualifier() {
                return ReflectUtils.findFirstQualifier(Rice.class);
            }
        };

        Provider<Food> providerWheat = new Provider<Food>(Food.class) {
            @Override
            protected Food createInstance() throws ProvideException {
                return new Wheat();
            }
            @Override
            public Annotation getQualifier() {
                return ReflectUtils.findFirstQualifier(Wheat.class);
            }
        };

        Provider<Food> providerNoodle = new Provider<Food>(Food.class) {
            @Override
            protected Food createInstance() throws ProvideException {
                return new Noodle();
            }
            @Override
            public Annotation getQualifier() {
                return ReflectUtils.findFirstQualifier(Noodle.class);
            }
        };

        Provider<Food> providerBread = new Provider<Food>(Food.class) {
            @Override
            protected Food createInstance() throws ProvideException {
                return new Bread();
            }
            @Override
            public Annotation getQualifier() {
                return ReflectUtils.findFirstQualifier(Bread.class);
            }
        };

        Provider<Food> providerChicken = new Provider<Food>(Food.class) {
            @Override
            protected Food createInstance() throws ProvideException {
                return new Chicken();
            }
            @Override
            public Annotation getQualifier() {
                return ReflectUtils.findFirstQualifier(Chicken.class);
            }
        };

        Provider<Food> providerBeef = new Provider<Food>(Food.class) {
            @Override
            protected Food createInstance() throws ProvideException {
                return new Beef();
            }
            @Override
            public Annotation getQualifier() {
                return ReflectUtils.findFirstQualifier(Beef.class);
            }
        };

        class Basket {
            @MyInject
            @Named
            private Food r;

            @MyInject
            private Food w;
        }

        Basket basket = new Basket();

        providerFinder.register(providerRice);
        providerFinder.register(providerWheat);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Rice.class);
        Assert.assertEquals(basket.w.getClass(), Wheat.class);

        boolean conflicted = false;
        try {
            providerFinder.register(providerNoodle);
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        conflicted = false;
        try {
            providerFinder.register(providerBread);
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        providerFinder.register(providerNoodle, true);
        providerFinder.register(providerBread, true);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Noodle.class);
        Assert.assertEquals(basket.w.getClass(), Bread.class);

        providerFinder.register(providerChicken, true);
        providerFinder.register(providerBeef, true);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Chicken.class);
        Assert.assertEquals(basket.w.getClass(), Beef.class);

        providerFinder.unregister(providerChicken);
        providerFinder.unregister(providerBeef);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Rice.class);
        Assert.assertEquals(basket.w.getClass(), Wheat.class);

        providerFinder.unregister(providerNoodle);
        providerFinder.unregister(providerWheat);
        basket = new Basket();
        boolean shouldCatchProviderMissingException = false;
        try {
            graph.inject(basket, MyInject.class);
        } catch (ProviderMissingException e) {
            shouldCatchProviderMissingException = true;
        }
        Assert.assertTrue(shouldCatchProviderMissingException);
    }

    static class Apple implements Food{}
    static class Orange implements Food{}
    static class Banana implements Food{}

    static class FoodCompA extends Component {
        @Provides @Named
        public Food provideApple() {
            return new Apple();
        }

        @Provides
        public Food provideOrange() {
            return new Orange();
        }
    }

    static class FoodCompB extends Component {
        @Provides
        public Food provideApple() {
            return new Apple();
        }

        @Provides @Named
        public Food provideOrange() {
            return new Orange();
        }
    }

    static class FoodCompC extends Component {
        @Provides @Named
        public Food provideApple() {
            return new Apple();
        }

        @Provides
        public Food provideBanana() {
            return new Banana();
        }
    }

    @Test
    public void overridingComponentRegisteringShouldWorkAsExpected() throws
            ProviderConflictException, ProvideException, CircularDependenciesException, ProviderMissingException {
        class Basket {
            @MyInject
            @Named
            private Food r;

            @MyInject
            private Food w;
        }

        Basket basket = new Basket();

        FoodCompA foodCompA = new FoodCompA();
        FoodCompB foodCompB = new FoodCompB();
        FoodCompC foodCompC = new FoodCompC();

        providerFinder.register(foodCompA);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Apple.class);
        Assert.assertEquals(basket.w.getClass(), Orange.class);

        boolean conflicted = false;
        try {
            providerFinder.register(new FoodCompB());
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        providerFinder.register(foodCompB, true);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Orange.class);
        Assert.assertEquals(basket.w.getClass(), Apple.class);

        providerFinder.register(foodCompC, true);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Apple.class);
        Assert.assertEquals(basket.w.getClass(), Banana.class);

        providerFinder.unregister(foodCompA);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Apple.class);
        Assert.assertEquals(basket.w.getClass(), Orange.class);

        providerFinder.unregister(foodCompA);
        basket = new Basket();
        boolean shouldCatchProviderMissingException = false;
        try {
            graph.inject(basket, MyInject.class);
        } catch (ProviderMissingException e) {
            shouldCatchProviderMissingException = true;
        }
        Assert.assertTrue(shouldCatchProviderMissingException);
    }

    @Test
    public void scopeCacheShouldRemoveUnregisteredBindings() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        class Basket {
            @MyInject
            @Named
            private Food r;

            @MyInject
            private Food w;
        }

        final Banana banana = new Banana();
        final Orange orange = new Orange();
        ScopeCache scopeCache = new ScopeCache();
        ScopeCache scopeCacheOverridden = new ScopeCache();

        Provider<Food> namedProviderBanana = new Provider<Food>(Food.class) {
            @Override
            protected Food createInstance() throws ProvideException {
                return banana;
            }

            @Override
            public Annotation getQualifier() {
                return ReflectUtils.findFirstQualifier(Noodle.class);
            }
        };
        namedProviderBanana.setScopeCache(scopeCache);

        Provider<Food> unnammedProviderOrange = new Provider<Food>(Food.class) {
            @Override
            protected Food createInstance() throws ProvideException {
                return orange;
            }

            @Override
            public Annotation getQualifier() {
                return null;
            }
        };
        unnammedProviderOrange.setScopeCache(scopeCache);

        Provider<Food> namedProviderOrangeOverridden = new Provider<Food>(Food.class) {
            @Override
            protected Food createInstance() throws ProvideException {
                return orange;
            }

            @Override
            public Annotation getQualifier() {
                return ReflectUtils.findFirstQualifier(Chicken.class);
            }
        };
        namedProviderOrangeOverridden.setScopeCache(scopeCacheOverridden);

        Provider<Food> unnamedProviderBananaOverridden = new Provider<Food>(Food.class) {
            @Override
            protected Food createInstance() throws ProvideException {
                return banana;
            }

            @Override
            public Annotation getQualifier() {
                return null;
            }
        };
        unnamedProviderBananaOverridden.setScopeCache(scopeCacheOverridden);

        Basket basket = new Basket();

        providerFinder.register(namedProviderBanana);
        providerFinder.register(unnammedProviderOrange);
        graph.inject(basket, MyInject.class);
        Assert.assertTrue(basket.r == banana);
        Assert.assertTrue(basket.w == orange);
        Assert.assertTrue(findCacheInstance(scopeCache, namedProviderBanana) == banana);
        Assert.assertTrue(findCacheInstance(scopeCache, unnammedProviderOrange) == orange);
        Assert.assertTrue(findCacheInstance(scopeCacheOverridden, namedProviderBanana) == null);
        Assert.assertTrue(findCacheInstance(scopeCacheOverridden, unnammedProviderOrange) == null);

        boolean conflicted = false;
        try {
            providerFinder.register(namedProviderOrangeOverridden);
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        conflicted = false;
        try {
            providerFinder.register(unnamedProviderBananaOverridden);
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        providerFinder.register(namedProviderOrangeOverridden, true);
        providerFinder.register(unnamedProviderBananaOverridden, true);
        graph.inject(basket, MyInject.class);
        Assert.assertTrue(basket.r == orange);
        Assert.assertTrue(basket.w == banana);
        Assert.assertTrue(findCacheInstance(scopeCache, namedProviderBanana) == banana);
        Assert.assertTrue(findCacheInstance(scopeCache, unnammedProviderOrange) == orange);
        Assert.assertTrue(findCacheInstance(scopeCacheOverridden, namedProviderBanana) == orange);
        Assert.assertTrue(findCacheInstance(scopeCacheOverridden, unnammedProviderOrange) == banana);

        providerFinder.unregister(namedProviderBanana);
        providerFinder.unregister(unnammedProviderOrange);
        graph.inject(basket, MyInject.class);
        Assert.assertTrue(basket.r == banana);
        Assert.assertTrue(basket.w == orange);
        Assert.assertTrue(findCacheInstance(scopeCache, namedProviderBanana) == banana);
        Assert.assertTrue(findCacheInstance(scopeCache, unnamedProviderBananaOverridden) == orange);
        Assert.assertTrue(findCacheInstance(scopeCacheOverridden, namedProviderBanana) == null);
        Assert.assertTrue(findCacheInstance(scopeCacheOverridden, unnammedProviderOrange) == null);

        providerFinder.unregister(namedProviderOrangeOverridden);
        providerFinder.unregister(unnammedProviderOrange);
        basket = new Basket();
        boolean shouldCatchProviderMissingException = false;
        try {
            graph.inject(basket, MyInject.class);
        } catch (ProviderMissingException e) {
            shouldCatchProviderMissingException = true;
        }
        Assert.assertTrue(shouldCatchProviderMissingException);
        Assert.assertTrue(findCacheInstance(scopeCache, namedProviderBanana) == null);
        Assert.assertTrue(findCacheInstance(scopeCache, unnamedProviderBananaOverridden) == null);
        Assert.assertTrue(findCacheInstance(scopeCacheOverridden, namedProviderBanana) == null);
        Assert.assertTrue(findCacheInstance(scopeCacheOverridden, unnammedProviderOrange) == null);
    }

    @SuppressWarnings("unchecked")
    private <T> T findCacheInstance(ScopeCache scopeCache, Provider<T> provider) {
        ScopeCache.CachedItem<T> cachedItem = scopeCache.findCacheItem(provider.type(), provider.getQualifier());
        if(cachedItem != null) {
            return cachedItem.instance;
        }
        return null;
    }

    interface Contract {

    }

    class Execution implements Contract {

    }

    @Test
    public void should_be_able_to_register_implementation_into_simple_graph ()
            throws ClassNotFoundException, ProviderConflictException, ProvideException {
        ProviderFinderByRegistry registry = mock(ProviderFinderByRegistry.class);

        SimpleGraph graph = new SimpleGraph(registry);

        //Register by name
        reset(registry);
        graph.register(String.class, "Impl1");
        verify(registry).register(eq(String.class), eq("Impl1"));

        reset(registry);
        ScopeCache cache = mock(ScopeCache.class);
        graph.register(String.class, "Impl2", cache);
        verify(registry).register(eq(String.class), eq("Impl2"), eq(cache));

        reset(registry);
        graph.register(String.class, "Impl3", cache, true);
        verify(registry).register(eq(String.class), eq("Impl3"), eq(cache), eq(true));

        reset(registry);
        graph.register(String.class, "Impl4", cache, false);
        verify(registry).register(eq(String.class), eq("Impl4"), eq(cache), eq(false));

        //Register by class
        reset(registry);
        graph.register(Contract.class, Execution.class);
        verify(registry).register(eq(Contract.class), eq(Execution.class));

        reset(registry);
        graph.register(Contract.class, Execution.class, cache);
        verify(registry).register(eq(Contract.class), eq(Execution.class), eq(cache));

        reset(registry);
        graph.register(Contract.class, Execution.class, cache, true);
        verify(registry).register(eq(Contract.class), eq(Execution.class), eq(cache), eq(true));

        reset(registry);
        graph.register(Contract.class, Execution.class, cache, false);
        verify(registry).register(eq(Contract.class), eq(Execution.class), eq(cache), eq(false));

        //Register by component
        Component component = mock(Component.class);
        reset(registry);
        graph.register(component);
        verify(registry).register(eq(component));

        reset(registry);
        graph.register(component, true);
        verify(registry).register(eq(component), eq(true));

        reset(registry);
        graph.register(component, false);
        verify(registry).register(eq(component), eq(false));

        //Register by provider
        Provider provider = mock(Provider.class);
        reset(registry);
        graph.register(provider);
        verify(registry).register(eq(provider));

        reset(registry);
        graph.register(provider, true);
        verify(registry).register(eq(provider), eq(true));

        reset(registry);
        graph.register(provider, false);
        verify(registry).register(eq(provider), eq(false));
    }

    @Test
    public void should_be_able_to_unregister_implementation_into_simple_graph ()
            throws ClassNotFoundException, ProviderConflictException, ProvideException {
        ProviderFinderByRegistry registry = mock(ProviderFinderByRegistry.class);

        SimpleGraph graph = new SimpleGraph(registry);

        reset(registry);
        graph.unregister(String.class, "Impl1");
        verify(registry).unregister(eq(String.class), eq("Impl1"));

        reset(registry);
        graph.unregister(Contract.class, Execution.class);
        verify(registry).unregister(eq(Contract.class), eq(Execution.class));

        Component component = mock(Component.class);
        reset(registry);
        graph.unregister(component);
        verify(registry).unregister(eq(component));

        Provider provider = mock(Provider.class);
        reset(registry);
        graph.unregister(provider);
        verify(registry).unregister(eq(provider));
    }

}