package com.shipdream.lib.android.mvc;

public interface UiThreadRunner {
    /**
     * Indicates whether current thread is UI thread.
     * @return
     */
    boolean isOnUiThread();
    /**
     * Run the runnable on Android UI thread
     * @param runnable
     */
    void run(Runnable runnable);
}