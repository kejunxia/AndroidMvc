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

package com.shipdream.lib.android.mvc.samples.simple.mvp.service.internal;

import android.content.Context;

import com.shipdream.lib.android.mvc.FragmentController;
import com.shipdream.lib.android.mvc.samples.simple.mvp.AppContext;
import com.shipdream.lib.android.mvc.samples.simple.mvp.R;
import com.shipdream.lib.android.mvc.samples.simple.mvp.controller.CounterDetailController;
import com.shipdream.lib.android.mvc.samples.simple.mvp.controller.CounterMasterController;
import com.shipdream.lib.android.mvc.samples.simple.mvp.service.ResourceService;

import javax.inject.Inject;

public class ResourceServiceImpl implements ResourceService {
    @Inject
    @AppContext
    private Context context;

    @Override
    public <C extends FragmentController> String getDefaultTitle(Class<C> controllerClass) {
        if (controllerClass == CounterMasterController.class) {
            return context.getString(R.string.title_master_screen);
        } else if (controllerClass == CounterDetailController.class) {
            return context.getString(R.string.title_detail_screen);
        }
        return context.getString(R.string.app_name);
    }
}
