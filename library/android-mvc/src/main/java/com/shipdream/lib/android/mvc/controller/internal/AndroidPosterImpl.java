package com.shipdream.lib.android.mvc.controller.internal;

import android.os.Handler;
import android.os.Looper;

import com.shipdream.lib.android.mvc.event.BaseEventC2V;
import com.shipdream.lib.android.mvc.event.bus.EventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AndroidPosterImpl implements BaseControllerImpl.AndroidPoster {
    private static Handler handler;
    private static Logger logger = LoggerFactory.getLogger(AndroidPosterImpl.class);

    /**
     * Internal use. Don't call me from your app.
     */
    public static void init() {
        BaseControllerImpl.androidPoster = new AndroidPosterImpl();
    }

    @Override
    public void post(final EventBus eventBusC2V, final BaseEventC2V eventC2V) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            doPost(eventBusC2V, eventC2V);
        } else {
            //Android handler is presented, posting to the main thread on Android.
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    doPost(eventBusC2V, eventC2V);
                }
            });
        }
    }

    private static void doPost(EventBus eventBusC2V, BaseEventC2V eventC2V) {
        if (eventBusC2V != null) {
            eventBusC2V.post(eventC2V);
        } else {
            logger.warn("Trying to post event but EventBusC2V is null");
        }
    }
}
