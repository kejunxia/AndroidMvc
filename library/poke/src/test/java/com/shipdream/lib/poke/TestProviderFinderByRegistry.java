/*
 * Copyright 2016 Kejun Xia
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
import com.shipdream.lib.poke.exception.PokeException;
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

public class TestProviderFinderByRegistry extends BaseTestCases {
    private Graph graph;
    private Component component;

    @Before
    public void setUp() throws Exception {
        component = new Component("AppSingleton");
        graph = new Graph();
        graph.setRootComponent(component);
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

    private void registerByClass(Class type, Class impl) throws ProviderConflictException {
        component.register(new ProviderByClassType(type, impl));
    }

    private void registerByName(Class type, String impl) throws ProviderConflictException, ClassNotFoundException {
        component.register(new ProviderByClassName(type, impl));
    }

    @Test(expected = ProviderConflictException.class)
    public void shouldDetectConflictProviderException() throws PokeException {
        registerByClass(Os.class, iOs.class);
        registerByClass(Os.class, Android.class);
        registerByClass(Os.class, Android.class);
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
    public void shouldInjectQualifiedWithDifferentInstances() throws PokeException {
        Component c = new Component(false);
        c.register(new ProviderByClassType(Os.class, iOs.class));
        c.register(new ProviderByClassType(Os.class, Android.class));
        c.register(new ProviderByClassType(Os.class, Windows.class));
        graph.setRootComponent(c);

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
    public void shouldInjectQualifiedSingletonInstance() throws PokeException {
        registerByClass(Os.class, iOs.class);
        registerByClass(Os.class, Android.class);
        registerByClass(Os.class, Windows.class);

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

    static class ContainerModule {
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
    public void componentProvidesQualifierShouldOverrideImplClassQualifier() throws PokeException {
        graph.setRootComponent(new Component(false).register(new ContainerModule()));

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
    public void namedQualifierShouldBeRecognized() throws PokeException {
        class Library {
            @MyInject
            @Named("A")
            private Book b1;

            @MyInject
            @Named("B")
            private Book b2;
        }


        registerByClass(Book.class, BookA.class);
        registerByClass(Book.class, BookB.class);

        Library library = new Library();
        graph.inject(library, MyInject.class);

        Assert.assertEquals(library.b1.getClass(), BookA.class);
        Assert.assertEquals(library.b2.getClass(), BookB.class);
    }

    @Test
    public void incorrectNamedQualifierShouldBeRecognized() throws PokeException {
        class Library {
            @MyInject
            @Named("B")
            private Book b1;
        }

        registerByClass(Book.class, BookA.class);
        registerByClass(Book.class, BookB.class);

        Library library = new Library();
        graph.inject(library, MyInject.class);

        Assert.assertFalse(library.b1.getClass() == BookA.class);
    }

    @Test(expected = ProviderMissingException.class)
    public void badNamedQualifierShouldBeTreatedAsMissing() throws
            ProviderConflictException, ProvideException,
            CircularDependenciesException, ProviderMissingException {

        class Library {
            @MyInject
            @Named("C")
            private Book b1;
        }

        registerByClass(Book.class, BookA.class);
        registerByClass(Book.class, BookB.class);

        Library library = new Library();
        graph.inject(library, MyInject.class);

        Assert.assertEquals(library.b1.getClass(), BookA.class);
    }

    @Test(expected = ProviderMissingException.class)
    public void badEmptyNamedQualifierShouldBeTreatedAsMissing() throws PokeException {
        class Library {
            //Empty named qualifier is allowed but will be different with any non empty string
            //Named qualifier
            @MyInject
            @Named
            private Book b1;
        }


        registerByClass(Book.class, BookA.class);
        registerByClass(Book.class, BookB.class);

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
    public void emptyNamedQualifierShouldBeTreatedAsNormalQualifier() throws PokeException {
        class Basket {
            @MyInject
            @Named
            private Food r;

            @MyInject
            private Food w;
        }

        registerByClass(Food.class, Rice.class);
        registerByClass(Food.class, Wheat.class);

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
    public void overridingClassNameRegisteringShouldWorkAsExpected() throws PokeException, ClassNotFoundException {
        class Basket {
            @MyInject
            @Named
            private Food r;

            @MyInject
            private Food w;
        }

        Basket basket = new Basket();

        registerByName(Food.class, Rice.class.getName());
        registerByName(Food.class, Wheat.class.getName());
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Rice.class);
        Assert.assertEquals(basket.w.getClass(), Wheat.class);

        boolean conflicted = false;
        try {
            registerByName(Food.class, Noodle.class.getName());
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        conflicted = false;
        try {
            registerByName(Food.class, Bread.class.getName());
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        component.unregister(new ProviderByClassName(Food.class, Chicken.class.getName()));
        component.unregister(new ProviderByClassName(Food.class, Bread.class.getName()));
        basket = new Basket();
        boolean shouldCatchProviderMissingException = false;
        try {
            graph.inject(basket, MyInject.class);
        } catch (ProviderMissingException e) {
            shouldCatchProviderMissingException = true;
        }
        Assert.assertTrue(shouldCatchProviderMissingException);
    }

    static class BadModule {
        @Provides
        void provideNothing() {
            return;
        }
    }

    @Test
    public void should_throw_exception_when_there_is_void_function_in_component()
            throws PokeException {
        BadModule badComponent = new BadModule();

        try {
            component.register(badComponent);
        } catch (ProvideException e) {
            Assert.assertTrue(e.getMessage().contains("must not return void"));
            return;
        }
        Assert.fail("Should raise ProvideException for provider returning void");
    }

    @Qualifier @Retention(RUNTIME) @interface Qualifier1 {}

    @Qualifier @Retention(RUNTIME) @interface Qualifier2 {}

    static class DuplicateModule {
        @Provides @Qualifier1 @Qualifier2
        String provideText() {
            return "123";
        }
    }

    @Test
    public void should_throw_exception_when_provider_has_more_than_one_qualifier()
            throws PokeException {
        DuplicateModule module = new DuplicateModule();
        try {
            component.register(module);
        } catch (ProvideException e) {
            Assert.assertTrue(e.getMessage().contains("Only one Qualifier"));
            return;
        }
        Assert.fail("Should raise ProvideException for provider with multiple qualifier");
    }

    @Test
    public void should_throw_exception_if_provider_returns_null()
            throws PokeException {
        Provider<String> provider = new Provider(String.class) {
            @Override
            protected String createInstance() throws ProvideException {
                return null;
            }
        };
        component.register(provider);

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
    public void overridingClassRegisteringShouldWorkAsExpected() throws PokeException {
        class Basket {
            @MyInject
            @Named
            private Food r;

            @MyInject
            private Food w;
        }

        Basket basket = new Basket();

        registerByClass(Food.class, Rice.class);
        registerByClass(Food.class, Wheat.class);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Rice.class);
        Assert.assertEquals(basket.w.getClass(), Wheat.class);

        boolean conflicted = false;
        try {
            registerByClass(Food.class, Noodle.class);
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        conflicted = false;
        try {
            registerByClass(Food.class, Bread.class);
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        component.unregister(new ProviderByClassType(Food.class, Noodle.class));
        component.unregister(new ProviderByClassType(Food.class, Bread.class));
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
                return ReflectUtils.findFirstQualifierInAnnotations(Rice.class);
            }
        };

        Provider<Food> providerWheat = new Provider<Food>(Food.class) {
            @Override
            protected Food createInstance() throws ProvideException {
                return new Wheat();
            }
            @Override
            public Annotation getQualifier() {
                return ReflectUtils.findFirstQualifierInAnnotations(Wheat.class);
            }
        };

        Provider<Food> providerNoodle = new Provider<Food>(Food.class) {
            @Override
            protected Food createInstance() throws ProvideException {
                return new Noodle();
            }
            @Override
            public Annotation getQualifier() {
                return ReflectUtils.findFirstQualifierInAnnotations(Noodle.class);
            }
        };

        Provider<Food> providerBread = new Provider<Food>(Food.class) {
            @Override
            protected Food createInstance() throws ProvideException {
                return new Bread();
            }
            @Override
            public Annotation getQualifier() {
                return ReflectUtils.findFirstQualifierInAnnotations(Bread.class);
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

        component.register(providerRice);
        component.register(providerWheat);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Rice.class);
        Assert.assertEquals(basket.w.getClass(), Wheat.class);

        boolean conflicted = false;
        try {
            component.register(providerNoodle);
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        conflicted = false;
        try {
            component.register(providerBread);
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        component.unregister(providerNoodle);
        component.unregister(providerWheat);
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

    static class FoodModuleA {
        @Provides @Named
        public Food provideApple() {
            return new Apple();
        }

        @Provides
        public Food provideOrange() {
            return new Orange();
        }
    }

    static class FoodModuleB {
        @Provides
        public Food provideApple() {
            return new Apple();
        }

        @Provides @Named
        public Food provideOrange() {
            return new Orange();
        }
    }

    static class FoodModuleC {
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
    public void overridingComponentRegisteringShouldWorkAsExpected() throws PokeException {
        class Basket {
            @MyInject
            @Named
            private Food r;

            @MyInject
            private Food w;
        }

        Basket basket = new Basket();

        FoodModuleA foodModuleA = new FoodModuleA();
        FoodModuleB foodModuleB = new FoodModuleB();
        FoodModuleC foodModuleC = new FoodModuleC();

        component.register(foodModuleA);
        graph.inject(basket, MyInject.class);
        Assert.assertEquals(basket.r.getClass(), Apple.class);
        Assert.assertEquals(basket.w.getClass(), Orange.class);

        boolean conflicted = false;
        try {
            component.register(new FoodModuleB());
        } catch (ProviderConflictException e) {
            conflicted = true;
        }
        Assert.assertTrue(conflicted);

        component.unregister(foodModuleA);

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

        Provider<Food> namedProviderBanana = new Provider<Food>(Food.class) {
            @Override
            protected Food createInstance() throws ProvideException {
                return banana;
            }

            @Override
            public Annotation getQualifier() {
                return ReflectUtils.findFirstQualifierInAnnotations(Noodle.class);
            }
        };

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

        Basket basket = new Basket();

        component.register(namedProviderBanana);
        component.register(unnammedProviderOrange);
        graph.inject(basket, MyInject.class);
        Assert.assertTrue(basket.r == banana);
        Assert.assertTrue(basket.w == orange);
        Assert.assertTrue(findCacheInstance(component.scopeCache, namedProviderBanana) == banana);
        Assert.assertTrue(findCacheInstance(component.scopeCache, unnammedProviderOrange) == orange);
    }

    @SuppressWarnings("unchecked")
    private <T> T findCacheInstance(ScopeCache scopeCache, Provider<T> provider) {
        T instance = scopeCache.findInstance(provider.type(), provider.getQualifier());
        if(instance != null) {
            return instance;
        }
        return null;
    }

}