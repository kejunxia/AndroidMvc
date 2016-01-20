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

package com.shipdream.lib.android.mvc.samples.note.controller.internal;

import com.shipdream.lib.android.mvc.Injector;
import com.shipdream.lib.android.mvc.MvcGraph;
import com.shipdream.lib.android.mvc.controller.BaseController;
import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.internal.EventBusImpl;

import org.junit.After;
import org.junit.Before;

import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.mock;

public abstract class TestControllerBase <Controller extends BaseController> extends TestBase {
    protected EventBus eventBusC;
    protected EventBus eventBusV;
    protected ExecutorService executorService;
    protected Controller controllerToTest;

    private void prepareGraph() {
        eventBusC = new EventBusImpl();
        eventBusV = new EventBusImpl();
        executorService = mock(ExecutorService.class);

        Injector.configGraph(new MvcGraph.BaseDependencies() {
            @Override
            public EventBus createEventBusC() {
                return eventBusC;
            }

            @Override
            public EventBus createEventBusV() {
                return eventBusV;
            }

            @Override
            public ExecutorService createExecutorService() {
                return executorService;
            }
        });
    }

    @Before
    public void setUp() throws Exception {
        prepareGraph();

        registerDependencies(Injector.getGraph());
        controllerToTest = createTestingController();
        Injector.getGraph().inject(controllerToTest);
        ((BaseControllerImpl)controllerToTest).onConstruct();
    }

    @After
    public void tearDown() throws Exception {
        Injector.getGraph().release(controllerToTest);
    }

    protected void registerDependencies(MvcGraph mvcGraph) {
    }

    protected abstract Controller createTestingController();
}
