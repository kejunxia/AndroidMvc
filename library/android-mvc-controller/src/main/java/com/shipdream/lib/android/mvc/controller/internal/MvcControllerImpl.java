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

package com.shipdream.lib.android.mvc.controller.internal;

import com.shipdream.lib.android.mvc.NavLocation;
import com.shipdream.lib.android.mvc.controller.MvcController;
import com.shipdream.lib.android.mvc.manager.NavigationManager;
import com.shipdream.lib.android.mvc.manager.internal.Navigator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class MvcControllerImpl extends BaseControllerImpl implements MvcController{
    Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private NavigationManager navigationManager;

    @Override
    public Class modelType() {
        return null;
    }

    @Override
    public Navigator navigate(Object sender) {
        return navigationManager.navigate(sender);
    }

    @Override
    public NavLocation getCurrentLocation() {
        return navigationManager.getModel().getCurrentLocation();
    }

    /**
     * Handle the forward navigation event call back. Relay the event to views
     *
     * @param event The forward navigation event
     */
    private void onEvent(final NavigationManager.EventC2C.OnLocationForward event) {
        postEvent2V(new EventC2V.OnLocationForward(
                event.getSender(),
                event.getLastValue(),
                event.getCurrentValue(),
                event.isClearHistory(),
                event.getLocationWhereHistoryClearedUpTo(),
                event.getNavigator()));
    }

    /**
     * Handle the backward navigation event call back. Relay the event to views
     *
     * @param event The backward navigation event
     */
    private void onEvent(final NavigationManager.EventC2C.OnLocationBack event) {
        postEvent2V(new EventC2V.OnLocationBack(
                event.getSender(),
                event.getLastValue(),
                event.getCurrentValue(),
                event.isFastRewind(),
                event.getNavigator()));
    }
}
