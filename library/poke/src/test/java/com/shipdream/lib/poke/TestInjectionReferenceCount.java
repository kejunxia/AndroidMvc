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
import com.shipdream.lib.poke.Provider.DereferenceListener;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Singleton;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unchecked")
public class TestInjectionReferenceCount extends BaseTestCases {
    interface Fruit {}

    static class Apple implements Fruit {
    }

    interface Container {}

    static class Fridge implements Container {
        @MyInject
        private Fruit a;

        @MyInject
        private Fruit b;
    }

    static class House {
        @MyInject
        private Container container;
    }

    private Graph graph;
    private Component component;

    @Before
    public void setUp() throws Exception {
        graph = new Graph();
        component = new Component("AppSingleton");
        component.register(new TestModule());
        graph.setRootComponent(component);
    }

    @Test
    public void unscopedProvidesShouldHaveDifferentInstances() throws PokeException {

        class Module1 {
            @Provides
            public Fruit providesFruit() {
                return new Apple();
            }

            @Provides
            public Container providesContainer() {
                return new Fridge();
            }
        }

        Graph g = new Graph();
        Component c;
        c = new Component(false);
        c.register(new Module1());
        g.setRootComponent(c);

        House house = new House();
        g.inject(house, MyInject.class);

        Assert.assertNotNull(house.container);
        Assert.assertNotNull(((Fridge) house.container).a);
        Assert.assertNotNull(((Fridge) house.container).b);
        Assert.assertTrue(((Fridge) house.container).a != ((Fridge) house.container).b);
    }

    static class TestModule {
        @Singleton
        @Provides
        public Fruit providesFruit() {
            return new Apple();
        }

        @Singleton
        @Provides
        public Container providesContainer() {
            return new Fridge();
        }
    }

    @Test
    public void referenceCountCanReduceCascadinglyFromRoot() throws ProvideException, ProviderConflictException,
            CircularDependenciesException, ProviderMissingException {
        House house = new House();
        graph.inject(house, MyInject.class);

        Assert.assertNotNull(house.container);
        Assert.assertNotNull(((Fridge) house.container).a);
        Assert.assertNotNull(((Fridge) house.container).b);
        Assert.assertTrue(((Fridge) house.container).a == ((Fridge) house.container).b);

        Provider<Container> containerProvider = component.findProvider(Container.class, null);
        Provider<Fruit> fruitProvider = component.findProvider(Fruit.class, null);

        Assert.assertTrue(containerProvider.getReferenceCount() == 1);
        Assert.assertTrue(fruitProvider.getReferenceCount() == 2);

        graph.release(house, MyInject.class);
        Assert.assertTrue(component.scopeCache.getCachedInstances().isEmpty());
    }

    static class Mansion extends House {
        @MyInject
        private Container container;
    }

    @Test
    public void should_be_able_to_release_inherited_fields_with_same_name() throws ProvideException, ProviderConflictException,
            CircularDependenciesException, ProviderMissingException {
        Mansion mansion = new Mansion();
        graph.inject(mansion, MyInject.class);

        Assert.assertNotNull(mansion.container);
        Assert.assertNotNull(((Fridge) mansion.container).a);
        Assert.assertNotNull(((Fridge) mansion.container).b);
        Assert.assertTrue(((Fridge) mansion.container).a == ((Fridge) mansion.container).b);

        Provider<Container> containerProvider = component.findProvider(Container.class, null);
        Provider<Fruit> fruitProvider = component.findProvider(Fruit.class, null);

        //container has been referenced twice by the fields Mansion.container and Mansion.House.container
        Assert.assertEquals(2, containerProvider.getReferenceCount());
        Assert.assertEquals(4, fruitProvider.getReferenceCount());

        graph.release(mansion, MyInject.class);
        Assert.assertTrue(component.scopeCache.getCachedInstances().isEmpty());
    }

    static class Kitchen {
        @MyInject
        private Container container;

        @MyInject
        private Fruit aOnFloor;

        @MyInject
        private Fruit bOnFloor;
    }

