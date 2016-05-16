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

package com.shipdream.lib.android.mvp;

import com.shipdream.lib.android.mvp.event.bus.EventBus;
import com.shipdream.lib.android.mvp.event.bus.annotation.EventBusC;
import com.shipdream.lib.android.mvp.event.bus.annotation.EventBusV;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;

public class TestInjector {
    @After
    public void tearDown() throws Exception {
        Injector.mvpGraph = null;
    }

    @Test(expected = RuntimeException.class)
    public void should_raise_exception_when_getting_mvp_graph_before_configuring_it() {
        Injector.getGraph();
    }

    private int getGraphSize() {
        return Injector.getGraph().singletonScopeCache.getCachedItems().size();
    }
    
    @Test
    public void should_return_all_cached_instances_from_injector_graph() {
        Injector.configGraph(new MvpGraph.BaseDependencies() {
            @Override
            protected ExecutorService createExecutorService() {
                return mock(ExecutorService.class);
            }
        });
        Assert.assertEquals(0, getGraphSize());

        class View1 {
            @Inject
            @EventBusC
            EventBus eventBus;
        }

        View1 v1 = new View1();
        Injector.getGraph().inject(v1);
        Assert.assertEquals(1, getGraphSize());

        class View2 {
            @Inject
            @EventBusC
            EventBus eventBus;
        }

        View2 v2 = new View2();
        Injector.getGraph().inject(v2);
        Assert.assertEquals(1, getGraphSize());

        class View3 {
            @Inject
            @EventBusV
            EventBus eventBus;
        }

        View3 v3 = new View3();
        Injector.getGraph().inject(v3);
        Assert.assertEquals(2, getGraphSize());
    }
}
