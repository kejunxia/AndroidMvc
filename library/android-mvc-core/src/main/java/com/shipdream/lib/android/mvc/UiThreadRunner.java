package com.shipdream.lib.android.mvc;

public interface UiThreadRunner {
    /**
     * Indicates whether current thread is UI thread.
     * @return
     */
    boolean isOnUiThread();

    /**
     * Post the runnable to run on Android UI thread
     * @param runnable
     */
    void post(Runnable runnable);

    /**
     * Post the runnable to run on Android UI thread
     * @param runnable
     */
    void postDelayed(Runnable runnable, long delayMs);
}