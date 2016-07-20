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

package com.shipdream.lib.android.mvc.samples.simple.mvp.view.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.shipdream.lib.android.mvc.MvcService;
import com.shipdream.lib.android.mvc.samples.simple.mvp.MainActivity;
import com.shipdream.lib.android.mvc.samples.simple.mvp.R;
import com.shipdream.lib.android.mvc.samples.simple.mvp.controller.CounterServiceController;

public class CountService extends MvcService<CounterServiceController> implements CounterServiceController.View{
    private final static int NOTIFICATION_ID = 0;

    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected Class<CounterServiceController> getControllerClass() {
        return CounterServiceController.class;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        controller.stopAutoIncrement();
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        controller.startAutoIncrement();
        return START_NOT_STICKY;
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

    @Override
    public void counterFinished() {
        stopSelf();
    }

    @Override
    public void update() {
        updateNotification(controller.getCount());
    }
}
