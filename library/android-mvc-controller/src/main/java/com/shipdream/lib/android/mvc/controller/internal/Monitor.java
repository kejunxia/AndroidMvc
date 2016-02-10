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

package com.shipdream.lib.android.mvc.controller.internal;

import java.util.concurrent.Future;

/**
 * Monitor is used to track and cancel the task
 *
 * @since 2.2.0
 */
public class Monitor {
    /**
     * State of executing phases of {@link AsyncTask}
     */
    public enum State {
        /**
         * The {@link Monitor} hasn't started
         */
        NOT_STARTED,
        /**
         * The {@link Monitor} has started. After this state and before {@link #DONE}, {@link #ERRED},
         * {@link #CANCELED} or {@link #INTERRUPTED} the task is in running state.
         */
        STARTED,
        /**
         * The {@link Monitor} has finished successfully
         */
        DONE,
        /**
         * The {@link Monitor} failed to finish due to errors
         */
        ERRED,
        /**
         * The {@link Monitor} is cancelled before it starts
         */
        CANCELED,
        /**
         * The {@link Monitor} is cancelled after it starts and before finishes
         */
        INTERRUPTED
    }

    private final Task.Callback callback;
    private Future future;
    private State state;
    private Task task;

    /**
     * Constructor
     * @param task The task being monitored
     * @param callback The callback for the execution of the task. It can be null.
     */
    public Monitor(Task task, Task.Callback callback) {
        this.state = State.NOT_STARTED;
        this.task = task;
        this.callback = callback;
    }

    synchronized void setState(State state) {
        this.state = state;
    }

    synchronized void setFuture(Future future) {
        this.future = future;
    }

    /**
     * Gets the state of this {@link AsyncTask}
     * @return
     */
    public synchronized State getState() {
        return state;
    }

    /**
     * The task this monitor is monitoring
     * @return The task
     */
    public Task getTask() {
        return task;
    }

    /**
     * Attempts to cancel execution of this task.  This attempt will
     * fail if the task has already completed, has already been cancelled,
     * or could not be cancelled for some other reason. If successful,
     * and this task has not started when <tt>cancel</tt> is called,
     * this task should never run.  If the task has already started,
     * then the <tt>mayInterruptIfRunning</tt> parameter determines
     * whether the thread executing this task should be interrupted in
     * an attempt to stop the task.
     *
     * <p>
     * If this method is called when {@link #getState()} is {@link State#NOT_STARTED}
     * subsequent calls to {@link #getState()} will always return <tt>{@link State#CANCELED}</tt>.
     * </p>
     *
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
     * task should be interrupted; otherwise, in-progress tasks are allowed
     * to complete
     * @return <tt>false</tt> if the task could not be cancelled,
     * typically because it has already completed normally;
     * <tt>true</tt> otherwise
     */
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        switch (state) {
            case NOT_STARTED:
                state = State.CANCELED;
                if (callback != null) {
                    callback.onCancelled(false);
                }
                return true;
            case STARTED:
                if (future != null) {
                    boolean cancelled = future.cancel(mayInterruptIfRunning);
                    if (cancelled) {
                        state = State.INTERRUPTED;
                        if (callback != null) {
                            callback.onCancelled(true);
                        }
                    }
                    return cancelled;
                } else {
                    return false;
                }
            case DONE:
            case ERRED:
            case CANCELED:
            case INTERRUPTED:
            default:
                return false;
        }
    }
}
