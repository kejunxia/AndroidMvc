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

/**
 * UiThreadRunner abstracts methods able to post actions to Android UI thread.
 */
public interface UiThreadRunner {
    /**
     * Indicates whether current thread is UI thread.
     * @return
     */
    boolean isOnUiThread();

    /**
     * Post the runnable to run on Android UI thread. In Android app, when the caller is not
     * currently on UI thread, it will be posted to the UI thread to be run in next main loop.
     * Otherwise, it will be run immediately.
     * @param runnable
     */
    void post(Runnable runnable);

    /**
     * Post the runnable to run on Android UI thread with the given delay.
     * @param runnable
     */
    void postDelayed(Runnable runnable, long delayMs);
}