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
import com.shipdream.lib.android.mvp.event.bus.internal.EventBusImpl;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Provides;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import javax.inject.Singleton;

import static org.mockito.Mockito.mock;

public class TestMvpGraphHelper {
    @After
    public void tearDown() throws Exception {
        Injector.mvpGraph = null;
    }

    @Test(expected = RuntimeException.class)
    public void should_raise_exception_when_getting_mvp_graph_before_configuring_it() {
        Injector.getGraph();
    }

    static class Comp extends Component {
        @Provides
        @EventBusC
        @Singleton
        public EventBus providesIEventBusC() {
            return new EventBusImpl();
        }
    }

    @Test(expected = RuntimeException.class)
    public void should_raise_runtime_exception_when_exception_occurrs_by_configuring_mvp_graph_dependencies() {
        Injector.getGraph().register(new Component() {
            @Provides
            @EventBusC
            @Singleton
            public EventBus providesIEventBusC() {
                return mock(EventBus.class);
            }
        });

        //Register an event bus that will raise a duplicate registering exception when register the
        //BaseDependencies

        MvpGraph.BaseDependencies baseDependencies = new MvpGraph.BaseDependencies() {
            @Override
            protected ExecutorService createExecutorService() {
                return mock(ExecutorService.class);
            }
        };

        Injector.configGraph(baseDependencies);
    }

    @Test(expected = RuntimeException.class)
    public void should_raise_runtime_exception_when_exception_occurrs_by_configuring_mvp_graph_by_injector() {
        MvpGraph.BaseDependencies baseDependencies = new MvpGraph.BaseDependencies() {
            @Override
            protected ExecutorService createExecutorService() {
                return mock(ExecutorService.class);
            }
        };

        Injector.configGraph(baseDependencies);

        //Register component providing duplicate instances
        Injector.getGraph().register(new Comp());

        //Exception should be raised here
        Injector.configGraph(baseDependencies);
    }
}
