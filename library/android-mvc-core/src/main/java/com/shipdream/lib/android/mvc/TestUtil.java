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

package com.shipdream.lib.android.mvc;

/**
 * Util to help test controllers.
 */
public class TestUtil {
    /**
     * Allow test cases to assign the view of controller.
     * @param controller The controller
     * @param view The view associated with the controller
     * @param <VIEW> The type of view
     */
    public static <VIEW extends UiView> void assignControllerView(Controller controller, VIEW view) {
        controller.view = view;
    }
}
