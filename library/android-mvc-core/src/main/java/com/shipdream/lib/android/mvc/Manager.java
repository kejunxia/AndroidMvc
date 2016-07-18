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

package com.shipdream.lib.android.mvc;


import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Abstract manager to be extended to manage shared presenter logic and data. The manager will listen
 * to {@link EventBusC}
 * @param <MODEL> The model the manager holds. On Android, models will be automatically
 *               serialized and deserialized by fragments when the manager is injected into a
 *               fragment as a class's field directly or indirectly(held by presenter's field).
 */
public abstract class Manager<MODEL> extends Bean<MODEL> {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    @EventBusC
    private EventBus eventBus2C;

    /**
     * Bind model to this manager
     * @param sender Who wants to bind it
     * @param model The model to bind to this manager. CANNOT be NULL otherwise a runtime
     */
    public void bindModel(Object sender, MODEL model) {
        super.bindModel(model);
    }

    public void onCreated() {
        super.onCreated();
        eventBus2C.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        eventBus2C.unregister(this);
    }
    /**
     * Post an event to controllers or other managers on {@link EventBusC}. The event will be posted
     * on to the same thread as the caller.
     *
     * @param event event to controllers
     */
    protected void postEvent2C(final Object event) {
        if (eventBus2C != null) {
            eventBus2C.post(event);
        } else {
            logger.warn("Trying to post event {} to EventBusC which is null", event.getClass().getName());
        }
    }
}
