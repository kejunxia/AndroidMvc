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

    @Override
    public void run(final Runnable runnable) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            runnable.run();
        } else {
            //Android handler is presented, posting to the main thread on Android.
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            });
        }
    }
}
