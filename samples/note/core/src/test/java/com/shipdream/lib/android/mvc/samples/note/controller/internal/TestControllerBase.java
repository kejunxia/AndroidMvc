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

package com.shipdream.lib.android.mvc.samples.note.controller.internal;

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
    protected EventBus eventBusC2C;
    protected EventBus eventBusC2V;
    protected ExecutorService executorService;
    private MvcGraph mvcGraph;
    protected Controller controllerToTest;

    @Before
    public void setUp() throws Exception {
        eventBusC2C = new EventBusImpl();
        eventBusC2V = new EventBusImpl();
        mvcGraph = new MvcGraph(new MvcGraph.BaseDependencies() {
            @Override
            public EventBus createEventBusC2C() {
                return eventBusC2C;
            }

            @Override
            public EventBus createEventBusC2V() {
                return eventBusC2V;
            }

            @Override
            public ExecutorService createExecutorService() {
                return executorService;
            }
        });
        executorService = mock(ExecutorService.class);
        registerDependencies(mvcGraph);
        controllerToTest = createTestingController();
        mvcGraph.inject(controllerToTest);
        ((BaseControllerImpl)controllerToTest).onConstruct();
    }

    @After
    public void tearDown() throws Exception {
        mvcGraph.release(controllerToTest);
    }

    protected void registerDependencies(MvcGraph mvcGraph) {
    }

    protected abstract Controller createTestingController();
}
