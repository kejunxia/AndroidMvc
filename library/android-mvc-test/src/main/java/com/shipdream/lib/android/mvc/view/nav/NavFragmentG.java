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

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.view.test.R;

import javax.inject.Inject;

public class NavFragmentG extends MvcFragment {
    @Inject
    private ControllerG presenterG;

    private TextView textView;

    @Override
    protected Class getControllerClass() {
        return null;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_mvc_test_nav_e;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);
        textView = (TextView) view.findViewById(R.id.nav_frag_e_text);

        textView.setText(presenterG.getValue());
    }

    @Override
    public void update() {

    }
}
