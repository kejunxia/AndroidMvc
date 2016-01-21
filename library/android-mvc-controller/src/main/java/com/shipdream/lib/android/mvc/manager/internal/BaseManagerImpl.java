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

package com.shipdream.lib.android.mvc.manager.internal;

import com.shipdream.lib.android.mvc.MvcBean;
import com.shipdream.lib.android.mvc.event.BaseEventC;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC;
import com.shipdream.lib.android.mvc.manager.BaseManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Abstract manager with model that needs to be managed. A manager can be shared by multiple
 * controllers. A LoginManager is an good example that manages the state of logged in user.
 *
 * <p>
 * Managers should only be serving controllers and not visible to views. Managers can post events
 * to controllers to notify the state change in the shared manager.
 * </p>
 */
public abstract class BaseManagerImpl<MODEL> extends MvcBean<MODEL> implements BaseManager<MODEL> {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    @EventBusC
    private EventBus eventBus2C;

    /**
     * Bind model to this manager
     * @param sender Who wants to bind it
     * @param model The model to bind to this manager. CANNOT be NULL otherwise a runtime
     */
    @Override
    public void bindModel(Object sender, MODEL model) {
        super.bindModel(model);
    }

    /**
     * Post an event to other controllers. Event will be posted on the same thread as the caller.
     *
     * @param event event to controllers
     */
    protected void postControllerEvent(final BaseEventC event) {
        if (eventBus2C != null) {
            eventBus2C.post(event);
        } else {
            logger.warn("Trying to post event {} to EventBusC which is null", event.getClass().getName());
        }
    }
}
