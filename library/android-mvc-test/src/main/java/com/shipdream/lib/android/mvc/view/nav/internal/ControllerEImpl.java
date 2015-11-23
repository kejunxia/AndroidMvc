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

package com.shipdream.lib.android.mvc.view.nav.internal;

import android.util.Log;

import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.view.nav.ControllerE;
import com.shipdream.lib.android.mvc.view.nav.DisposeCheckerE;

import javax.inject.Inject;

public class ControllerEImpl extends BaseControllerImpl<ControllerE.Model> implements ControllerE {
    private int value = 0;

    @Inject
    private DisposeCheckerE disposeCheckerE;

    @Override
    public void onDisposed() {
        Log.i("DisposeCheck", "Checker E disposed");
        disposeCheckerE.onDisposed();
    }

    @Override
    protected Class<Model> getModelClassType() {
        return Model.class;
    }

    @Override
    public void setValue(String value) {
        getModel().value = value;
    }

    @Override
    public String getValue() {
        return getModel().value;
    }

}
