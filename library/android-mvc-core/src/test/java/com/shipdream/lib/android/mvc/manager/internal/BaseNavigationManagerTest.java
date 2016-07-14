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

package com.shipdream.lib.android.mvc.manager.internal;

import com.shipdream.lib.android.mvc.Mvc;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.BaseTest;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Provides;

import org.junit.After;
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

        Mvc.graph().getRootComponent().attach(new Component().register(new Object() {
            @Provides
            protected ExecutorService createExecutorService() {
                return executorService;
            }
        }), true);

        navigationManager = new NavigationManager();
        graph.inject(navigationManager);
        navigationManager.onCreated();
    }

    @After
    public void tearDown() throws Exception {
        navigationManager.onDestroy();
    }
}
