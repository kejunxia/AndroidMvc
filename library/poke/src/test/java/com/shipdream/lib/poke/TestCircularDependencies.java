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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Singleton;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unchecked")
public class TestCircularDependencies extends BaseTestCases {
    private Component component;
    private Graph graph;

    @Before
    public void setUp() throws Exception {
        component = new Component("AppSingleton");
        graph = new Graph();
        graph.setRootComponent(component);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_not_throw_circular_dependency_exception_on_finite_circular_dependency() throws PokeException {
        Provider.ReferencedListener<Power> powerOnInject = mock(Provider.ReferencedListener.class);
        ProviderByClassType powerProvider = new ProviderByClassType<>(Power.class, PowerImpl.class);
        powerProvider.registerOnReferencedListener(powerOnInject);

        Provider.ReferencedListener<Driver> driverOnInject = mock(Provider.ReferencedListener.class);
        ProviderByClassType driverProvider = new ProviderByClassType(Driver.class, DriverImpl.class);
        driverProvider.registerOnReferencedListener(driverOnInject);

        Provider.ReferencedListener<Robot> robotOnInject = mock(Provider.ReferencedListener.class);
        ProviderByClassType robotProvider = new ProviderByClassType(Robot.class, RobotImpl.class);
        robotProvider.registerOnReferencedListener(robotOnInject);

        component.register(powerProvider);
        component.register(robotProvider);
        component.register(driverProvider);

        Factory factory = new Factory();
        graph.inject(factory, MyInject.class);

        Assert.assertNotNull(factory);

        PowerImpl power = (PowerImpl) factory.power;
        Assert.assertNotNull(power);

        RobotImpl robot = (RobotImpl) power.robot;
        Assert.assertNotNull(robot);

        DriverImpl driver = (DriverImpl) robot.driver;
        Assert.assertNotNull(driver);
    }

    static class RobotImpl2 implements Robot {
        @MyInject
        private Driver driver;

        @MyInject
        private Power power;
    }

    static class DriverImpl2 implements Driver {
        @MyInject
        private Power power;

        @MyInject
        private Robot robot;
    }

    static class PowerImpl2 implements Power {

        @MyInject
        private Robot robot;

        @MyInject
        private Driver driver;

        public Robot getConnectedRobot() {
            return robot;
        }
    }

    @Test(expected = CircularDependenciesException.class)
    public void should_detect_infinite_circular_dependencies() throws PokeException {
        //Create a new unscoped component
        Component c = new Component(null, false);
        graph.setRootComponent(c);
        c.register(new ProviderByClassType(Power.class, PowerImpl2.class));
        c.register(new ProviderByClassType(Driver.class, DriverImpl2.class));
        c.register(new ProviderByClassType(Robot.class, RobotImpl2.class));

        Factory factory = new Factory();
        graph.inject(factory, MyInject.class);
    }

    @Test
    public void shouldNotifyReferencedCallbackWhenObjectFullyInjectedWithCircularDependencies() throws PokeException {
        final Factory factory = new Factory();

        ProviderByClassType powerProvider = new ProviderByClassType(Power.class, PowerImpl.class);
        powerProvider.registerOnReferencedListener(new Provider.ReferencedListener<Power>() {
            @Override
            public void onReferenced(Provider<Power> provider, Power instance) {
                Assert.assertNotNull(((PowerImpl) instance).robot);
            }
        });

        Assert.assertEquals(1, powerProvider.getReferencedListeners().size());

        ProviderByClassType driverProvider = new ProviderByClassType(Driver.class, DriverImpl.class);
        driverProvider.registerOnReferencedListener(new Provider.ReferencedListener<Driver>() {
            @Override
            public void onReferenced(Provider<Driver> provider, Driver instance) {
                Assert.assertNotNull(((DriverImpl) instance).power);
            }
        });

        ProviderByClassType robotProvider = new ProviderByClassType(Robot.class, RobotImpl.class);
        robotProvider.registerOnReferencedListener(new Provider.ReferencedListener<Robot>() {
            @Override
            public void onReferenced(Provider<Robot> provider, Robot instance) {
                Assert.assertNotNull(((RobotImpl) instance).driver);
            }
        });

        component.register(powerProvider);
        component.register(robotProvider);
        component.register(driverProvider);

        graph.inject(factory, MyInject.class);

        powerProvider.clearOnReferencedListener();
        Assert.assertEquals(null, powerProvider.getReferencedListeners());
    }

    @Test
    public void shouldNotifyCreatedCallbackWhenObjectFullyInjectedWithCircularDependencies() throws PokeException {
        class Plant {
            @MyInject
            private Power power;
        }

        Provider powerProvider = new Provider<Power>(Power.class, new ScopeCache()) {
            @Override
            protected Power createInstance() throws ProvideException {
                return new Power() {
                };
            }
        };
        component.register(powerProvider);

        Provider.CreationListener creationListener = mock(Provider.CreationListener.class);
        powerProvider.registerCreationListener(creationListener);
        Assert.assertEquals(1, powerProvider.getCreationListeners().size());

        Plant plant = new Plant();
        graph.getRootComponent().scopeCache = null;
        graph.inject(plant, MyInject.class);

        verify(creationListener, times(1)).onCreated(eq(powerProvider), eq(plant.power));

        powerProvider.unregisterCreationListener(creationListener);
        Assert.assertEquals(null, powerProvider.getCreationListeners());
        graph.inject(plant, MyInject.class);
        verify(creationListener, times(1)).onCreated(eq(powerProvider), any(Power.class));

        powerProvider.registerCreationListener(creationListener);
        graph.inject(plant, MyInject.class);
        verify(creationListener, times(2)).onCreated(eq(powerProvider), any(Power.class));

        powerProvider.clearCreationListeners();
        graph.inject(plant, MyInject.class);
        verify(creationListener, times(2)).onCreated(eq(powerProvider), any(Power.class));
    }

    @Test
    public void component_cache_should_override_provider_cache() throws PokeException {
        class Plant {
            @MyInject
            private Power power;
        }

        ScopeCache scopeCache = new ScopeCache();
        Provider powerProvider = new Provider<Power>(Power.class, scopeCache) {
            @Override
            protected Power createInstance() throws ProvideException {
                return new Power() {
                };
            }
        };

        Assert.assertTrue(scopeCache == powerProvider.getScopeCache());

        component.register(powerProvider);

        Assert.assertTrue(scopeCache != powerProvider.getScopeCache());
        Assert.assertTrue(powerProvider.getScopeCache() == component.scopeCache);
    }

    @Test
    public void shouldInjectObjectOnlyOnceWithCircularDependencies() throws PokeException, NoSuchFieldException {
        final Factory factory = new Factory();

        final Provider.ReferencedListener<Power> powerOnInject = mock(Provider.ReferencedListener.class);
        final ProviderByClassType<Power> powerProvider = new ProviderByClassType(Power.class, PowerImpl.class);
        powerProvider.registerOnReferencedListener(new Provider.ReferencedListener<Power>() {
            @Override
            public void onReferenced(Provider<Power> provider, Power instance) {
                if (provider.getReferenceCount() == 1) {
                    powerOnInject.onReferenced(provider, instance);
                }
            }
        });

        final Provider.ReferencedListener<Driver> driverOnInject = mock(Provider.ReferencedListener.class);
        ProviderByClassType<Driver> driverProvider = new ProviderByClassType(Driver.class, DriverImpl.class);
        driverProvider.registerOnReferencedListener(new Provider.ReferencedListener<Driver>() {
            @Override
            public void onReferenced(Provider<Driver> provider, Driver instance) {
                if (provider.getReferenceCount() == 1) {
                    driverOnInject.onReferenced(provider, instance);
                }
            }
        });

        final Provider.ReferencedListener<Robot> robotOnInject = mock(Provider.ReferencedListener.class);
        ProviderByClassType<Robot> robotProvider = new ProviderByClassType(Robot.class, RobotImpl.class);
        robotProvider.registerOnReferencedListener(new Provider.ReferencedListener<Robot>() {
            @Override
            public void onReferenced(Provider<Robot> provider, Robot instance) {
                if (provider.getReferenceCount() == 1) {
                    robotOnInject.onReferenced(provider, instance);
                }
            }
        });

        component.register(powerProvider);
        component.register(robotProvider);
        component.register(driverProvider);

        graph.inject(factory, MyInject.class);

        verify(powerOnInject, times(1)).onReferenced(eq(powerProvider), any(Power.class));
        verify(driverOnInject, times(1)).onReferenced(eq(driverProvider), any(Driver.class));
        verify(robotOnInject, times(1)).onReferenced(eq(robotProvider), any(Robot.class));
    }

    @Test
    public void test_should_inject_and_release_correctly_on_single_object() throws PokeException {
        prepareInjection();

        final Factory factory = new Factory();

        graph.inject(factory, MyInject.class);
        Assert.assertFalse(component.scopeCache.getCachedInstances().isEmpty());
        graph.release(factory, MyInject.class);
        Assert.assertTrue(component.scopeCache.getCachedInstances().isEmpty());
    }

    @Test
    public void test_should_inject_and_release_correctly_on_multiple_objects() throws PokeException {
        final ScopeCache scopeCache = new ScopeCache();
        prepareInjection();

        final Factory factory1 = new Factory();
        final Factory factory2 = new Factory();

        graph.inject(factory1, MyInject.class);

        Provider<Power> powerProvider = component.findProvider(Power.class, null);
        Provider<Driver> driverProvider = component.findProvider(Driver.class, null);
        Provider<Robot> robotProvider = component.findProvider(Robot.class, null);

        Assert.assertFalse(component.scopeCache.getCachedInstances().isEmpty());

        graph.inject(factory2, MyInject.class);
        Assert.assertFalse(component.scopeCache.getCachedInstances().isEmpty());

        graph.release(factory2, MyInject.class);
        Assert.assertFalse(component.scopeCache.getCachedInstances().isEmpty());

        graph.release(factory1, MyInject.class);
        Assert.assertTrue(component.scopeCache.getCachedInstances().isEmpty());

        Assert.assertTrue(powerProvider.owners.isEmpty());
        Assert.assertEquals(0, powerProvider.getReferenceCount());

        Assert.assertTrue(driverProvider.owners.isEmpty());
        Assert.assertEquals(0, driverProvider.getReferenceCount());

        Assert.assertTrue(robotProvider.owners.isEmpty());
        Assert.assertEquals(0, robotProvider.getReferenceCount());
    }

    @Test
    public void test_should_inject_and_release_correctly_even_with_same_cached_objects_multiple_times()
            throws PokeException {
        prepareInjection();

        final Factory factory = new Factory();
        graph.inject(factory, MyInject.class);

        Provider<Power> powerProvider = component.findProvider(Power.class, null);
        Provider<Driver> driverProvider = component.findProvider(Driver.class, null);
        Provider<Robot> robotProvider = component.findProvider(Robot.class, null);

        Assert.assertFalse(component.scopeCache.getCachedInstances().isEmpty());

        graph.inject(factory, MyInject.class);
        Assert.assertFalse(component.scopeCache.getCachedInstances().isEmpty());

        graph.release(factory, MyInject.class);
        Assert.assertFalse(component.scopeCache.getCachedInstances().isEmpty());

        graph.release(factory, MyInject.class);
        Assert.assertTrue(component.scopeCache.getCachedInstances().isEmpty());

        Assert.assertTrue(powerProvider.owners.isEmpty());
        Assert.assertEquals(0, powerProvider.getReferenceCount());

        Assert.assertTrue(driverProvider.owners.isEmpty());
        Assert.assertEquals(0, driverProvider.getReferenceCount());

        Assert.assertTrue(robotProvider.owners.isEmpty());
        Assert.assertEquals(0, robotProvider.getReferenceCount());
    }

    @Test
    public void test_should_inject_and_release_correctly_on_multiple_objects_even_with_same_cached_objects_multiple_times()
            throws PokeException {
        prepareInjection();

        final Factory factory1 = new Factory();
        graph.inject(factory1, MyInject.class);

        Provider<Power> powerProvider = component.findProvider(Power.class, null);
        Provider<Driver> driverProvider = component.findProvider(Driver.class, null);
        Provider<Robot> robotProvider = component.findProvider(Robot.class, null);

        Assert.assertFalse(component.scopeCache.getCachedInstances().isEmpty());

        final Factory factory2 = new Factory();
        graph.inject(factory2, MyInject.class);
        Assert.assertFalse(component.scopeCache.getCachedInstances().isEmpty());

        graph.inject(factory1, MyInject.class);
        Assert.assertFalse(component.scopeCache.getCachedInstances().isEmpty());

        Assert.assertNotNull(factory1);
        Assert.assertNotNull(factory1.power);
        Assert.assertNotNull(factory1.driver);
        Assert.assertNotNull(((PowerImpl) factory1.power).driver);
        Assert.assertNotNull(((PowerImpl) factory1.power).robot);
        Assert.assertNotNull(((DriverImpl) factory1.driver).power);
        Assert.assertNotNull(((DriverImpl) factory1.driver).robot);
        Assert.assertNotNull(((RobotImpl) ((DriverImpl) factory1.driver).robot).driver);
        Assert.assertNotNull(((RobotImpl) ((DriverImpl) factory1.driver).robot).power);

        Assert.assertNotNull(factory2);
        Assert.assertNotNull(factory2.power);
        Assert.assertNotNull(factory2.driver);
        Assert.assertNotNull(((PowerImpl)factory2.power).driver);
        Assert.assertNotNull(((PowerImpl)factory2.power).robot);
        Assert.assertNotNull(((DriverImpl)factory2.driver).power);
        Assert.assertNotNull(((DriverImpl)factory2.driver).robot);
        Assert.assertNotNull(((RobotImpl)((DriverImpl) factory2.driver).robot).driver);
        Assert.assertNotNull(((RobotImpl)((DriverImpl) factory2.driver).robot).power);

        graph.release(factory1, MyInject.class);
        Assert.assertFalse(component.scopeCache.getCachedInstances().isEmpty());

        Assert.assertNotNull(factory1);
        Assert.assertNotNull(factory1.power);
        Assert.assertNotNull(factory1.driver);
        Assert.assertNotNull(((PowerImpl) factory1.power).driver);
        Assert.assertNotNull(((PowerImpl) factory1.power).robot);
        Assert.assertNotNull(((DriverImpl) factory1.driver).power);
        Assert.assertNotNull(((DriverImpl) factory1.driver).robot);
        Assert.assertNotNull(((RobotImpl) ((DriverImpl) factory1.driver).robot).driver);
        Assert.assertNotNull(((RobotImpl) ((DriverImpl) factory1.driver).robot).power);

        Assert.assertNotNull(factory2);
        Assert.assertNotNull(factory2.power);
        Assert.assertNotNull(factory2.driver);
        Assert.assertNotNull(((PowerImpl)factory2.power).driver);
        Assert.assertNotNull(((PowerImpl)factory2.power).robot);
        Assert.assertNotNull(((DriverImpl)factory2.driver).power);
        Assert.assertNotNull(((DriverImpl)factory2.driver).robot);
        Assert.assertNotNull(((RobotImpl)((DriverImpl) factory2.driver).robot).driver);
        Assert.assertNotNull(((RobotImpl)((DriverImpl) factory2.driver).robot).power);

        graph.release(factory1, MyInject.class);
        Assert.assertFalse(component.scopeCache.getCachedInstances().isEmpty());

        Assert.assertNotNull(factory1);
        Assert.assertNotNull(factory1.power);
        Assert.assertNotNull(factory1.driver);

        Assert.assertNotNull(factory2);
        Assert.assertNotNull(factory2.power);
        Assert.assertNotNull(factory2.driver);
        Assert.assertNotNull(((PowerImpl)factory2.power).driver);
        Assert.assertNotNull(((PowerImpl)factory2.power).robot);
        Assert.assertNotNull(((DriverImpl)factory2.driver).power);
        Assert.assertNotNull(((DriverImpl)factory2.driver).robot);
        Assert.assertNotNull(((RobotImpl)((DriverImpl) factory2.driver).robot).driver);
        Assert.assertNotNull(((RobotImpl)((DriverImpl) factory2.driver).robot).power);

        graph.release(factory1, MyInject.class);
        Assert.assertFalse(component.scopeCache.getCachedInstances().isEmpty());

        Assert.assertNotNull(factory1);
        Assert.assertNotNull(factory1.power);
        Assert.assertNotNull(factory1.driver);

        Assert.assertNotNull(factory2);
        Assert.assertNotNull(factory2.power);
        Assert.assertNotNull(factory2.driver);
        Assert.assertNotNull(((PowerImpl)factory2.power).driver);
        Assert.assertNotNull(((PowerImpl)factory2.power).robot);
        Assert.assertNotNull(((DriverImpl)factory2.driver).power);
        Assert.assertNotNull(((DriverImpl)factory2.driver).robot);
        Assert.assertNotNull(((RobotImpl)((DriverImpl) factory2.driver).robot).driver);
        Assert.assertNotNull(((RobotImpl)((DriverImpl) factory2.driver).robot).power);

        graph.release(factory2, MyInject.class);
        Assert.assertTrue(component.scopeCache.getCachedInstances().isEmpty());

        Assert.assertTrue(powerProvider.owners.isEmpty());
        Assert.assertEquals(0, powerProvider.getReferenceCount());

        Assert.assertTrue(driverProvider.owners.isEmpty());
        Assert.assertEquals(0, driverProvider.getReferenceCount());

        Assert.assertTrue(robotProvider.owners.isEmpty());
        Assert.assertEquals(0, robotProvider.getReferenceCount());
    }

    private void prepareInjection() throws PokeException {
        ProviderByClassType powerProvider = new ProviderByClassType(Power.class, PowerImpl.class);

        ProviderByClassType driverProvider = new ProviderByClassType(Driver.class, DriverImpl.class);

        ProviderByClassType robotProvider = new ProviderByClassType(Robot.class, RobotImpl.class);

        component.register(powerProvider);
        component.register(robotProvider);
        component.register(driverProvider);
    }

    @Singleton
    static class Factory {
        @MyInject
        private Power power;

        @MyInject
        private Driver driver;
    }

    @Singleton
    static class RobotImpl implements Robot {
        @MyInject
        private Driver driver;

        @MyInject
        private Power power;
    }

    @Singleton
    static class DriverImpl implements Driver {
        @MyInject
        private Power power;

        @MyInject
        private Robot robot;
    }

    @Singleton
    static class PowerImpl implements Power {

        @MyInject
        private Robot robot;

        @MyInject
        private Driver driver;

        public Robot getConnectedRobot() {
            return robot;
        }
    }

    interface Robot {
    }

    interface Driver {
    }

    interface Power {
    }
}
