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
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusV;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

class EventRegister {
    private static UiThreadRunner uiThreadRunner;

    @Inject
    @EventBusV
    private EventBus eventBusV;

    static {
        uiThreadRunner = new AndroidUiThreadRunner();
        Mvc.graph().uiThreadRunner = uiThreadRunner;

        /**
         * All mvc components will use this class to register events. So the static configuration
         * is set in this class static block.
         */
        try {
            Mvc.graph().getRootComponent().unregister(UiThreadRunner.class, null);
            Mvc.graph().getRootComponent().register(new Object() {
                @Provides
                public UiThreadRunner uiThreadRunner() {
                    return uiThreadRunner;
                }
            });
        } catch (ProvideException e) {
            throw new RuntimeException(e);
        } catch (ProviderConflictException e) {
            throw new RuntimeException(e);
        } catch (ProviderMissingException e) {
            throw new RuntimeException(e);
        }
    }

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Object androidComponent;
    private boolean eventsRegistered = false;

    EventRegister(Object androidComponent) {
        this.androidComponent = androidComponent;
    }

    /**
     * Register event bus for views.
     */
    void registerEventBuses() {
        if (!eventsRegistered) {
            Mvc.graph().inject(this);
            eventBusV.register(androidComponent);
            eventsRegistered = true;
            logger.trace("+Event2V bus registered for view - '{}'.",
                    androidComponent.getClass().getSimpleName());
        } else {
            logger.trace("!Event2V bus already registered for view - '{}' and its controllers.",
                    androidComponent.getClass().getSimpleName());
        }
    }

    /**
     * Unregister event bus for views.
     */
    void unregisterEventBuses() {
        if (eventsRegistered) {
            eventBusV.unregister(androidComponent);
            eventsRegistered = false;
            logger.trace("-Event2V bus unregistered for view - '{}' and its controllers.",
                    androidComponent.getClass().getSimpleName());
            Mvc.graph().release(this);
        } else {
            logger.trace("!Event2V bus already unregistered for view - '{}'.",
                    androidComponent.getClass().getSimpleName());
        }
    }

    void postEvent2V(final Object event) {
        eventBusV.post(event);
    }

}
