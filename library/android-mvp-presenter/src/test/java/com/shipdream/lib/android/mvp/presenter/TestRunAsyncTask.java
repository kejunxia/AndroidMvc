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

package com.shipdream.lib.android.mvp.presenter;

import com.shipdream.lib.android.mvp.Mvp;
import com.shipdream.lib.android.mvp.Task;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.concurrent.Executors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestRunAsyncTask extends BaseTest {
    private MyPresenter controller;
    private MyPresenter.View view;
    private static final long WAIT_DURATION = MyPresenter.LONG_TASK_DURATION + 100;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        executorService = Executors.newSingleThreadExecutor();

        controller = new MyPresenter();
        graph.inject(controller);
        controller.onConstruct();

        view = mock(MyPresenter.View.class);
        eventBusV.register(view);

        controller.view = view;
    }

    @Test
    public void should_be_able_to_run_async_task_without_error_handler_with_default_executorService() throws Exception {
        Task.Monitor asyncTask = controller.loadHeavyResourceSuccessfullyWithoutErrorHandlerWithDefaultExecutorService(this);

        Thread.sleep(WAIT_DURATION);

        verify(view, times(1)).onResourceLoaded();

        Assert.assertEquals(asyncTask.getState(), Task.Monitor.State.DONE);
    }

    @Test
    public void should_be_able_to_run_async_task_without_error_handler() throws Exception {
        Task.Monitor asyncTask = controller.loadHeavyResourceSuccessfullyWithoutErrorHandler(this);

        Thread.sleep(WAIT_DURATION);

        verify(view, times(1)).onResourceLoaded();

        Assert.assertEquals(asyncTask.getState(), Task.Monitor.State.DONE);
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
        Task.Monitor asyncTask = controller.loadHeavyResourceSuccessfullyWithErrorHandler(this);

        Thread.sleep(WAIT_DURATION);

        verify(view, times(1)).onResourceLoaded();

        verify(view, times(0)).onResourceFailed(any(Mvp.Exception.class));

        verify(view, times(0)).onResourceCancelled();

        Assert.assertEquals(asyncTask.getState(), Task.Monitor.State.DONE);
    }

    @Test
    public void shouldHandleAsyncTaskExceptionAndDetectFailEvent() throws Exception {
        Task.Monitor asyncTask = controller.loadHeavyResourceWithException(this);

        Thread.sleep(WAIT_DURATION);

        verify(view, times(0)).onResourceLoaded();

        verify(view, times(1)).onResourceFailed(any(Mvp.Exception.class));

        verify(view, times(0)).onResourceCancelled();

        Assert.assertEquals(asyncTask.getState(), Task.Monitor.State.ERRED);
    }

    @Test
    public void shouldBeAbleToCancelAsyncActionAndDetectCancelEvent() throws Exception {
        Task.Monitor monitor = controller.loadHeavyResourceAndCancel(this);
        monitor.cancel(true);

        Thread.sleep(WAIT_DURATION);

        verify(view, times(0)).onResourceLoaded();

        verify(view, times(0)).onResourceFailed(any(Mvp.Exception.class));

        verify(view, times(1)).onResourceCancelled();

        Assert.assertEquals(monitor.getState(), Task.Monitor.State.CANCELED);
    }
}
