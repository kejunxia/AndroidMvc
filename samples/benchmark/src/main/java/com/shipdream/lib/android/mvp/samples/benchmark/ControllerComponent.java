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

package com.shipdream.lib.android.mvc.samples.benchmark;

import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller0Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller1Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller2Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller3Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller4Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller5Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller6Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller7Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller8Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller9Impl;

import dagger.Component;

@Component(modules = ControllerModule.class)
public interface ControllerComponent {
    void inject(MainActivity.Container10 obj);
    void inject(MainActivity.Container10x10 obj);
    void inject(Controller0Impl controller);
    void inject(Controller1Impl controller);
    void inject(Controller2Impl controller);
    void inject(Controller3Impl controller);
    void inject(Controller4Impl controller);
    void inject(Controller5Impl controller);
    void inject(Controller6Impl controller);
    void inject(Controller7Impl controller);
    void inject(Controller8Impl controller);
    void inject(Controller9Impl controller);
}