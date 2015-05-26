package com.shipdream.lib.android.mvc.samples.simple.view;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.shipdream.lib.android.mvc.samples.simple.controller.CounterController;
import com.shipdream.lib.android.mvc.view.MvcService;

import javax.inject.Inject;

public class CountService extends MvcService{
    @Inject
    private CounterController counterController;

    private Handler handler;
    private boolean keepIncrementing = false;
    private int incrementedCount = 0;
    private static final int AUTO_FINISH_COUNT = 10;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        startAutoIncrement();

        return START_NOT_STICKY;
    }

    private void startAutoIncrement() {
        keepIncrementing = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (keepIncrementing) {
                    counterController.increment(CountService.this);
                    handler.postDelayed(this, 1000);
                    incrementedCount++;
                    if(incrementedCount  >= AUTO_FINISH_COUNT) {
                        stopAutoIncrement();
                    }
                }
            }
        }, 1000);
    }

    private void stopAutoIncrement() {
        keepIncrementing = false;
        incrementedCount = 0;
        stopSelf();
    }
}
