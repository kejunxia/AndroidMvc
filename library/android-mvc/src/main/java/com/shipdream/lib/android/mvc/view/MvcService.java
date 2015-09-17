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
        eventRegister.registerEventBuses();
    }

    /**
     * Callback of destroying a service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        eventRegister.unregisterEventBuses();
        AndroidMvc.graph().release(this);
    }

    /**
     * Post an event from this view to other views
     * @param event
     */
    protected void postEventV2V(BaseEventV2V event) {
        AndroidMvc.getEventBusC2V().post(event);
    }
}
