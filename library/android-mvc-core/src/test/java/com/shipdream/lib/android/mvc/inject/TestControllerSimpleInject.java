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

package com.shipdream.lib.android.mvc.inject;

import com.shipdream.lib.android.mvc.BaseTest;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.LifeCycleTestController;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.MissingImplPresenter;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.PrintController;
import com.shipdream.lib.poke.Provider;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestControllerSimpleInject extends BaseTest {
    private static class TestView {
        @Inject
        private PrintController printController;

        public void present() {
            printController.print();
        }
    }

    @Test
    public void testInjectionOfRealController() throws Exception {
        TestView testView = new TestView();
        graph.inject(testView);

        testView.present();
    }

    @Test
    public void testInjectionOfMockController() throws Exception {
        final PrintController mockPrintController = mock(PrintController.class);

        graph.getRootComponent().register(new Provider<PrintController>(PrintController.class) {
            @Override
            protected PrintController createInstance() throws ProvideException {
                return mockPrintController;
            }
        });
        TestView testView = new TestView();
        graph.inject(testView);
        testView.present();

        verify(mockPrintController, times(1)).print();
    }

    public static class TestBadView {
        @Inject
        private MissingImplPresenter controller;
    }

    @Test
    public void shouldThrowProviderMissingWhenControllerImplIsNotFound() throws Exception {
        TestBadView testView = new TestBadView();

        boolean detectedProviderMissingException = false;
        try {
            graph.inject(testView);
        } catch (RuntimeException e) {
            if(e.getCause() instanceof ProviderMissingException) {
                detectedProviderMissingException = true;
            }
        }
        Assert.assertTrue(detectedProviderMissingException);
    }

    @Test
    public void injectedControllerShouldBeSingleton() throws Exception {
        TestView testView = new TestView();
        graph.inject(testView);

        TestView testView1 = new TestView();
        graph.inject(testView1);

        Assert.assertTrue(testView != testView1);
        Assert.assertTrue(testView.printController == testView1.printController);
    }

    private static class TestLifCycleView {
        @Inject
        private LifeCycleTestController controller;
    }

    @Test
    public void testOnDisposeShouldBeCalledWhenControllerReleased() throws Exception {
        final LifeCycleTestController.Proxy lifeCycleProxy = mock(LifeCycleTestController.Proxy.class);
        graph.getRootComponent().register(new LifeCycleTestControllerModule(lifeCycleProxy));

        TestLifCycleView testView = new TestLifCycleView();
        verify(lifeCycleProxy, times(0)).onCreated();
        graph.inject(testView);
        verify(lifeCycleProxy, times(1)).onCreated();
        verify(lifeCycleProxy, times(0)).onDestroy();

        TestLifCycleView testView1 = new TestLifCycleView();
        graph.inject(testView1);
        //Should be 1 still since a cached instance will be reused.
        verify(lifeCycleProxy, times(1)).onCreated();
        verify(lifeCycleProxy, times(0)).onDestroy();

        graph.release(testView1);
        verify(lifeCycleProxy, times(0)).onDestroy();

        graph.release(testView);
        verify(lifeCycleProxy, times(1)).onDestroy();
    }

    public static class Car {

    }

    static class Road {
        @Inject
        private Car car;
    }

    @Test
    public void should_be_able_to_inject_concrete_class() throws Exception {
        Road road = new Road();

        Assert.assertNull(road.car);

        graph.inject(road);

        Assert.assertNotNull(road.car);
    }


    public static class LifeCycleTestControllerModule {
        private LifeCycleTestController.Proxy proxy;

        public LifeCycleTestControllerModule(LifeCycleTestController.Proxy proxy) {
            this.proxy = proxy;
        }

        @Provides
        public LifeCycleTestController provideLifeCycleTestPresenter() {
            LifeCycleTestController controller = new LifeCycleTestController();
            controller.proxy = proxy;
            return controller;
        }
    }
}
