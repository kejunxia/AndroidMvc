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

package com.shipdream.lib.android.mvc.view.nav;

import android.util.Log;

import com.shipdream.lib.android.mvc.FragmentController;
import com.shipdream.lib.android.mvc.UiView;

import javax.inject.Inject;

public class ControllerG extends FragmentController<ControllerG.Model, UiView> {
    public static class Model {
        public String value;
    }

    @Inject
    private DisposeCheckerG disposeCheckerG;

    @Override
    public void onDestroy() {
        Log.i("DisposeCheck", "Controller G disposed");
    }

    @Override
    public Class<Model> modelType() {
        return Model.class;
    }

    public void setValue(String value) {
        getModel().value = value;
    }

    public String getValue() {
        return getModel().value;
    }
}
