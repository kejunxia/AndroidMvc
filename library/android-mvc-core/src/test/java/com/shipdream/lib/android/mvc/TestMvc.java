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

package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusV;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestMvc extends BaseTest{
    private UiThreadRunner originalUiThreadRunner;
    private UiThreadRunner uiThreadRunnerMock;

    @Inject
    @EventBusV
    private EventBus eventBusV;

    @Inject
    private ExecutorService executorService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        originalUiThreadRunner = Mvc.graph().uiThreadRunner;

        uiThreadRunnerMock = mock(UiThreadRunner.class);

        when(uiThreadRunnerMock.isOnUiThread()).thenReturn(true);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = (Runnable) invocation.getArguments()[0];
                runnable.run();
                return null;
            }
        }).when(uiThreadRunnerMock).post(any(Runnable.class));
        Mvc.graph().uiThreadRunner = uiThreadRunnerMock;
    }

    @After
    public void tearDown() throws Exception {
        Mvc.graph().uiThreadRunner = originalUiThreadRunner;
    }

    @Test
    public void eventBusV_should_post_event_to_ui_thread_strait_away_from_ui_thread() throws Exception {
        Mvc.graph().inject(this);

        when(uiThreadRunnerMock.isOnUiThread()).thenReturn(true);
        eventBusV.post("");

        verify(uiThreadRunnerMock, times(2)).isOnUiThread();
        verify(uiThreadRunnerMock, times(0)).post(any(Runnable.class));
    }

    @Test
    public void eventBusV_should_post_event_to_ui_thread_from_non_ui_thread() throws Exception {
        Mvc.graph().inject(this);

        when(uiThreadRunnerMock.isOnUiThread()).thenReturn(false);
        eventBusV.post("");

        verify(uiThreadRunnerMock, times(2)).isOnUiThread();
        verify(uiThreadRunnerMock, times(1)).post(any(Runnable.class));
    }

    @Test
    public void should_be_able_to_schedule_runnable_on_new_thread() {
        Mvc.graph().inject(this);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
            }
        });
    }
}

