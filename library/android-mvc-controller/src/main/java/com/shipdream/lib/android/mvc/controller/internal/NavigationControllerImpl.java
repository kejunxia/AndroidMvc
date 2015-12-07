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

package com.shipdream.lib.android.mvc.controller.internal;

import com.shipdream.lib.android.mvc.NavLocation;
import com.shipdream.lib.android.mvc.controller.NavigationController;

/**
 * Implementation of {@link NavigationController}
 */
public class NavigationControllerImpl extends BaseControllerImpl<NavigationController.Model>
        implements NavigationController {
    public boolean dumpHistoryOnLocationChange = false;

    @Override
    public Class<Model> getModelClassType() {
        return NavigationController.Model.class;
    }

    @Override
    public Navigator navigate(Object sender) {
        return new Navigator(sender, this);
    }

    @Override
    public void navigateTo(Object sender, String locationId) {
        navigate(sender).to(locationId);
    }

    @Override
    public void navigateTo(Object sender, String locationId, String clearTopToLocationId) {
        navigate(sender).to(locationId, clearTopToLocationId);
    }

    @Override
    public void navigateBack(Object sender) {
        navigate(sender).back();
    }

    @Override
    public void navigateBack(Object sender, String toLocationId) {
        navigate(sender).back(toLocationId);
    }

}
