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

package com.shipdream.lib.android.mvc.samples.note.controller;

import com.shipdream.lib.android.mvc.controller.BaseController;

public interface AppController extends BaseController {
    /**
     * Orientation that controllers understand without depending on android which make the testing
     * easier on pure JVM
     */
    enum Orientation {
        UNSPECIFIED,
        PORTRAIT,
        LANDSCAPE
    }

    /**
     * Navigate the app to the initial location. Current logic is navigate the app to note list if
     * the current location is null.
     */
    void navigateToInitialLocation();

    void notifyOrientationChanged(Orientation lastOrientation, Orientation currentOrientation);

    Orientation getCurrentOrientation();
}
