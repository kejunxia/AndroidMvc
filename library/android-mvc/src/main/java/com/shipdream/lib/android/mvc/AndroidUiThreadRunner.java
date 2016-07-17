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

import android.os.Handler;
import android.os.Looper;

/**
 * Implementation of {@link UiThreadRunner}
 */
public class AndroidUiThreadRunner implements UiThreadRunner {
    private Handler handler;

    @Override
    public boolean isOnUiThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    private Handler handler() {
        //Android handler is presented, posting to the main thread on Android.
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    @Override
    public void post(final Runnable runnable) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            runnable.run();
        } else {
            handler().post(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            });
        }
    }

    @Override
    public void postDelayed(final Runnable runnable, long delayMs) {
        handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }, delayMs);
    }
}
