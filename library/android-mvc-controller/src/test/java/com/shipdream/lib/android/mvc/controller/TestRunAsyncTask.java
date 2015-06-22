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

package com.shipdream.lib.android.mvc.controller;

import com.shipdream.lib.android.mvc.controller.internal.AsyncTask;
import com.shipdream.lib.android.mvc.controller.internal.MyControllerImpl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import java.util.concurrent.Executors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestRunAsyncTask extends BaseControllerTest {
    class EventMonitor {
        public void onEvent(MyControllerImpl.ResourceLoaded event) {}

        public void onEvent(MyControllerImpl.ResourceLoadFailed event) {}

        public void onEvent(MyControllerImpl.ResourceLoadCanceled event) {}
    }

    private MyControllerImpl controller;
    private EventMonitor eventMonitor;
    private static final long WAIT_DURATION = MyControllerImpl.LONG_TASK_DURATION + 1000;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        executorService = Executors.newSingleThreadExecutor();

        controller = new MyControllerImpl();
        graph.inject(controller);
        controller.init();

        eventMonitor = mock(EventMonitor.class);
        eventBusC2V.register(eventMonitor);
    }

    @Test
    public void should_be_able_to_run_async_task_without_error_handler_with_default_executorService() throws Exception {
        AsyncTask asyncTask = controller.loadHeavyResourceSuccessfullyWithoutErrorHandlerWithDefaultExecutorService(this);

        Thread.sleep(WAIT_DURATION);

        ArgumentCaptor<MyControllerImpl.ResourceLoaded> eventResourceLoaded
                = ArgumentCaptor.forClass(MyControllerImpl.ResourceLoaded.class);
        verify(eventMonitor, times(1)).onEvent(eventResourceLoaded.capture());

        Assert.assertEquals(asyncTask.getState(), AsyncTask.State.DONE);
    }

    @Test
    public void should_be_able_to_run_async_task_without_error_handler() throws Exception {
        AsyncTask asyncTask = controller.loadHeavyResourceSuccessfullyWithoutErrorHandler(this);

        Thread.sleep(WAIT_DURATION);

        ArgumentCaptor<MyControllerImpl.ResourceLoaded> eventResourceLoaded
                = ArgumentCaptor.forClass(MyControllerImpl.ResourceLoaded.class);
        verify(eventMonitor, times(1)).onEvent(eventResourceLoaded.capture());

        Assert.assertEquals(asyncTask.getState(), AsyncTask.State.DONE);
    }

    @Test
    public void should_log_error_without_error_handler() throws Exception {
        Logger logger = mock(Logger.class);
        controller.setLogger(logger);
        controller.loadHeavyResourceWithExceptionButWithoutCustomErrorHandler(this);

        Thread.sleep(WAIT_DURATION);

        verify(logger, times(1)).warn(anyString(), any(Exception.class));
    }

    @Test
    public void shouldBeAbleToRunAsyncTaskSuccessfully() throws Exception {
        AsyncTask asyncTask = controller.loadHeavyResourceSuccessfullyWithErrorHandler(this);

        Thread.sleep(WAIT_DURATION);

        ArgumentCaptor<MyControllerImpl.ResourceLoaded> eventResourceLoaded
                = ArgumentCaptor.forClass(MyControllerImpl.ResourceLoaded.class);
        verify(eventMonitor, times(1)).onEvent(eventResourceLoaded.capture());

        ArgumentCaptor<MyControllerImpl.ResourceLoadFailed> eventResourceLoadFailed
                = ArgumentCaptor.forClass(MyControllerImpl.ResourceLoadFailed.class);
        verify(eventMonitor, times(0)).onEvent(eventResourceLoadFailed.capture());

        ArgumentCaptor<MyControllerImpl.ResourceLoadCanceled> eventResourceLoadCanceled
                = ArgumentCaptor.forClass(MyControllerImpl.ResourceLoadCanceled.class);
        verify(eventMonitor, times(0)).onEvent(eventResourceLoadCanceled.capture());

        Assert.assertEquals(asyncTask.getState(), AsyncTask.State.DONE);
    }

    @Test
    public void shouldHandleAsyncTaskExceptionAndDetectFailEvent() throws Exception {
        AsyncTask asyncTask = controller.loadHeavyResourceWithException(this);

        Thread.sleep(WAIT_DURATION);

        ArgumentCaptor<MyControllerImpl.ResourceLoaded> eventResourceLoaded
                = ArgumentCaptor.forClass(MyControllerImpl.ResourceLoaded.class);
        verify(eventMonitor, times(0)).onEvent(eventResourceLoaded.capture());

        ArgumentCaptor<MyControllerImpl.ResourceLoadFailed> eventResourceLoadFailed
                = ArgumentCaptor.forClass(MyControllerImpl.ResourceLoadFailed.class);
        verify(eventMonitor, times(1)).onEvent(eventResourceLoadFailed.capture());

        ArgumentCaptor<MyControllerImpl.ResourceLoadCanceled> eventResourceLoadCanceled
                = ArgumentCaptor.forClass(MyControllerImpl.ResourceLoadCanceled.class);
        verify(eventMonitor, times(0)).onEvent(eventResourceLoadCanceled.capture());

        Assert.assertEquals(asyncTask.getState(), AsyncTask.State.ERRED);
    }

    @Test
    public void shouldBeAbleToCancelAsyncActionAndDetectCancelEvent() throws Exception {
        AsyncTask asyncTask = controller.loadHeavyResourceAndCancel(this);
        asyncTask.cancel();

        Thread.sleep(WAIT_DURATION);

        ArgumentCaptor<MyControllerImpl.ResourceLoaded> eventResourceLoaded
                = ArgumentCaptor.forClass(MyControllerImpl.ResourceLoaded.class);
        verify(eventMonitor, times(0)).onEvent(eventResourceLoaded.capture());

        ArgumentCaptor<MyControllerImpl.ResourceLoadFailed> eventResourceLoadFailed
                = ArgumentCaptor.forClass(MyControllerImpl.ResourceLoadFailed.class);
        verify(eventMonitor, times(0)).onEvent(eventResourceLoadFailed.capture());

        ArgumentCaptor<MyControllerImpl.ResourceLoadCanceled> eventResourceLoadCanceled
                = ArgumentCaptor.forClass(MyControllerImpl.ResourceLoadCanceled.class);
        verify(eventMonitor, times(1)).onEvent(eventResourceLoadCanceled.capture());

        Assert.assertEquals(asyncTask.getState(), AsyncTask.State.CANCELED);
    }
}
