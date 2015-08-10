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

package com.shipdream.lib.android.mvc.controller.internal;

import com.shipdream.lib.android.mvc.event.BaseEventC2V;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class MyControllerImpl extends BaseControllerImpl {
    public static final long LONG_TASK_DURATION = 100;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public Class getModelClassType() {
        return null;
    }

    public AsyncTask loadHeavyResourceSuccessfullyWithoutErrorHandlerWithDefaultExecutorService(final Object sender) {
        return runAsyncTask(this, new AsyncTask() {
            @Override
            public void execute() throws Exception {
                Thread.sleep(LONG_TASK_DURATION);
                postC2VEvent(new ResourceLoaded(sender));
            }
        });
    }

    public AsyncTask loadHeavyResourceSuccessfullyWithoutErrorHandler(final Object sender) {
        ExecutorService executorService = mock(ExecutorService.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = (Runnable) invocation.getArguments()[0];
                runnable.run();
                return null;
            }
        }).when(executorService).submit(any(Runnable.class));

        return runAsyncTask(this, executorService, new AsyncTask() {
            @Override
            public void execute() throws Exception {
                Thread.sleep(LONG_TASK_DURATION);
                postC2VEvent(new ResourceLoaded(sender));
            }
        });
    }

    public AsyncTask loadHeavyResourceSuccessfullyWithErrorHandler(final Object sender) {
        return runAsyncTask(this, new AsyncTask() {
            @Override
            public void execute() throws Exception {
                Thread.sleep(LONG_TASK_DURATION);
                postC2VEvent(new ResourceLoaded(sender));
            }
        }, new AsyncExceptionHandler() {
            @Override
            public void handleException(Exception exception) {
                postC2VEvent(new ResourceLoadFailed(sender, exception));
            }
        });
    }

    public AsyncTask loadHeavyResourceWithExceptionButWithoutCustomErrorHandler(final Object sender) {
        return runAsyncTask(this, new AsyncTask() {
            @Override
            public void execute() throws Exception {
                Thread.sleep(LONG_TASK_DURATION);
                throw new RuntimeException("Something went wrong");
            }
        });
    }
    public AsyncTask loadHeavyResourceWithException(final Object sender) {
        return runAsyncTask(this, new AsyncTask() {
            @Override
            public void execute() throws Exception {
                Thread.sleep(LONG_TASK_DURATION);
                throw new RuntimeException("Something went wrong");
            }
        }, new AsyncExceptionHandler() {
            @Override
            public void handleException(Exception exception) {
                postC2VEvent(new ResourceLoadFailed(sender, exception));
            }
        });
    }


    public AsyncTask loadHeavyResourceAndCancel(final Object sender) {
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            public void execute() throws Exception {
                Thread.sleep(LONG_TASK_DURATION);
                if (getState() == State.CANCELED) {
                    postC2VEvent(new ResourceLoadCanceled(sender));
                } else {
                    postC2VEvent(new ResourceLoaded(sender));
                }
            }
        };

        runAsyncTask(this, asyncTask,
                new AsyncExceptionHandler() {
                    @Override
                    public void handleException(Exception exception) {
                        postC2VEvent(new ResourceLoadFailed(sender, exception));
                    }
                });

        return asyncTask;
    }

    public static class ResourceLoaded extends BaseEventC2V {
        public ResourceLoaded(Object sender) {
            super(sender);
        }
    }

    public static class ResourceLoadCanceled extends BaseEventC2V {
        public ResourceLoadCanceled(Object sender) {
            super(sender);
        }
    }

    public static class ResourceLoadFailed extends BaseEventC2V {
        private final Exception exception;

        public ResourceLoadFailed(Object sender, Exception exception) {
            super(sender);
            this.exception = exception;
        }

        public Exception getException() {
            return exception;
        }
    }
}
