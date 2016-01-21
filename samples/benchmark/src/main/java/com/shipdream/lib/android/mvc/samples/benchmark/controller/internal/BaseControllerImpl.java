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

package com.shipdream.lib.android.mvc.samples.benchmark.controller.internal;

import com.shipdream.lib.android.mvc.samples.benchmark.controller.BaseController;
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

import javax.inject.Inject;

public class BaseControllerImpl implements BaseController {
    @Inject
    Service0 service0;
    @Inject
    Service1 service1;
    @Inject
    Service2 service2;
    @Inject
    Service3 service3;
    @Inject
    Service4 service4;
    @Inject
    Service5 service5;
    @Inject
    Service6 service6;
    @Inject
    Service7 service7;
    @Inject
    Service8 service8;
    @Inject
    Service9 service9;

    public BaseControllerImpl() {
        System.out.print(getClass().getSimpleName() + "created");
    }
}
