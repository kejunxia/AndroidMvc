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

package com.shipdream.lib.android.mvc.view;

import android.app.Service;

import com.shipdream.lib.android.mvc.event.BaseEventV;

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
     * Handy method to post an event to other views directly. However, when possible, it's
     * recommended to post events from controllers to views.
     * @param event
     */
    protected void postEvent2V(BaseEventV event) {
        eventRegister.postEvent2V(event);
    }

}
