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
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Singleton;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestNestedInjectionAndRelease extends BaseTestCases {
    interface Service {
    }

    interface Controller {
    }

    class OnFree {
        void onFreed() {}
    }

    Object module;

    class ControllerImpl implements Controller {
        @MyInject
        Service service;
    }

    class ViewA {
        @MyInject
        Controller controller;
    }

    class ViewB {
        @MyInject
        Controller controller;
    }

    class ViewC {
        @MyInject
        Controller controller;
    }

    private OnFree serviceOnFreed;
    private OnFree controllerOnFreed;
    private Service serviceMock;
    private Graph graph;
    private Component component;

    @Before
    public void setUp() throws Exception {
        serviceOnFreed = mock(OnFree.class);
        controllerOnFreed = mock(OnFree.class);
        serviceMock = mock(Service.class);
        graph = new Graph();
        module = new Object() {
            @Provides
            @Singleton
            public Controller provideController() {
                return new ControllerImpl();
            }

            @Provides
            @Singleton
            public Service providesFood2() {
                return serviceMock;
            }
        };

        component = new Component("AppSignleton");
        component.register(module);
        graph.registerDereferencedListener(new Provider.DereferenceListener() {
            @Override
            public <T> void onDereferenced(Provider<T> provider, T instance) {
                if (provider.type() == Service.class) {
                    if (provider.getReferenceCount() == 0) {
                        serviceOnFreed.onFreed();
                    }
                } else if (provider.type() == Controller.class) {
                    if (provider.getReferenceCount() == 0) {
                        controllerOnFreed.onFreed();
                    }
                }
            }
        });

        graph.setRootComponent(component);
    }

    @Test
    public void should_not_release_nested_instance_until_all_of_its_holders_are_disposed_by_instance_injection()
            throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        ViewA viewA = new ViewA();
        ViewB viewB = new ViewB();
        ViewC viewC = new ViewC();

        //Simulate to navigate to ViewA
        graph.inject(viewA, MyInject.class);
        Controller controllerInA = viewA.controller;
        Service serviceInA = ((ControllerImpl) viewA.controller).service;

        verify(serviceOnFreed, times(0)).onFreed();
        verify(controllerOnFreed, times(0)).onFreed();

        //Simulate to navigate to ViewB
        graph.inject(viewB, MyInject.class);
        Controller controllerInB = viewB.controller;
        Service serviceInB = ((ControllerImpl) viewB.controller).service;
        graph.release(viewA, MyInject.class);

        verify(controllerOnFreed, times(0)).onFreed();
        verify(serviceOnFreed, times(0)).onFreed();
        Assert.assertNotNull(component.scopeCache.findInstance(Controller.class, null));
        Assert.assertNotNull(component.scopeCache.findInstance(Service.class, null));
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInA);
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInB);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInA);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInB);
        Assert.assertEquals(1, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(1, component.findProvider(Service.class, null).getReferenceCount());

        //Simulate to navigate to ViewC
        graph.inject(viewC, MyInject.class);
        Controller controllerInC = viewB.controller;
        Service serviceInC = ((ControllerImpl) viewC.controller).service;
        graph.release(viewB, MyInject.class);

        verify(controllerOnFreed, times(0)).onFreed();
        verify(serviceOnFreed, times(0)).onFreed();
        Assert.assertNotNull(component.scopeCache.findInstance(Controller.class, null));
        Assert.assertNotNull(component.scopeCache.findInstance(Service.class, null));
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInA);
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInB);
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInC);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInA);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInB);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInC);
        Assert.assertEquals(1, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(1, component.findProvider(Service.class, null).getReferenceCount());

        Assert.assertTrue(controllerInA == controllerInB);
        Assert.assertTrue(controllerInB == controllerInC);

        //Simulate to navigate back to ViewB
        graph.inject(viewB, MyInject.class);
        Assert.assertTrue(controllerInC == viewB.controller);
        Assert.assertTrue(serviceInC == serviceInB);
        graph.release(viewC, MyInject.class);

        verify(controllerOnFreed, times(0)).onFreed();
        verify(serviceOnFreed, times(0)).onFreed();
        Assert.assertNotNull(component.scopeCache.findInstance(Controller.class, null));
        Assert.assertNotNull(component.scopeCache.findInstance(Service.class, null));
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInA);
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInB);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInA);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInB);
        Assert.assertEquals(1, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(1, component.findProvider(Service.class, null).getReferenceCount());

        //Simulate to navigate back to ViewA
        graph.inject(viewA, MyInject.class);
        Assert.assertTrue(controllerInB == viewA.controller);
        Assert.assertTrue(serviceInB == serviceInA);
        graph.release(viewB, MyInject.class);

        verify(controllerOnFreed, times(0)).onFreed();
        verify(serviceOnFreed, times(0)).onFreed();
        Assert.assertNotNull(component.scopeCache.findInstance(Controller.class, null));
        Assert.assertNotNull(component.scopeCache.findInstance(Service.class, null));
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInA);
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInB);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInA);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInB);
        Assert.assertEquals(1, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(1, component.findProvider(Service.class, null).getReferenceCount());

        //Simulate to navigate back to exit
        graph.release(viewA, MyInject.class);

        verify(controllerOnFreed, times(1)).onFreed();
        verify(serviceOnFreed, times(1)).onFreed();
        Assert.assertNull(component.scopeCache.findInstance(Controller.class, null));
        Assert.assertNull(component.scopeCache.findInstance(Service.class, null));
        Assert.assertEquals(0, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(0, component.findProvider(Service.class, null).getReferenceCount());
    }

    @Test
    public void should_count_reference_correctly_for_reference_dereference_methods()
            throws ProvideException, CircularDependenciesException, ProviderMissingException {
        ViewA viewA = new ViewA();
        ViewB viewB = new ViewB();

        //Simulate to navigate to ViewA
        graph.reference(Controller.class, null, MyInject.class);
        Assert.assertEquals(1, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(1, component.findProvider(Service.class, null).getReferenceCount());

        graph.inject(viewA, MyInject.class);
        Assert.assertEquals(2, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(2, component.findProvider(Service.class, null).getReferenceCount());

        graph.dereference(viewA.controller, Controller.class, null, MyInject.class);
        Assert.assertEquals(1, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(1, component.findProvider(Service.class, null).getReferenceCount());

        verify(serviceOnFreed, times(0)).onFreed();
        verify(controllerOnFreed, times(0)).onFreed();

        //Simulate to navigate to ViewB
        graph.reference(Controller.class, null, MyInject.class);
        Assert.assertEquals(2, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(2, component.findProvider(Service.class, null).getReferenceCount());

        graph.inject(viewB, MyInject.class);
        Assert.assertEquals(3, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(3, component.findProvider(Service.class, null).getReferenceCount());

        graph.release(viewA, MyInject.class);
        Assert.assertEquals(2, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(2, component.findProvider(Service.class, null).getReferenceCount());

        graph.dereference(viewB.controller, Controller.class, null, MyInject.class);
        Assert.assertEquals(1, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(1, component.findProvider(Service.class, null).getReferenceCount());
    }

    @Test
    public void should_not_release_nested_instance_until_all_of_its_holders_are_disposed_by_reference_methods()
            throws ProvideException, ProviderConflictException, CircularDependenciesException, ProviderMissingException {
        ViewA viewA = new ViewA();
        ViewB viewB = new ViewB();
        ViewC viewC = new ViewC();

        //Simulate to navigate to ViewA
        graph.reference(Controller.class, null, MyInject.class);
        graph.inject(viewA, MyInject.class);
        Controller controllerInA = viewA.controller;
        Service serviceInA = ((ControllerImpl)viewA.controller).service;
        graph.dereference(controllerInA, Controller.class, null, MyInject.class);

        verify(serviceOnFreed, times(0)).onFreed();
        verify(controllerOnFreed, times(0)).onFreed();

        //Simulate to navigate to ViewB
        graph.reference(Controller.class, null, MyInject.class);
        graph.inject(viewB, MyInject.class);
        Controller controllerInB = viewB.controller;
        Service serviceInB = ((ControllerImpl)viewB.controller).service;
        graph.release(viewA, MyInject.class);
        graph.dereference(controllerInB, Controller.class, null, MyInject.class);

        verify(controllerOnFreed, times(0)).onFreed();
        verify(serviceOnFreed, times(0)).onFreed();
        Assert.assertNotNull(component.scopeCache.findInstance(Controller.class, null));
        Assert.assertNotNull(component.scopeCache.findInstance(Service.class, null));
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInA);
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInB);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInA);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInB);
        Assert.assertEquals(1, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(1, component.findProvider(Service.class, null).getReferenceCount());

        //Simulate to navigate to ViewC
        graph.reference(Controller.class, null, MyInject.class);
        graph.inject(viewC, MyInject.class);
        Controller controllerInC = viewB.controller;
        Service serviceInC = ((ControllerImpl)viewC.controller).service;
        graph.release(viewB, MyInject.class);
        graph.dereference(controllerInC, Controller.class, null, MyInject.class);

        verify(controllerOnFreed, times(0)).onFreed();
        verify(serviceOnFreed, times(0)).onFreed();
        Assert.assertNotNull(component.scopeCache.findInstance(Controller.class, null));
        Assert.assertNotNull(component.scopeCache.findInstance(Service.class, null));
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInA);
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInB);
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInC);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInA);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInB);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInC);
        Assert.assertEquals(1, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(1, component.findProvider(Service.class, null).getReferenceCount());

        Assert.assertTrue(controllerInA == controllerInB);
        Assert.assertTrue(controllerInB == controllerInC);

        //Simulate to navigate back to ViewB
        graph.reference(Controller.class, null, MyInject.class);
        graph.inject(viewB, MyInject.class);
        Assert.assertTrue(controllerInC == viewB.controller);
        Assert.assertTrue(serviceInC == serviceInB);
        graph.release(viewC, MyInject.class);
        graph.dereference(viewB.controller, Controller.class, null, MyInject.class);

        verify(controllerOnFreed, times(0)).onFreed();
        verify(serviceOnFreed, times(0)).onFreed();
        Assert.assertNotNull(component.scopeCache.findInstance(Controller.class, null));
        Assert.assertNotNull(component.scopeCache.findInstance(Service.class, null));
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInA);
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInB);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInA);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInB);
        Assert.assertEquals(1, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(1, component.findProvider(Service.class, null).getReferenceCount());

        //Simulate to navigate back to ViewA
        graph.reference(Controller.class, null, MyInject.class);
        graph.inject(viewA, MyInject.class);
        Assert.assertTrue(controllerInB == viewA.controller);
        Assert.assertTrue(serviceInB == serviceInA);
        graph.release(viewB, MyInject.class);
        graph.dereference(viewA.controller, Controller.class, null, MyInject.class);

        verify(controllerOnFreed, times(0)).onFreed();
        verify(serviceOnFreed, times(0)).onFreed();
        Assert.assertNotNull(component.scopeCache.findInstance(Controller.class, null));
        Assert.assertNotNull(component.scopeCache.findInstance(Service.class, null));
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInA);
        Assert.assertTrue(component.findProvider(Controller.class, null).get() == controllerInB);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInA);
        Assert.assertTrue(component.findProvider(Service.class, null).get() == serviceInB);
        Assert.assertEquals(1, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(1, component.findProvider(Service.class, null).getReferenceCount());

        //Simulate to navigate back to exit
        graph.release(viewA, MyInject.class);

        verify(controllerOnFreed, times(1)).onFreed();
        verify(serviceOnFreed, times(1)).onFreed();
        Assert.assertNull(component.scopeCache.findInstance(Controller.class, null));
        Assert.assertNull(component.scopeCache.findInstance(Service.class, null));
        Assert.assertEquals(0, component.findProvider(Controller.class, null).getReferenceCount());
        Assert.assertEquals(0, component.findProvider(Service.class, null).getReferenceCount());
    }
}
