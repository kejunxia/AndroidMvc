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

/**
 * Object to wrap up a task that can be run asynchronously by {@link BaseControllerImpl#runAsyncTask(Object, AsyncTask)},
 * {@link BaseControllerImpl#runAsyncTask(Object, AsyncTask, AsyncExceptionHandler)},
 * {@link BaseControllerImpl#runAsyncTask(Object, java.util.concurrent.ExecutorService, AsyncTask)}
 * or {@link BaseControllerImpl#runAsyncTask(Object, java.util.concurrent.ExecutorService, AsyncTask, AsyncExceptionHandler)}
 */
public abstract class AsyncTask {
    /**
     * State of executing phases of {@link AsyncTask}
     */
    public enum State {
        /**
         * The {@link AsyncTask} hasn't started
         */
        NOT_STARTED,
        /**
         * The {@link AsyncTask} is running
         */
        RUNNING,
        /**
         * The {@link AsyncTask} has finished successfully
         */
        DONE,
        /**
         * The {@link AsyncTask} failed to finish due to errors
         */
        ERRED,
        /**
         * The {@link AsyncTask} is canceled already
         */
        CANCELED
    }

    State state;

    public AsyncTask() {
        this.state = State.NOT_STARTED;
    }

    /**
     * Gets the state of this {@link AsyncTask}
     * @return
     */
    public State getState() {
        return state;
    }

    /**
     * Change the {@link AsyncTask.State} of this {@link AsyncTask} to {@link State#CANCELED}
     * only when its current {@link AsyncTask.State} is {@link State#NOT_STARTED} or
     * {@link State#RUNNING} otherwise, there will be not effect.
     *
     * @return <b>TRUE</b> when successfully cancel this {@link AsyncTask} if current
     * {@link AsyncTask.State} is {@link State#NOT_STARTED} or {@link State#RUNNING}, otherwise
     * returns <b>FALSE</b>
     */
    public boolean cancel() {
        switch (state) {
            case NOT_STARTED:
            case RUNNING:
                state = State.CANCELED;
                return true;
            case DONE:
            case ERRED:
            case CANCELED:
            default:
                return false;
        }
    }

    /**
     * Override this method to execute the task.
     * @throws Exception
     */
    public abstract void execute() throws Exception;
}
