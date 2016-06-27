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

package com.shipdream.lib.android.mvc.view.help;

import android.os.Bundle;
import android.view.View;

import com.shipdream.lib.android.mvc.Reason;

public interface LifeCycleMonitor {
    void onCreate(Bundle savedInstanceState);
    void onCreateView(View rootView, Bundle savedInstanceState);
    void onViewCreated(View rootView, Bundle savedInstanceState);
    void onViewReady(View rootView, Bundle savedInstanceState, Reason reason);
    void onResume();
    void onReturnForeground();
    void onPushToBackStack();
    void onPopAway();
    void onPoppedOutToFront();
    void onOrientationChanged(int lastOrientation, int currentOrientation);
    void onDestroyView();
    void onDestroy();
}
