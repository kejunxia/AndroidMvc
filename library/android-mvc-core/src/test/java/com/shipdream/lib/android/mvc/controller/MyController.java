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

import com.shipdream.lib.android.mvc.Controller;
import com.shipdream.lib.android.mvc.Task;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class MyController extends Controller {
    public interface View {
        void onResourceLoaded();

        void onResourceFailed(Exception exception);

        void onResourceCancelled();
    }

    public static final long LONG_TASK_DURATION = 100;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    public View view;

    @Override
    public Class modelType() {
        return null;
    }

    public Task.Monitor<Void> loadHeavyResourceSuccessfullyWithoutErrorHandlerWithDefaultExecutorService(final Object sender) {
        return runTask(new Task() {
            @Override
            public Void execute(Monitor monitor) throws Exception {
                Thread.sleep(LONG_TASK_DURATION);
                view.onResourceLoaded();
                return null;
            }
        });
    }

    public Task.Monitor<Void> loadHeavyResourceSuccessfullyWithoutErrorHandler(final Object sender) {
        ExecutorService executorService = mock(ExecutorService.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Callable callable = (Callable) invocation.getArguments()[0];
                callable.call();
                return null;
            }
        }).when(executorService).submit(any(Callable.class));

        return runTask(executorService, new Task<Void>() {
            @Override
            public Void execute(Monitor monitor) throws Exception {
                Thread.sleep(LONG_TASK_DURATION);
                view.onResourceLoaded();
                return null;
            }
        }, null);
    }

    public Task.Monitor<Void> loadHeavyResourceSuccessfullyWithErrorHandler(final Object sender) {
        return runTask(new Task<Void>() {
            @Override
            public Void execute(Monitor monitor) throws Exception {
                Thread.sleep(LONG_TASK_DURATION);
                view.onResourceLoaded();
                return null;
            }
        }, new Task.Callback() {
            @Override
            public void onException(Exception e) {
                view.onResourceFailed(e);
            }
        });
    }

    public Task.Monitor<Void> loadHeavyResourceWithExceptionButWithoutCustomErrorHandler(final Object sender) {
        return runTask(new Task<Void>() {
            @Override
            public Void execute(Monitor monitor) throws Exception {
                Thread.sleep(LONG_TASK_DURATION);
                throw new RuntimeException("Something went wrong");
            }
        });
    }

    public Task.Monitor<Void> loadHeavyResourceWithException(final Object sender) {
        return runTask(new Task() {
            @Override
            public Void execute(Monitor monitor) throws Exception {
                Thread.sleep(LONG_TASK_DURATION);
                throw new RuntimeException("Something went wrong");
            }
        }, new Task.Callback<Void>() {
            @Override
            public void onException(Exception e) {
                view.onResourceFailed(e);
            }
        });
    }

    public Task.Monitor<Void> loadHeavyResourceAndCancel(final Object sender) {
        Task asyncTask = new Task<Void>() {
            @Override
            public Void execute(Monitor<Void> monitor) throws Exception {
                Thread.sleep(LONG_TASK_DURATION);
                if (monitor.getState() != Monitor.State.CANCELED) {
                    view.onResourceLoaded();
                }
                return null;
            }
        };

        return runTask(asyncTask,
                new Task.Callback() {
                    @Override
                    public void onException(Exception e) {
                        view.onResourceFailed(e);
                    }

                    @Override
                    public void onCancelled(boolean interrupted) {
                        super.onCancelled(interrupted);
                        view.onResourceCancelled();
                    }
                });
    }

    public Task.Monitor<Void> loadHeavyResource(final Object sender, Task task, Task.Callback callback) {
        return runTask(task,
                callback);
    }
}
