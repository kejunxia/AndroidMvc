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

import com.shipdream.lib.android.mvc.BaseTest;
import com.shipdream.lib.android.mvc.MvcGraphException;
import com.shipdream.lib.android.mvc.Task;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestRunAsyncTask extends BaseTest {
    private MyController controller;
    private MyController.View view;
    private static final long WAIT_DURATION = MyController.LONG_TASK_DURATION + 100;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        executorService = Executors.newSingleThreadExecutor();

        controller = new MyController();
        graph.inject(controller);
        controller.onCreated();

        view = mock(MyController.View.class);
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
    public void shouldBeAbleToRunAsyncTaskSuccessfully() throws Exception {
        Task.Monitor asyncTask = controller.loadHeavyResourceSuccessfullyWithErrorHandler(this);

        Thread.sleep(WAIT_DURATION);

        verify(view, times(1)).onResourceLoaded();

        verify(view, times(0)).onResourceFailed(any(MvcGraphException.class));

        verify(view, times(0)).onResourceCancelled();

        Assert.assertEquals(asyncTask.getState(), Task.Monitor.State.DONE);
    }

    @Test
    public void shouldHandleAsyncTaskExceptionAndDetectFailEvent() throws Exception {
        Task.Monitor asyncTask = controller.loadHeavyResourceWithException(this);

        Thread.sleep(WAIT_DURATION);

        verify(view, times(0)).onResourceLoaded();

        verify(view, times(1)).onResourceFailed(any(MvcGraphException.class));

        verify(view, times(0)).onResourceCancelled();

        Assert.assertEquals(asyncTask.getState(), Task.Monitor.State.ERRED);
    }

    @Test
    public void shouldBeAbleToCancelAsyncActionAndDetectInterruptedEvent() throws Exception {
        Task.Monitor monitor = controller.loadHeavyResourceAndCancel(this);
        Thread.sleep(10);
        monitor.cancel(true);

        Thread.sleep(WAIT_DURATION);

        verify(view, times(0)).onResourceLoaded();

        verify(view, times(0)).onResourceFailed(any(MvcGraphException.class));

        verify(view, times(1)).onResourceCancelled();

        Assert.assertEquals(monitor.getState(), Task.Monitor.State.INTERRUPTED);
    }

    @Test
    public void shouldBeAbleToCancelAsyncActionAndDetectCancelEvent() throws Exception {
        Task.Monitor monitor = controller.loadHeavyResourceAndCancel(this);
        Thread.sleep(10);
        monitor.cancel(false);

        Thread.sleep(WAIT_DURATION);

        verify(view, times(0)).onResourceLoaded();

        verify(view, times(0)).onResourceFailed(any(MvcGraphException.class));

        verify(view, times(1)).onResourceCancelled();

        Assert.assertEquals(monitor.getState(), Task.Monitor.State.CANCELED);
    }

    @Test
    public void should_catch_exception_during_running_async_task() throws Exception {
        final Task.Callback callback = mock(Task.Callback.class);
        Task.Monitor monitor1 = controller.loadHeavyResource(this, new Task() {
            @Override
            public Object execute(Monitor monitor) throws Exception {
                Thread.sleep(50);
                throw new RuntimeException();
            }
        }, new Task.Callback() {
            @Override
            public void onStarted() {
                super.onStarted();
                callback.onStarted();
            }

            @Override
            public void onStarted(Task.Monitor monitor) {
                super.onStarted(monitor);
                callback.onStarted(monitor);
            }

            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);
                callback.onSuccess(o);
            }

            @Override
            public void onCancelled(boolean interrupted) {
                super.onCancelled(interrupted);
                callback.onCancelled(interrupted);
            }

            @Override
            public void onException(Exception e) throws Exception {
                callback.onException(e);
            }

            @Override
            public void onFinally() {
                super.onFinally();
                callback.onFinally();
            }
        });

        Thread.sleep(100);

        verify(callback, times(1)).onStarted(monitor1);
        verify(callback, times(1)).onException(any(Exception.class));
        verify(callback, times(0)).onSuccess(anyObject());
        verify(callback, times(0)).onCancelled(anyBoolean());
        verify(callback, times(1)).onFinally();
    }

    @Test
    public void should_be_able_to_cancel_a_task_before_it_starts() throws Exception {
        Task.Callback callback = mock(Task.Callback.class);
        Task.Monitor monitor1 = controller.loadHeavyResource(this, new Task() {
            @Override
            public Object execute(Monitor monitor) throws Exception {
                Thread.sleep(WAIT_DURATION);
                return null;
            }
        }, callback);

        Task.Callback callback2 = mock(Task.Callback.class);
        Task.Monitor monitor2 = controller.loadHeavyResource(this, new Task() {
            @Override
            public Object execute(Monitor monitor) throws Exception {
                return null;
            }
        }, callback2);
        monitor2.cancel(true);

        Thread.sleep(WAIT_DURATION + 100);

        verify(callback, times(1)).onStarted(monitor1);
        verify(callback, times(0)).onException(any(Exception.class));
        verify(callback, times(1)).onSuccess(anyObject());
        verify(callback, times(0)).onCancelled(anyBoolean());
        verify(callback, times(1)).onFinally();

        verify(callback2, times(0)).onStarted(monitor2);
        verify(callback2, times(0)).onException(any(Exception.class));
        verify(callback2, times(0)).onSuccess(anyObject());
        verify(callback2, times(1)).onCancelled(anyBoolean());
        verify(callback2, times(1)).onFinally();

        Assert.assertEquals(monitor1.getState(), Task.Monitor.State.DONE);
        Assert.assertEquals(monitor2.getState(), Task.Monitor.State.CANCELED);

        Assert.assertFalse(monitor1.cancel(true));
        Assert.assertFalse(monitor1.cancel(false));
        Assert.assertFalse(monitor2.cancel(true));
        Assert.assertFalse(monitor2.cancel(false));
    }
}
