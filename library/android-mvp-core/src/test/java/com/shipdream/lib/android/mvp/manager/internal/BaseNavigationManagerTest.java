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

package com.shipdream.lib.android.mvp.manager.internal;

import com.shipdream.lib.android.mvp.Mvp;
import com.shipdream.lib.android.mvp.MvpComponent;
import com.shipdream.lib.android.mvp.NavigationManager;
import com.shipdream.lib.android.mvp.event.bus.EventBus;
import com.shipdream.lib.android.mvp.event.bus.annotation.EventBusC;
import com.shipdream.lib.android.mvp.event.bus.internal.EventBusImpl;
import com.shipdream.lib.android.mvp.presenter.BaseTest;
import com.shipdream.lib.poke.Provides;

import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class BaseNavigationManagerTest extends BaseTest {
    protected NavigationManager navigationManager;
    protected ExecutorService executorService;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        executorService = mock(ExecutorService.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = (Runnable) invocation.getArguments()[0];
                runnable.run();
                return null;
            }
        }).when(executorService).submit(any(Runnable.class));

        Mvp.graph().setRootComponent(new MvpComponent("MvpTestRoot").register(new Object() {
            @Provides
            @EventBusC
            protected EventBus createEventBusC() {
                return new EventBusImpl();
            }

            @Provides
            protected ExecutorService createExecutorService() {
                return executorService;
            }
        }));

        navigationManager = new NavigationManager();
        graph.inject(navigationManager);
        navigationManager.onCreated();
    }
}
