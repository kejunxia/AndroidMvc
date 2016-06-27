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

package com.shipdream.lib.android.mvc.view.viewpager;

import android.os.Bundle;
import android.view.View;

import com.shipdream.lib.android.mvc.FragmentController;
import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitor;

public abstract class BaseTabFragment<C extends FragmentController> extends MvcFragment<C> {
    protected abstract LifeCycleMonitor getLifeCycleMonitor();

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        getLifeCycleMonitor().onCreateView(view, savedInstanceState);
        getLifeCycleMonitor().onViewCreated(view, savedInstanceState);
        super.onViewReady(view, savedInstanceState, reason);
        getLifeCycleMonitor().onViewReady(view, savedInstanceState, reason);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifeCycleMonitor().onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLifeCycleMonitor().onResume();
    }

    @Override
    protected void onReturnForeground() {
        super.onReturnForeground();
        getLifeCycleMonitor().onReturnForeground();
    }

    @Override
    protected void onPushToBackStack() {
        super.onPushToBackStack();
        getLifeCycleMonitor().onPushToBackStack();
    }

    @Override
    protected void onPopAway() {
        super.onPopAway();
        getLifeCycleMonitor().onPopAway();
    }

    @Override
    protected void onPoppedOutToFront() {
        super.onPoppedOutToFront();
        getLifeCycleMonitor().onPoppedOutToFront();
    }

    @Override
    protected void onOrientationChanged(int lastOrientation, int currentOrientation) {
        super.onOrientationChanged(lastOrientation, currentOrientation);
        getLifeCycleMonitor().onOrientationChanged(lastOrientation, currentOrientation);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLifeCycleMonitor().onDestroyView();
    }

    @Override
    public void onDestroy() {
        getLifeCycleMonitor().onDestroy();
        super.onDestroy();
    }
}
