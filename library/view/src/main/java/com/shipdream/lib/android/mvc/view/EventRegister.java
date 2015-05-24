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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EventRegister {
    private Logger mLogger = LoggerFactory.getLogger(getClass());
    private Object mAndroidComponent;
    private boolean mEventsRegistered = false;

    public EventRegister(Object androidComponent) {
        mAndroidComponent = androidComponent;
    }

    /**
     * Register c2v and v2v event buses. This method should be called on view's onCreate life cycle callback.
     */
    public void registerEventBuses() {
        if (!mEventsRegistered) {
            AndroidMvc.getEventBusC2V().register(mAndroidComponent);
            AndroidMvc.getEventBusV2V().register(mAndroidComponent);
            mEventsRegistered = true;
            mLogger.trace("+Event bus registered for view - '{}'.",
                    mAndroidComponent.getClass().getSimpleName());
        } else {
            mLogger.trace("!Event bus already registered for view - '{}' and its controllers.",
                    mAndroidComponent.getClass().getSimpleName());
        }
    }

    /**
     * Unregister c2v and v2v event buses. This method should be called on view's onDestroy life cycle callback.
     */
    public void unregisterEventBuses() {
        if (mEventsRegistered) {
            AndroidMvc.getEventBusC2V().unregister(mAndroidComponent);
            AndroidMvc.getEventBusV2V().unregister(mAndroidComponent);
            mEventsRegistered = false;
            mLogger.trace("-Event bus unregistered for view - '{}' and its controllers.",
                    mAndroidComponent.getClass().getSimpleName());
        } else {
            mLogger.trace("!Event bus already unregistered for view - '{}'.",
                    mAndroidComponent.getClass().getSimpleName());
        }
    }

}
