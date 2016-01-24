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

package com.shipdream.lib.android.mvc.samples.simple.controller.internal;

import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.manager.NavigationManager;
import com.shipdream.lib.android.mvc.samples.simple.controller.AppController;

import javax.inject.Inject;

public class AppControllerImpl extends BaseControllerImpl implements AppController{
    @Inject
    private NavigationManager navigationManager;

    @Override
    public void startApp(Object sender) {
        //Navigate to location a when app starts for the first time by navigation controller
        //here we navigate to LocationA which result in load FragmentA mapped by the
        //the method mapNavigationFragment above
        navigationManager.navigate(sender).to("LocationA");
    }

    @Override
    public Class modelType() {
        return null;
    }
}
