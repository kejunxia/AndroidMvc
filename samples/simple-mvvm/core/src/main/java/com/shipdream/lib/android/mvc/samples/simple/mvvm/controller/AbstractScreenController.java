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

package com.shipdream.lib.android.mvc.samples.simple.mvp.controller;

import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.UiView;
import com.shipdream.lib.android.mvc.samples.simple.mvp.manager.AppManager;
import com.shipdream.lib.android.mvc.samples.simple.mvp.service.ResourceService;

import javax.inject.Inject;

/**
 * Controller for fragments used as a screen - full screen page
 */
public abstract class AbstractScreenController<MODEL, VIEW extends UiView>
        extends AbstractController<MODEL, VIEW> {
    @Inject
    protected AppManager appManager;

    @Inject
    protected ResourceService resourceService;

    @Override
    public void onViewReady(Reason reason) {
        super.onViewReady(reason);

        //Refresh title when the controller's screen creates or recreates view
        appManager.setTitle(resourceService.getDefaultTitle(getClass()));
    }
}