    @Test
    public void referenceCountCanReduceCascadinglyFromSubNode() throws ProvideException, ProviderConflictException,
            CircularDependenciesException, ProviderMissingException {
        Kitchen kitchen = new Kitchen();
        graph.inject(kitchen, MyInject.class);

        Fridge fridge = (Fridge) kitchen.container;

        Assert.assertNotNull(kitchen.container);
        Assert.assertNotNull(fridge.a);
        Assert.assertNotNull(fridge.b);
        Assert.assertTrue((fridge).a == fridge.b);
        Assert.assertTrue(kitchen.aOnFloor == kitchen.bOnFloor);

        Provider<Container> containerProvider = component.findProvider(Container.class, null);
        Provider<Fruit> fruitProvider = component.findProvider(Fruit.class, null);

        Assert.assertEquals(1, containerProvider.getReferenceCount());
        Assert.assertEquals(4, fruitProvider.getReferenceCount());

        graph.release(kitchen.container, MyInject.class);
        Assert.assertNotNull(kitchen.container);
        Assert.assertNotNull(kitchen.aOnFloor);
        Assert.assertNotNull(kitchen.bOnFloor);
        Assert.assertNotNull(fridge.a);
        Assert.assertNotNull(fridge.b);
        Assert.assertEquals(1, containerProvider.getReferenceCount());
        Assert.assertEquals(2, fruitProvider.getReferenceCount());

        graph.release(kitchen.container, MyInject.class);
        Assert.assertNotNull(fridge.a);
        Assert.assertNotNull(fridge.b);
        Assert.assertEquals(1, containerProvider.getReferenceCount());
        Assert.assertEquals(2, fruitProvider.getReferenceCount());
    }

    @Test
    public void releaseInjectedFieldsShouldSetThemNull() throws ProvideException, ProviderConflictException,
            CircularDependenciesException, ProviderMissingException {

        Kitchen kitchen = new Kitchen();
        graph.inject(kitchen, MyInject.class);

        Provider<Container> containerProvider = component.findProvider(Container.class, null);
        Provider<Fruit> fruitProvider = component.findProvider(Fruit.class, null);

        graph.release(kitchen.container, MyInject.class);
        Assert.assertEquals(1, containerProvider.getReferenceCount());
        Assert.assertEquals(2, fruitProvider.getReferenceCount());

        graph.release(kitchen.container, MyInject.class);

        Assert.assertNotNull(kitchen.aOnFloor);
        Assert.assertNotNull(kitchen.bOnFloor);

        Assert.assertTrue(containerProvider.getReferenceCount() == 1);
        Assert.assertTrue(fruitProvider.getReferenceCount() == 2);

        graph.release(kitchen, MyInject.class);

        Assert.assertTrue(component.scopeCache.getCachedInstances().isEmpty());
    }

    @Test
    public void should_invoke_on_freed_callback_when_providers_are_freed_from_child_node() throws ProvideException,
            ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        //As mockito can't mock annotation so we need a proxy to stub it
        class OnCacheFreedProxy {
            public void onFreed(Class<?> type) {}
        }

        Kitchen kitchen = new Kitchen();
        graph.inject(kitchen, MyInject.class);

        final OnCacheFreedProxy proxy = mock(OnCacheFreedProxy.class);
        Provider.DereferenceListener onFreed = new DereferenceListener() {
            @Override
            public <T> void onDereferenced(Provider<T> provider, T instance) {
                if (provider.getReferenceCount() == 0) {
                    proxy.onFreed(provider.type());
                }
            }
        };
        graph.registerDereferencedListener(onFreed);

        //Assert

        //Releasing fruits are still held by kitchen, so no callback should be seen, though
        //reference count should be decreased
        graph.release(kitchen.container, MyInject.class);
        verify(proxy, times(0)).onFreed(eq(Container.class));
        verify(proxy, times(0)).onFreed(eq(Fruit.class));

        //Releasing fruits are still held by kitchen, so no callback should be seen.
        graph.release(kitchen.container, MyInject.class);
        verify(proxy, times(0)).onFreed(eq(Container.class));
        verify(proxy, times(0)).onFreed(eq(Fruit.class));

