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

import com.shipdream.lib.android.mvc.event.BaseEventV2V;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC2V;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

class EventRegister {
    @Inject
    @EventBusC2V
    private EventBus eventBusC2V;

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Object androidComponent;
    private boolean eventsRegistered = false;

    EventRegister(Object androidComponent) {
        this.androidComponent = androidComponent;
    }

    /**
     * Register c2v and v2v event buses.
     */
    void registerEventBuses() {
        if (!eventsRegistered) {
            eventBusC2V.register(androidComponent);
            AndroidMvc.getEventBusV2V().register(androidComponent);
            eventsRegistered = true;
            logger.trace("+Event bus registered for view - '{}'.",
                    androidComponent.getClass().getSimpleName());
        } else {
            logger.trace("!Event bus already registered for view - '{}' and its controllers.",
                    androidComponent.getClass().getSimpleName());
        }
    }

    /**
     * Unregister c2v and v2v event buses.
     */
    void unregisterEventBuses() {
        if (eventsRegistered) {
            eventBusC2V.unregister(androidComponent);
            AndroidMvc.getEventBusV2V().unregister(androidComponent);
            eventsRegistered = false;
            logger.trace("-Event bus unregistered for view - '{}' and its controllers.",
                    androidComponent.getClass().getSimpleName());
        } else {
            logger.trace("!Event bus already unregistered for view - '{}'.",
                    androidComponent.getClass().getSimpleName());
        }
    }

    void onCreate() {
        AndroidMvc.graph().inject(this);
    }

    void onDestroy() {
        AndroidMvc.graph().release(this);
    }
}
