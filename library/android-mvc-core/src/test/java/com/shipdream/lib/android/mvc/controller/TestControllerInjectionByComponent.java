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

package com.shipdream.lib.android.mvc.controller;

import com.shipdream.lib.android.mvc.MvcGraph;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC;
import com.shipdream.lib.android.mvc.event.bus.internal.EventBusImpl;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.PrintController;
import com.shipdream.lib.poke.Provides;

import org.junit.Test;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;

public class TestControllerInjectionByComponent {
    public static class TestBadView {
        @Inject
        private PrintController controller;
    }

    @Test
    public void dependenciesOfBaseControllerImplShouldBeInjected() throws Exception{
        MvcGraph graph = new MvcGraph();

        graph.getRootComponent().register(new Object() {
            @Provides
            @EventBusC
            public EventBus eventBus() {
                return new EventBusImpl();
            }

            @Provides
            public ExecutorService provideExe() {
                return mock(ExecutorService.class);
            }
        });

        TestBadView testView = new TestBadView();
        graph.inject(testView);
    }
}
