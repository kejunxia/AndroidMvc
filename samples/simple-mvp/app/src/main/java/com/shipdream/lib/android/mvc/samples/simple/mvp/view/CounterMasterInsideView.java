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

package com.shipdream.lib.android.mvc.samples.simple.mvp.view;

import android.widget.TextView;

import com.shipdream.lib.android.mvc.samples.simple.mvp.R;
import com.shipdream.lib.android.mvc.samples.simple.mvp.controller.CounterMasterInsideController;

import butterknife.BindView;

public class CounterMasterInsideView extends AbstractFragment<CounterMasterInsideController> {
    @BindView(R.id.screen_master_sub_countInEnglish)
    TextView txtCountInEnglish;

    @Override
    protected Class<CounterMasterInsideController> getControllerClass() {
        return CounterMasterInsideController.class;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.screen_master_subview;
    }

    @Override
    public void update() {
        txtCountInEnglish.setText(controller.getCountInEnglish());
    }
}
