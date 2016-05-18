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

package com.shipdream.lib.android.mvp;

/**
 * Task to execute a block of code
 * @since 2.2.0
 */
public interface Task {
    /**
     * The callback for the execution of a {@link Task}
     */
    abstract class Callback {
        /**
         * Called when the execution of the task starts
         */
        public void onStarted() {}

        /**
         * Called when the execution of the task completes successfully
         */
        public void onSuccess(){}

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
         * @param e The exception
         */
        public void onException(Exception e){}
    }

    /**
     * Override this method to define what this task does.
     * @param monitor The monitor to track the execution of this task
     * @throws Exception exception occurring during the execution
     */
    void execute(Monitor monitor) throws Exception;
}
