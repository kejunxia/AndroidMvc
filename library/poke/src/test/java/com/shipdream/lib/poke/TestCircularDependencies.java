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

import java.lang.annotation.Annotation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unchecked")
public class TestCircularDependencies extends BaseTestCases {
    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotDetectCircularDependencies() throws ProvideException, CircularDependenciesException, ProviderMissingException, ProviderConflictException {
        SimpleGraph graph = new SimpleGraph();
        ScopeCache scopeCache = new ScopeCache();

        Provider.OnInjectedListener<Power> powerOnInject = mock(Provider.OnInjectedListener.class);
        ProviderByClassName powerProvider = new ProviderByClassName(Power.class, PowerImpl.class);
        powerProvider.setScopeCache(scopeCache);
        powerProvider.registerOnInjectedListener(powerOnInject);

        Provider.OnInjectedListener<Driver> driverOnInject = mock(Provider.OnInjectedListener.class);
        ProviderByClassName driverProvider = new ProviderByClassName(Driver.class, DriverImpl.class);
        driverProvider.setScopeCache(scopeCache);
        driverProvider.registerOnInjectedListener(driverOnInject);

        Provider.OnInjectedListener<Robot> robotOnInject = mock(Provider.OnInjectedListener.class);
        ProviderByClassName robotProvider = new ProviderByClassName(Robot.class, RobotImpl.class);
        robotProvider.setScopeCache(scopeCache);
        robotProvider.registerOnInjectedListener(robotOnInject);

        graph.register(powerProvider);
        graph.register(robotProvider);
        graph.register(driverProvider);

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

    @Test(expected = CircularDependenciesException.class)
    public void shouldDetectCircularDependencies() throws ProvideException, CircularDependenciesException, ProviderMissingException, ProviderConflictException {
        SimpleGraph graph = new SimpleGraph();
        graph.register(Power.class, PowerImpl.class);
        graph.register(Driver.class, DriverImpl.class);
        graph.register(Robot.class, RobotImpl.class);

        Factory factory = new Factory();
        graph.inject(factory, MyInject.class);
    }

    @Test
    public void shouldNotifyInjectedCallbackWhenObjectFullyInjectedWithCircularDependencies() throws ProvideException,
            CircularDependenciesException, ProviderMissingException, ProviderConflictException {
        final Factory factory = new Factory();

        final SimpleGraph graph = new SimpleGraph();
        final ScopeCache scopeCache = new ScopeCache();

        ProviderByClassName powerProvider = new ProviderByClassName(Power.class, PowerImpl.class);
        powerProvider.setScopeCache(scopeCache);
        powerProvider.registerOnInjectedListener(new Provider.OnInjectedListener<Power>() {
            @Override
            public void onInjected(Power object) {
                Assert.assertNotNull(((PowerImpl) object).robot);
            }
        });

        ProviderByClassName driverProvider = new ProviderByClassName(Driver.class, DriverImpl.class);
        driverProvider.setScopeCache(scopeCache);
        driverProvider.registerOnInjectedListener(new Provider.OnInjectedListener<Driver>() {
            @Override
            public void onInjected(Driver object) {
                Assert.assertNotNull(((DriverImpl) object).power);
            }
        });

        ProviderByClassName robotProvider = new ProviderByClassName(Robot.class, RobotImpl.class);
        robotProvider.setScopeCache(scopeCache);
        robotProvider.registerOnInjectedListener(new Provider.OnInjectedListener<Robot>() {
            @Override
            public void onInjected(Robot object) {
                Assert.assertNotNull(((RobotImpl) object).driver);
            }
        });

        graph.register(powerProvider);
        graph.register(robotProvider);
        graph.register(driverProvider);

        graph.inject(factory, MyInject.class);
    }

    @Test
    public void shouldInjectObjectOnlyOnceWithCircularDependencies() throws ProvideException,
            CircularDependenciesException, ProviderMissingException, ProviderConflictException, NoSuchFieldException {
        final Factory factory = new Factory();

        final SimpleGraph graph = new SimpleGraph();
        final ScopeCache scopeCache = new ScopeCache();

        Provider.OnInjectedListener<Power> powerOnInject = mock(Provider.OnInjectedListener.class);
        ProviderByClassName powerProvider = new ProviderByClassName(Power.class, PowerImpl.class);
        powerProvider.setScopeCache(scopeCache);
        powerProvider.registerOnInjectedListener(powerOnInject);

        Provider.OnInjectedListener<Driver> driverOnInject = mock(Provider.OnInjectedListener.class);
        ProviderByClassName driverProvider = new ProviderByClassName(Driver.class, DriverImpl.class);
        driverProvider.setScopeCache(scopeCache);
        driverProvider.registerOnInjectedListener(driverOnInject);

        Provider.OnInjectedListener<Robot> robotOnInject = mock(Provider.OnInjectedListener.class);
        ProviderByClassName robotProvider = new ProviderByClassName(Robot.class, RobotImpl.class);
        robotProvider.setScopeCache(scopeCache);
        robotProvider.registerOnInjectedListener(robotOnInject);

        graph.register(powerProvider);
        graph.register(robotProvider);
        graph.register(driverProvider);

        graph.inject(factory, MyInject.class);

        verify(powerOnInject, times(1)).onInjected(any(Power.class));
        verify(driverOnInject, times(1)).onInjected(any(Driver.class));
        verify(robotOnInject, times(1)).onInjected(any(Robot.class));
    }

    @Test
    public void test_should_inject_and_release_correctly_on_single_object() throws ProvideException,
            CircularDependenciesException, ProviderMissingException, ProviderConflictException {
        final SimpleGraph graph = new SimpleGraph();
        final ScopeCache scopeCache = new ScopeCache();
        prepareInjection(scopeCache, graph);

        final Factory factory = new Factory();

        graph.inject(factory, MyInject.class);
        Assert.assertFalse(scopeCache.cache.isEmpty());
        graph.release(factory, MyInject.class);
        Assert.assertTrue(scopeCache.cache.isEmpty());
    }

    @Test
    public void test_should_inject_and_release_correctly_on_multiple_objects() throws ProvideException,
            CircularDependenciesException, ProviderMissingException, ProviderConflictException {
        final SimpleGraph graph = new SimpleGraph();
        final ScopeCache scopeCache = new ScopeCache();
        prepareInjection(scopeCache, graph);

        final Factory factory1 = new Factory();
        final Factory factory2 = new Factory();

        graph.inject(factory1, MyInject.class);

        Provider<Power> powerProvider = graph.getProvider(Power.class, null);
        Provider<Driver> driverProvider = graph.getProvider(Driver.class, null);
        Provider<Robot> robotProvider = graph.getProvider(Robot.class, null);
        
        assertReferenceCount(powerProvider, Power.class, null, 3);
        assertReferenceCount(driverProvider, Driver.class, null, 3);
        assertReferenceCount(robotProvider, Robot.class, null, 2);
        Assert.assertFalse(scopeCache.cache.isEmpty());

        graph.inject(factory2, MyInject.class);
        assertReferenceCount(powerProvider, Power.class, null, 6);
        assertReferenceCount(driverProvider, Driver.class, null, 6);
        assertReferenceCount(robotProvider, Robot.class, null, 4);
        Assert.assertFalse(scopeCache.cache.isEmpty());

        graph.release(factory2, MyInject.class);
        assertReferenceCount(powerProvider, Power.class, null, 3);
        assertReferenceCount(driverProvider, Driver.class, null, 3);
        assertReferenceCount(robotProvider, Robot.class, null, 2);
        Assert.assertFalse(scopeCache.cache.isEmpty());

        graph.release(factory1, MyInject.class);
        Assert.assertTrue(scopeCache.cache.isEmpty());
    }

    @Test
    public void test_should_inject_and_release_correctly_even_with_same_cached_objects_multiple_times() throws ProvideException,
            CircularDependenciesException, ProviderMissingException, ProviderConflictException {
        final SimpleGraph graph = new SimpleGraph();
        final ScopeCache scopeCache = new ScopeCache();
        prepareInjection(scopeCache, graph);

        final Factory factory = new Factory();
        graph.inject(factory, MyInject.class);

        Provider<Power> powerProvider = graph.getProvider(Power.class, null);
        Provider<Driver> driverProvider = graph.getProvider(Driver.class, null);
        Provider<Robot> robotProvider = graph.getProvider(Robot.class, null);

        assertReferenceCount(powerProvider, Power.class, null, 3);
        assertReferenceCount(driverProvider, Driver.class, null, 3);
        assertReferenceCount(robotProvider, Robot.class, null, 2);
        Assert.assertFalse(scopeCache.cache.isEmpty());

        graph.inject(factory, MyInject.class);
        assertReferenceCount(powerProvider, Power.class, null, 6);
        assertReferenceCount(driverProvider, Driver.class, null, 6);
        assertReferenceCount(robotProvider, Robot.class, null, 4);
        Assert.assertFalse(scopeCache.cache.isEmpty());

        graph.release(factory, MyInject.class);
        assertReferenceCount(powerProvider, Power.class, null, 3);
        assertReferenceCount(driverProvider, Driver.class, null, 3);
        assertReferenceCount(robotProvider, Robot.class, null, 2);
        Assert.assertFalse(scopeCache.cache.isEmpty());

        graph.release(factory, MyInject.class);
        Assert.assertTrue(scopeCache.cache.isEmpty());
    }

    @Test
    public void test_should_inject_and_release_correctly_on_multiple_objects_even_with_same_cached_objects_multiple_times() throws ProvideException,
            CircularDependenciesException, ProviderMissingException, ProviderConflictException {
        final SimpleGraph graph = new SimpleGraph();
        final ScopeCache scopeCache = new ScopeCache();
        prepareInjection(scopeCache, graph);

        final Factory factory1 = new Factory();
        graph.inject(factory1, MyInject.class);

        Provider<Power> powerProvider = graph.getProvider(Power.class, null);
        Provider<Driver> driverProvider = graph.getProvider(Driver.class, null);
        Provider<Robot> robotProvider = graph.getProvider(Robot.class, null);
        
        assertReferenceCount(powerProvider, Power.class, null, 3);
        assertReferenceCount(driverProvider, Driver.class, null, 3);
        assertReferenceCount(robotProvider, Robot.class, null, 2);
        Assert.assertFalse(scopeCache.cache.isEmpty());

        final Factory factory2 = new Factory();
        graph.inject(factory2, MyInject.class);
        assertReferenceCount(powerProvider, Power.class, null, 6);
        assertReferenceCount(driverProvider, Driver.class, null, 6);
        assertReferenceCount(robotProvider, Robot.class, null, 4);
        Assert.assertFalse(scopeCache.cache.isEmpty());

        graph.inject(factory1, MyInject.class);
        assertReferenceCount(powerProvider, Power.class, null, 9);
        assertReferenceCount(driverProvider, Driver.class, null, 9);
        assertReferenceCount(robotProvider, Robot.class, null, 6);
        Assert.assertFalse(scopeCache.cache.isEmpty());

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
        assertReferenceCount(powerProvider, Power.class, null, 6);
        assertReferenceCount(driverProvider, Driver.class, null, 6);
        assertReferenceCount(robotProvider, Robot.class, null, 4);
        Assert.assertFalse(scopeCache.cache.isEmpty());

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
        assertReferenceCount(powerProvider, Power.class, null, 3);
        assertReferenceCount(driverProvider, Driver.class, null, 3);
        assertReferenceCount(robotProvider, Robot.class, null, 2);
        Assert.assertFalse(scopeCache.cache.isEmpty());

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
        assertReferenceCount(powerProvider, Power.class, null, 3);
        assertReferenceCount(driverProvider, Driver.class, null, 3);
        assertReferenceCount(robotProvider, Robot.class, null, 2);
        Assert.assertFalse(scopeCache.cache.isEmpty());

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
        Assert.assertTrue(scopeCache.cache.isEmpty());
    }

    private void assertReferenceCount(Provider provider, Class type, Annotation qualifier, int count) {
        if (provider != null) {
            Assert.assertEquals(provider.totalReference(), count);
        } else {
            Assert.assertEquals(0, count);
        }
    }

    private void prepareInjection(ScopeCache scopeCache, SimpleGraph graph) throws ProviderConflictException {
        ProviderByClassName powerProvider = new ProviderByClassName(Power.class, PowerImpl.class);
        powerProvider.setScopeCache(scopeCache);

        ProviderByClassName driverProvider = new ProviderByClassName(Driver.class, DriverImpl.class);
        driverProvider.setScopeCache(scopeCache);

        ProviderByClassName robotProvider = new ProviderByClassName(Robot.class, RobotImpl.class);
        robotProvider.setScopeCache(scopeCache);

        graph.register(powerProvider);
        graph.register(robotProvider);
        graph.register(driverProvider);
    }

    static class Factory {
        @MyInject
        private Power power;

        @MyInject
        private Driver driver;
    }

    static class RobotImpl implements Robot {
        @MyInject
        private Driver driver;

        @MyInject
        private Power power;
    }

    static class DriverImpl implements Driver {
        @MyInject
        private Power power;

        @MyInject
        private Robot robot;
    }

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
