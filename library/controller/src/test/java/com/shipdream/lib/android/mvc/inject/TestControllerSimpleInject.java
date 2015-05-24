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

package com.shipdream.lib.android.mvc.inject;

import com.shipdream.lib.android.mvc.MvcGraph;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.LifeCycleTestController;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.MissingImplController;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.PrintController;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.SimpleGraph;
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

public class TestControllerSimpleInject extends BaseTestCases {
    private static class TestView {
        @Inject
        private PrintController printController;

        public void present() {
            printController.print();
        }
    }

    @Test
    public void testInjectionOfRealController() throws Exception {
        MvcGraph graph = new MvcGraph(new BaseControllerDependencies());

        TestView testView = new TestView();
        graph.inject(testView);

        testView.present();
    }

    @Test
    public void testInjectionOfMockController() throws Exception {
        final PrintController mockPrintController = mock(PrintController.class);

        SimpleGraph graph = new SimpleGraph();
        graph.register(new Provider<PrintController>(PrintController.class) {
            @Override
            protected PrintController createInstance() throws ProvideException {
                return mockPrintController;
            }
        });
        TestView testView = new TestView();
        graph.inject(testView, Inject.class);
        testView.present();

        verify(mockPrintController, times(1)).print();
    }

    public static class TestBadView {
        @Inject
        private MissingImplController controller;
    }

    @Test
    public void shouldThrowProviderMissingWhenControllerImplIsNotFound() throws Exception {
        MvcGraph graph = new MvcGraph(new BaseControllerDependencies());

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
        MvcGraph graph = new MvcGraph(new BaseControllerDependencies());

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
        MvcGraph graph = new MvcGraph(new BaseControllerDependencies());
        graph.register(new LifeCycleTestControllerComponent(lifeCycleProxy));

        TestLifCycleView testView = new TestLifCycleView();
        verify(lifeCycleProxy, times(0)).initCalled();
        graph.inject(testView);
        verify(lifeCycleProxy, times(1)).initCalled();
        verify(lifeCycleProxy, times(0)).disposeCalled();

        graph.release(testView);
        verify(lifeCycleProxy, times(1)).disposeCalled();
    }

    public static class LifeCycleTestControllerComponent extends Component {
        private LifeCycleTestController.Proxy proxy;

        public LifeCycleTestControllerComponent(LifeCycleTestController.Proxy proxy) {
            this.proxy = proxy;
        }

        @Provides
        public LifeCycleTestController.Proxy provideLifeCycleTestController$Proxy() {
            return proxy;
        }
    }
}
