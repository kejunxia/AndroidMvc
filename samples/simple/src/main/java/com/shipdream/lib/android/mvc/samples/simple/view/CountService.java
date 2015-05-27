package com.shipdream.lib.android.mvc.samples.simple.view;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.shipdream.lib.android.mvc.samples.simple.R;
import com.shipdream.lib.android.mvc.samples.simple.controller.CounterController;
import com.shipdream.lib.android.mvc.view.MvcService;

import javax.inject.Inject;

public class CountService extends MvcService{
    private class AutoCounter implements Runnable {
        private int count = 0;
        private boolean canceled = false;

        @Override
        public void run() {
            if (!canceled) {
                if (count ++ <= AUTO_FINISH_COUNT) {
                    counterController.increment(this);
                    handler.postDelayed(this, 1000);
                } else {
                    stopAutoIncrement();
                    stopSelf();
                }
            }
        }

        private void cancel() {
            canceled = true;
        }
    }

    @Inject
    private CounterController counterController;

    private final static int NOTIFICATION_ID = 0;
    private Handler handler;
    private static final int AUTO_FINISH_COUNT = 10;
    private AutoCounter autoCounter;
    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startAutoIncrement();
        return START_NOT_STICKY;
    }

    /**
     * Update notification when receiving count updated event. Note the notification is updated
     * driven by the same event that is updating FragmentA, FragmentA_Sub and FragmentB.
     * @param event The event. This event is also monitored by FragmentA, FragmentA_Sub and FragmentB
     */
    private void onEvent(CounterController.EventC2V.OnCounterUpdated event) {
        updateNotification(event.getCount());
    }

    private void updateNotification(int currentCount) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Count to 10")
                .setContentText("Current count: " + currentCount)
                .setSmallIcon(R.drawable.ic_notification_auto_count)
                .setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void startAutoIncrement() {
        stopAutoIncrement();
        updateNotification(counterController.getModel().getCount());
        autoCounter = new AutoCounter();
        autoCounter.run();
    }

    private void stopAutoIncrement() {
        if (autoCounter != null) {
            autoCounter.cancel();
        }
    }
}
