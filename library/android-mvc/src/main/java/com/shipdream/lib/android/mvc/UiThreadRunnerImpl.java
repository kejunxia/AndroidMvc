package com.shipdream.lib.android.mvc;

import android.os.Handler;
import android.os.Looper;

/**
 * Internal use.
 */
public class UiThreadRunnerImpl implements UiThreadRunner {
    private Handler handler;

    /**
     * Internal use.
     */
    public static void init() {
        MvcGraph.uiThreadRunner = new UiThreadRunnerImpl();
    }

    @Override
    public void runOnUiThread(Runnable runnable) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            runnable.run();
        } else {
            //Android handler is presented, posting to the main thread on Android.
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler.post(runnable);
        }
    }
}
