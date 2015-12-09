/*
 * Copyright 2015 Kejun Xia
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

package com.shipdream.lib.android.mvc.view;

import android.app.Service;

import com.shipdream.lib.android.mvc.event.BaseEventV2V;

/**
 * Android service can be thought as a kind of view sitting on top and driven by controller which
 * manage the state of the app.
 */
public abstract class MvcService extends Service{
    private EventRegister eventRegister;

    /**
     * Callback of creating a service
     */
    @Override
    public void onCreate() {
        super.onCreate();

        AndroidMvc.graph().inject(this);

        eventRegister = new EventRegister(this);
        eventRegister.onCreate();
        eventRegister.registerEventBuses();
    }

    /**
     * Callback of destroying a service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        eventRegister.unregisterEventBuses();
        eventRegister.onDestroy();
        AndroidMvc.graph().release(this);
    }

    /**
     * Post an event from this view to other views. Using EventBusV2V is a handy way to
     * inter-communicate among views but it's a little anti pattern. Best practice is that views
     * communicates to other views through controllers and EventBusC2V. For example, if view1 wants
     * to talk to view2, instead of sending V2V events, view1 can send a command to a controller and
     * that controller will fire an C2VEvent that will be received by view2. In this way, more
     * business logic can be wrapped into controllers rather than exposed to view1.
     *
     * <p>However, it's not absolute. If touching a controller is an overkill, sending events
     * directly through V2V channel is still an option.</p>
     * @param event
     */
    protected void postEventV2V(BaseEventV2V event) {
        AndroidMvc.getEventBusV2V().post(event);
    }
}