        graph.release(kitchen, MyInject.class);
        verify(proxy, times(1)).onFreed(eq(Container.class));
        verify(proxy, times(1)).onFreed(eq(Fruit.class));
    }

    @Test
    public void should_invoke_on_freed_callback_when_providers_are_freed_from_root_node() throws ProvideException,
            ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        //As mockito can't mock annotation so we need a proxy to stub it
        class OnCacheFreedProxy {
            public void onFreed(Class<?> type) {}
        }

        Kitchen kitchen = new Kitchen();
        graph.inject(kitchen, MyInject.class);

        final OnCacheFreedProxy proxy = mock(OnCacheFreedProxy.class);
        DereferenceListener onFreed = new Provider.DereferenceListener() {
            @Override
            public <T> void onDereferenced(Provider<T> provider, T instance) {
                if (provider.getReferenceCount() == 0) {
                    proxy.onFreed(provider.type());
                }
            }
        };
        graph.registerDereferencedListener(onFreed);

        //Assert
        //Releasing root should free providers and invoke callbacks
        graph.release(kitchen, MyInject.class);
        verify(proxy, times(1)).onFreed(eq(Container.class));
        verify(proxy, times(1)).onFreed(eq(Fruit.class));
    }

    @Test
    public void should_not_invoke_on_freed_callback_when_freedListeners_are_unregistered()
            throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        //As mockito can't mock annotation so we need a proxy to stub it
        class OnCacheFreedProxy {
            public void onFreed(Class<?> type) {}
        }

        Kitchen kitchen = new Kitchen();
        graph.inject(kitchen, MyInject.class);

        final OnCacheFreedProxy proxy = mock(OnCacheFreedProxy.class);
        DereferenceListener onFreed = new Provider.DereferenceListener() {
            @Override
            public <T> void onDereferenced(Provider<T> provider, T instance) {
                if (provider.getReferenceCount() == 0) {
                    proxy.onFreed(provider.type());
                }
            }
        };
        graph.registerDereferencedListener(onFreed);

        graph.unregisterDereferencedListener(onFreed);

        //Assert
        graph.release(kitchen, MyInject.class);
        verify(proxy, times(0)).onFreed(eq(Container.class));
        verify(proxy, times(0)).onFreed(eq(Fruit.class));
    }

    @Test
    public void should_not_invoke_on_freed_callback_when_freedListeners_are_cleared()
            throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        //As mockito can't mock annotation so we need a proxy to stub it
        class OnCacheFreedProxy {
            public void onFreed(Class<?> type) {}
        }

        Kitchen kitchen = new Kitchen();
        graph.inject(kitchen, MyInject.class);

        final OnCacheFreedProxy proxy = mock(OnCacheFreedProxy.class);
        DereferenceListener onFreed = new Provider.DereferenceListener() {
            @Override
            public <T> void onDereferenced(Provider<T> provider, T instance) {
                if (provider.getReferenceCount() == 0) {
                    proxy.onFreed(provider.type());
                }
            }
        };
        graph.registerDereferencedListener(onFreed);

        graph.clearDereferencedListeners();

        //Assert
        graph.release(kitchen, MyInject.class);
        verify(proxy, times(0)).onFreed(eq(Container.class));
        verify(proxy, times(0)).onFreed(eq(Fruit.class));
    }

    @Test
    public void should_invoke_call_backs_of_registered_graph_monitors()
            throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        //As mockito can't mock annotation so we need a proxy to stub it
        class MonitorProxy {
            public void onInject(Object target) {}
            public void onRelease(Object target) {}
        }

        final MonitorProxy proxy = mock(MonitorProxy.class);
        Graph.Monitor monitor = new Graph.Monitor() {
            @Override
            public void onInject(Object intoTarget) {
                proxy.onInject(intoTarget);
            }

            @Override
            public void onRelease(Object fromTarget) {
                proxy.onRelease(fromTarget);
            }
        };
        graph.registerMonitor(monitor);

        Kitchen kitchen = new Kitchen();

        //Act
        graph.inject(kitchen, MyInject.class);

        //Assert
        verify(proxy, times(1)).onInject(kitchen);

        reset(proxy);
        //Act
        graph.release(kitchen, MyInject.class);
        //Assert
        verify(proxy, times(1)).onRelease(kitchen);
    }

    @Test
    public void should_not_invoke_call_backs_of_unregistered_graph_monitors()
            throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        //As mockito can't mock annotation so we need a proxy to stub it
        class MonitorProxy {
            public void onInject(Object target) {}
            public void onRelease(Object target) {}
        }

        final MonitorProxy proxy = mock(MonitorProxy.class);
        Graph.Monitor monitor = new Graph.Monitor() {
            @Override
            public void onInject(Object intoTarget) {
                proxy.onInject(intoTarget);
            }

            @Override
            public void onRelease(Object fromTarget) {
                proxy.onRelease(fromTarget);
            }
        };
        graph.registerMonitor(monitor);

        Kitchen kitchen = new Kitchen();

        graph.unregisterMonitor(monitor);

        //Act
        graph.inject(kitchen, MyInject.class);

        //Assert
        verify(proxy, times(0)).onInject(kitchen);

        reset(proxy);
        //Act
        graph.release(kitchen, MyInject.class);
        //Assert
        verify(proxy, times(0)).onRelease(kitchen);
    }

    @Test
    public void should_not_invoke_call_backs_after_clear_graph_monitors()
            throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        //As mockito can't mock annotation so we need a proxy to stub it
        class MonitorProxy {
            public void onInject(Object target) {}
            public void onRelease(Object target) {}
        }

        final MonitorProxy proxy = mock(MonitorProxy.class);
        Graph.Monitor monitor = new Graph.Monitor() {
            @Override
            public void onInject(Object intoTarget) {
                proxy.onInject(intoTarget);
            }

            @Override
            public void onRelease(Object fromTarget) {
                proxy.onRelease(fromTarget);
            }
        };
        graph.registerMonitor(monitor);

        Kitchen kitchen = new Kitchen();

        graph.clearMonitors();

        //Act
        graph.inject(kitchen, MyInject.class);

        //Assert
        verify(proxy, times(0)).onInject(kitchen);

        reset(proxy);
        //Act
        graph.release(kitchen, MyInject.class);
        //Assert
        verify(proxy, times(0)).onRelease(kitchen);
    }

    static class Orange implements Fruit {
        @MyInject
        private Container container;
    }

    static class TestComp3 {
        @Singleton
        @Provides
        public Fruit providesFruit() {
            return new Orange();
        }

        @Singleton
        @Provides
        public Container providesContainer() {
            return new Fridge();
        }
    }

    @Test
    public void should_hold_provider_until_no_objects_is_referencing_to_it()
            throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {

        Kitchen kitchen1 = new Kitchen();
        graph.inject(kitchen1, MyInject.class);
        Fridge fridge1 = (Fridge) kitchen1.container;

        Kitchen kitchen2 = new Kitchen();
        graph.inject(kitchen2, MyInject.class);
        Fridge fridge2 = (Fridge) kitchen2.container;

        Assert.assertEquals(fridge1, fridge2);

        Assert.assertNotNull(kitchen1);
        Assert.assertNotNull(kitchen1.container);
        Assert.assertNotNull(kitchen1.aOnFloor);
        Assert.assertNotNull(kitchen1.bOnFloor);
        Assert.assertNotNull(fridge1.a);
        Assert.assertNotNull(fridge1.b);

        Assert.assertNotNull(kitchen2);
        Assert.assertNotNull(kitchen2.container);
        Assert.assertNotNull(kitchen2.aOnFloor);
        Assert.assertNotNull(kitchen2.bOnFloor);
        Assert.assertNotNull(fridge2.a);
        Assert.assertNotNull(fridge2.b);

        graph.release(kitchen1.container, MyInject.class);

        Assert.assertNotNull(kitchen1.container);
        Assert.assertNotNull(kitchen1.aOnFloor);
        Assert.assertNotNull(kitchen1.bOnFloor);
        Assert.assertNotNull(fridge2.a);
        Assert.assertNotNull(fridge2.b);
        Assert.assertNotNull(fridge1.a);
        Assert.assertNotNull(fridge1.b);

        Assert.assertNotNull(kitchen2.container);
        Assert.assertNotNull(kitchen2.aOnFloor);
        Assert.assertNotNull(kitchen2.bOnFloor);
        Assert.assertNotNull(fridge2.a);
        Assert.assertNotNull(fridge2.b);
    }

}
