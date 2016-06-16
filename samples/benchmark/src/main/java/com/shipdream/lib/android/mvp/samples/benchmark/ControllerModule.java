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

import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller0;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller1;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller2;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller3;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller4;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller5;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller6;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller7;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller8;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller9;
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
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service0;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service1;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service2;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service3;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service4;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service5;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service6;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service7;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service8;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service9;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service0Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service1Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service2Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service3Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service4Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service5Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service6Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service7Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service8Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service9Impl;

import dagger.Module;
import dagger.Provides;

@Module
public class ControllerModule {
    @Provides
    public Controller0 providesController0() {
        return new Controller0Impl();
    }

    @Provides
    public Controller1 providesController1() {
        return new Controller1Impl();
    }

    @Provides
    public Controller2 providesController2() {
        return new Controller2Impl();
    }

    @Provides
    public Controller3 providesController3() {
        return new Controller3Impl();
    }

    @Provides
    public Controller4 providesController4() {
        return new Controller4Impl();
    }

    @Provides
    public Controller5 providesController5() {
        return new Controller5Impl();
    }

    @Provides
    public Controller6 providesController6() {
        return new Controller6Impl();
    }

    @Provides
    public Controller7 providesController7() {
        return new Controller7Impl();
    }

    @Provides
    public Controller8 providesController8() {
        return new Controller8Impl();
    }

    @Provides
    public Controller9 providesController9() {
        return new Controller9Impl();
    }

    @Provides
    public Service0 providesService0() {
        return new Service0Impl();
    }

    @Provides
    public Service1 providesService1() {
        return new Service1Impl();
    }

    @Provides
    public Service2 providesService2() {
        return new Service2Impl();
    }

    @Provides
    public Service3 providesService3() {
        return new Service3Impl();
    }

    @Provides
    public Service4 providesService4() {
        return new Service4Impl();
    }

    @Provides
    public Service5 providesService5() {
        return new Service5Impl();
    }

    @Provides
    public Service6 providesService6() {
        return new Service6Impl();
    }

    @Provides
    public Service7 providesService7() {
        return new Service7Impl();
    }

    @Provides
    public Service8 providesService8() {
        return new Service8Impl();
    }

    @Provides
    public Service9 providesService9() {
        return new Service9Impl();
    }
}
