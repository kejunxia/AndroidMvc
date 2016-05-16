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

package com.shipdream.lib.android.mvp.controller.internal;

import com.shipdream.lib.android.mvp.presenter.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvp.presenter.internal.Monitor;
import com.shipdream.lib.android.mvp.presenter.internal.Task;
import com.shipdream.lib.android.mvp.event.BaseEventV;

import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;

public class MyControllerImpl2 extends BaseControllerImpl {
    public static final long LONG_TASK_DURATION = 100;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public Class modelType() {
        return null;
    }

    public Monitor runTaskCustomCallback(final Object sender, final Task task, Task.Callback callback) {
        return super.runTask(this, task, callback);
    }

    public Monitor runTaskWithoutCallback(final Object sender, final Task task) {
        return super.runTask(this, task);
    }

    public Monitor runTask(final Object sender, final Task task) {
        return runTask(this, task, new Task.Callback() {
            @Override
            public void onStarted() {
                postEvent2V(new ResourceLoadStarted(sender, task));
            }

            @Override
            public void onSuccess() {
                postEvent2V(new ResourceLoaded(sender, task));
            }

            @Override
            public void onCancelled(boolean interrupted) {
                postEvent2V(new ResourceLoadCanceled(sender, task, interrupted));
            }

            @Override
            public void onException(Exception e) {
                postEvent2V(new ResourceLoadFailed(sender, task, e));
            }
        });
    }

    public Monitor runTask(final Object sender, ExecutorService executorService, final Task task) {
        return runTask(this, executorService, task, new Task.Callback() {
            @Override
            public void onStarted() {
                postEvent2V(new ResourceLoadStarted(sender, task));
            }

            @Override
            public void onSuccess() {
                postEvent2V(new ResourceLoaded(sender, task));
            }

            @Override
            public void onCancelled(boolean interrupted) {
                postEvent2V(new ResourceLoadCanceled(sender, task, interrupted));
            }

            @Override
            public void onException(Exception e) {
                postEvent2V(new ResourceLoadFailed(sender, task, e));
            }
        });
    }

    public static class ResourceLoadStarted extends BaseEventV {
        private final Task monitor;
        public ResourceLoadStarted(Object sender, Task task) {
            super(sender);
            this.monitor = task;
        }

        public Task getTask() {
            return monitor;
        }
    }

    public static class ResourceLoaded extends BaseEventV {
        private final Task task;
        public ResourceLoaded(Object sender, Task task) {
            super(sender);
            this.task = task;
        }

        public Task getTask() {
            return task;
        }
    }

    public static class ResourceLoadCanceled extends BaseEventV {
        private final Task task;
        private final boolean interrupted;
        public ResourceLoadCanceled(Object sender, Task task, boolean interrupted) {
            super(sender);
            this.task = task;
            this.interrupted = interrupted;
        }

        public boolean isInterrupted() {
            return interrupted;
        }

        public Task getTask() {
            return task;
        }
    }

    public static class ResourceLoadFailed extends BaseEventV {
        private final Task task;
        private final Exception exception;

        public ResourceLoadFailed(Object sender, Task task, Exception exception) {
            super(sender);
            this.task = task;
            this.exception = exception;
        }

        public Exception getException() {
            return exception;
        }

        public Task getTask() {
            return task;
        }
    }
}
