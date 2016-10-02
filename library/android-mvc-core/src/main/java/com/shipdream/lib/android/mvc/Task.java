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

import java.util.concurrent.Future;

/**
 * Task to execute a block of code
 */
public interface Task<RESULT> {
    /**
     * Monitor is used to track and cancel the task
     */
    class Monitor<RESULT> {
        /**
         * State of executing phases of {@link Task}
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

        private final Callback<RESULT> callback;
        private final UiThreadRunner uiThreadRunner;
        private Future future;
        private State state;
        private Task<RESULT> task;

        /**
         * Constructor
         * @param task The task being monitored
         * @param uiThreadRunner The runner runs runnable on ui thread
         * @param callback The callback for the execution of the task. It can be null.
         * @param uiThreadRunner
         */
        public Monitor(Task<RESULT> task, UiThreadRunner uiThreadRunner, Callback<RESULT> callback) {
            this.uiThreadRunner = uiThreadRunner;
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
         * Gets the state of this {@link Task}
         * @return
         */
        public synchronized State getState() {
            return state;
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
        public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
            switch (state) {
                case NOT_STARTED:
                    state = State.CANCELED;
                    if (callback != null) {
                        callback.onCancelled(false);
                        callback.onFinally();
                    }
                    return true;
                case STARTED:
                    boolean cancelled = future.cancel(mayInterruptIfRunning);
                    if (cancelled) {
                        if (mayInterruptIfRunning) {
                            state = State.INTERRUPTED;
                        } else {
                            state = State.CANCELED;
                        }
                    }

                    if (callback != null) {
                        uiThreadRunner.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onCancelled(mayInterruptIfRunning);
                                callback.onFinally();
                            }
                        });
                    }
                    return cancelled;
                case DONE:
                case ERRED:
                case CANCELED:
                case INTERRUPTED:
                default:
                    return false;
            }
        }
    }
    /**
     * The callback for the execution of a {@link Task}
     */
    abstract class Callback<RESULT> {
        /**
         * Called when the execution of the task starts
         * @deprecated
         */
        public void onStarted() {}

        /**
         * Called when the execution of the task starts
         * @param monitor The monitor to watch this task. The task can be cancelled by {@link Monitor#cancel(boolean)}
         */
        public void onStarted(Monitor monitor) {}

        /**
         * Called when the execution of the task completes successfully
         * @param result The result of the execution
         */
        public void onSuccess(RESULT result){}

        /**
         * Called when the execution of the task is cancelled
         * @param interrupted true when the task is cancelled while the task is running.
         *                    false when the task is cancelled before it starts running
         */
        public void onCancelled(boolean interrupted){}

        /**
         * Called when the execution of the task encounters exceptions. Note that an
         * {@link InterruptedException} caused by cancelling won't trigger this callback but
         * {@link #onCancelled(boolean)} with argument equals true
         * @param e The exception to handle
         * @throws Exception The exception by default will be thrown out otherwise override this
         * method to handle it and <b>DO NOT call super.onException(e)</b>
         */
        public void onException(Exception e) throws Exception {
            throw e;
        }

        /**
         * Called when the task has started and runs into {@link #onSuccess(Object)} ()},
         * {@link #onCancelled(boolean)} or {@link #onException(Exception)}
         */
        public void onFinally() {}
    }

    /**
     * Override this method to define what this task does.
     * @param monitor The monitor to track the execution of this task
     * @throws Exception exception occurring during the execution
     */
    RESULT execute(Monitor<RESULT> monitor) throws Exception;
}
