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
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import javax.inject.Singleton;

import static org.mockito.Mockito.mock;

public class TestMvpGraphHelper {
    @After
    public void tearDown() throws Exception {
        Injector.graph = null;
    }

    @Test(expected = RuntimeException.class)
    public void should_raise_exception_when_getting_mvp_graph_before_configuring_it() {
        Injector.getGraph();
    }

    static class Comp {
        @Provides
        @EventBusC
        @Singleton
        public EventBus providesIEventBusC() {
            return new EventBusImpl();
        }
    }

    @Test(expected = RuntimeException.class)
    public void should_raise_runtime_exception_when_exception_occurs_by_configuring_mvp_graph_dependencies()
            throws ProvideException, ProviderConflictException {
        Injector.getGraph().getRootComponent().register(new Object() {
            @Provides
            @EventBusC
            @Singleton
            public EventBus providesIEventBusC() {
                return mock(EventBus.class);
            }

            public ExecutorService provideExe() {
                return mock(ExecutorService.class);
            }
        });
    }

}
