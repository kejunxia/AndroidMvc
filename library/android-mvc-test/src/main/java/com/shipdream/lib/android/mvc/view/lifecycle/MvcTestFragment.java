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

package com.shipdream.lib.android.mvc.view.lifecycle;

import android.os.Bundle;
import android.view.View;

import com.shipdream.lib.android.mvc.FragmentController;
import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitor;
import com.shipdream.lib.android.mvc.view.test.R;

import javax.inject.Inject;

public class MvcTestFragment extends MvcFragment<MvcTestFragment.Controller> {
    public static class Controller extends FragmentController{
        @Override
        public Class modelType() {
            return null;
        }
    }

    @Inject
    private LifeCycleMonitor lifeCycleMonitor;

    @Override
    protected Class<Controller> getControllerClass() {
        return Controller.class;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_mvc_test;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifeCycleMonitor.onCreate(savedInstanceState);
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        lifeCycleMonitor.onCreateView(view, savedInstanceState);
        lifeCycleMonitor.onViewCreated(view, savedInstanceState);
        super.onViewReady(view, savedInstanceState, reason);
        lifeCycleMonitor.onViewReady(view, savedInstanceState, reason);
    }

    @Override
    public void onResume() {
        super.onResume();
        lifeCycleMonitor.onResume();
    }

    @Override
    protected void onReturnForeground() {
        super.onReturnForeground();
        lifeCycleMonitor.onReturnForeground();
    }

    @Override
    protected void onPushToBackStack() {
        super.onPushToBackStack();
        lifeCycleMonitor.onPushToBackStack();
    }

    @Override
    protected void onPopAway() {
        super.onPopAway();
        lifeCycleMonitor.onPopAway();
    }

    @Override
    protected void onPoppedOutToFront() {
        super.onPoppedOutToFront();
        lifeCycleMonitor.onPoppedOutToFront();
    }

    @Override
    protected void onOrientationChanged(int lastOrientation, int currentOrientation) {
        super.onOrientationChanged(lastOrientation, currentOrientation);
        lifeCycleMonitor.onOrientationChanged(lastOrientation, currentOrientation);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        lifeCycleMonitor.onDestroyView();
    }

    @Override
    public void onDestroy() {
        lifeCycleMonitor.onDestroy();
        super.onDestroy();
    }

    @Override
    public void update() {

    }
}
