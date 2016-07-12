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

package com.shipdream.lib.android.mvc.view.injection;

import android.os.Bundle;
import android.view.View;

import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitor;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitorC;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerB;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerC;

import javax.inject.Inject;

public class FragmentC extends FragmentInjection {
    @Inject
    private ControllerB presenterB;

    //TODO: should be removed if designed correctly
    @Inject
    private ControllerC presenterC;

    @Inject
    private LifeCycleMonitorC lifeCycleMonitorC;

    @Override
    protected void setUpData() {
        presenterB.addTag("Added by " + getClass().getSimpleName());
        presenterC.addTag("Added by " + getClass().getSimpleName());
    }

    @Override
    protected LifeCycleMonitor getLifeCycleMonitor() {
        return lifeCycleMonitorC;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);
        displayTags(textViewB, presenterB.getTags());
        displayTags(textViewC, presenterC.getTags());
    }

    @Override
    protected Class getControllerClass() {
        return null;
    }

    @Override
    public void update() {

    }
}
