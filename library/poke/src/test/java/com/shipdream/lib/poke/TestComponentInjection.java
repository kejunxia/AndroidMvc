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

import java.lang.annotation.Retention;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class TestComponentInjection extends BaseTestCases {
    private interface Food {

    }

    private static class Apple implements Food {

    }

    private static class Milk implements Food {

    }

    @Test(expected = ProviderConflictException.class)
    public void shouldDetectConflictProvides() throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        Component component = new Component() {
            @Provides
            public Food providesFood1() {
                return new Apple();
            }

            @Provides
            public Food providesFood2() {
                return new Milk();
            }
        };

        SimpleGraph graph = new SimpleGraph();
        graph.register(component);
    }

    @Qualifier
    @Retention(RUNTIME)
    @interface Fruit {
    }

    @Qualifier
    @Retention(RUNTIME)
    @interface Drink {
    }

    @Test
    public void componentMethodInjectionShouldWork() throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        class Bread implements Food{

        }

        Component component = new Component() {
            @Fruit
            @Provides
            public Food providesFood1() {
                return new Apple();
            }

            @Drink
            @Provides
            public Food providesFood2() {
                return new Milk();
            }

            @Provides
            public Food providesFood3() {
                return new Bread();
            }
        };

        class Fridge {
            @Fruit
            @MyInject
            private Food fruit;

            @MyInject
            private Food bread;

            @Drink
            @MyInject
            private Food drink;
        }

        SimpleGraph graph = new SimpleGraph();
        graph.register(component);

        Fridge fridge = new Fridge();
        graph.inject(fridge, MyInject.class);

        Assert.assertEquals(fridge.fruit.getClass(), Apple.class);
        Assert.assertEquals(fridge.drink.getClass(), Milk.class);
        Assert.assertEquals(fridge.bread.getClass(), Bread.class);
    }

    @Test
    public void componentMethodInjectionShouldWork1() throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        Component component = new Component() {
            @Named("Fruit")
            @Provides
            public Food providesFood1() {
                return new Apple();
            }

            @Named("Drink")
            @Provides
            public Food providesFood2() {
                return new Milk();
            }

            @Named("Untagged")
            @Provides
            public Food providesFood3() {
                return new Milk();
            }
        };

        class Fridge {
            @Named("Fruit")
            @MyInject
            private Food fruit;

            @Named("Untagged")
            @MyInject
            private Food drink2;

            @Named("Drink")
            @MyInject
            private Food drink;
        }

        SimpleGraph graph = new SimpleGraph();
        graph.register(component);

        Fridge fridge = new Fridge();
        graph.inject(fridge, MyInject.class);

        Assert.assertEquals(fridge.fruit.getClass(), Apple.class);
        Assert.assertEquals(fridge.drink.getClass(), Milk.class);
        Assert.assertEquals(fridge.drink2.getClass(), Milk.class);
    }

    static class SingletonComponent extends Component{
        SingletonComponent(ScopeCache scopeCache) {
            super(scopeCache);
        }

        @Singleton
        @Provides
        public Food providesFood() {
            return new Apple();
        }
    }

    @Test
    public void singletonComponentShouldProvideSameInstanceStically()
            throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        ScopeCache singletonCache = new ScopeCache();

        Component component1 = new SingletonComponent(singletonCache);
        Component component2 = new SingletonComponent(singletonCache);

        SimpleGraph graph = new SimpleGraph();
        graph.register(component1);

        SimpleGraph graph2 = new SimpleGraph();
        graph2.register(component2);

        class Fridge {
            @MyInject
            private Food fruit;
        }

        Fridge fridge = new Fridge();
        graph.inject(fridge, MyInject.class);

        Fridge fridge1 = new Fridge();
        graph2.inject(fridge1, MyInject.class);

        Assert.assertNotNull(fridge.fruit);
        Assert.assertTrue(fridge.fruit == fridge1.fruit);
    }

    static class BadComponent extends Component{
        @Provides
        public Food providesFood() {
            return null;
        }
    }

    @Test (expected = ProvideException.class)
    public void should_detect_ProvideException_with_providers_return_void ()
            throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        SimpleGraph graph = new SimpleGraph();
        graph.register(new BadComponent());

        class Fridge {
            @MyInject
            private Food myFood;
        }

        Fridge fridge = new Fridge();
        graph.inject(fridge, MyInject.class);
    }
}
