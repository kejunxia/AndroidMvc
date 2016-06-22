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

import com.shipdream.lib.android.mvc.MvcComponent;
import com.shipdream.lib.android.mvc.MvcGraph;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusV;
import com.shipdream.lib.android.mvc.event.bus.internal.EventBusImpl;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;

public class TestInjector {
    MvcGraph graph;
    
    @Before
    public void setUp() throws Exception {
        graph = new MvcGraph();
        graph.setRootComponent((MvcComponent) new MvcComponent("Root").register(new Object() {
            @Provides
            @EventBusC
            public EventBus eventBusC() {
                return new EventBusImpl();
            }

            @Provides
            @EventBusV
            public EventBus eventBusV() {
                return new EventBusImpl();
            }

            @Provides
            public ExecutorService provideExe() {
                return mock(ExecutorService.class);
            }
        }));
    }

    private int getGraphSize() {
        Map<String, Object> cache = graph.getRootComponent().getCache();
        return cache == null ? 0 :cache.size();
    }
    
    @Test
    public void should_return_all_cached_instances_from_mvc_graph() throws ProvideException, ProviderConflictException {
        Assert.assertEquals(0, getGraphSize());

        class View1 {
            @Inject
            @EventBusC
            EventBus eventBus;
        }

        View1 v1 = new View1();
        graph.inject(v1);
        Assert.assertEquals(1, getGraphSize());

        class View2 {
            @Inject
            @EventBusC
            EventBus eventBus;
        }

        View2 v2 = new View2();
        graph.inject(v2);
        Assert.assertEquals(1, getGraphSize());

        class View3 {
            @Inject
            @EventBusV
            EventBus eventBus;
        }

        View3 v3 = new View3();
        graph.inject(v3);
        Assert.assertEquals(2, getGraphSize());
    }
}
