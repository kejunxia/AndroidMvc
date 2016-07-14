package com.shipdream.lib.android.mvc;

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