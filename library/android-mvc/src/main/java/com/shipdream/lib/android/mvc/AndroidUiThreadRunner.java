package com.shipdream.lib.android.mvc;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by kejun on 28/06/2016.
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
