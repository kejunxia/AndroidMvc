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

package com.shipdream.lib.android.mvc.samples.simple.mvvm.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.R;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.controller.CounterMasterInsideController;

public class CounterMasterInsideView extends AbstractFragment<CounterMasterInsideController> {
    private TextView txtCountInEnglish;

    @Override
    protected Class<CounterMasterInsideController> getControllerClass() {
        return CounterMasterInsideController.class;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_a_sub;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        txtCountInEnglish = (TextView) view.findViewById(R.id.fragment_a_sub_countInEnglish);
    }

    @Override
    public void update() {
        txtCountInEnglish.setText(controller.getCountInEnglish());
    }
}
