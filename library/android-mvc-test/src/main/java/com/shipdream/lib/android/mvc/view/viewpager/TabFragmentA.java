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

package com.shipdream.lib.android.mvc.view.viewpager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitor;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitorA;
import com.shipdream.lib.android.mvc.view.test.R;
import com.shipdream.lib.android.mvc.view.viewpager.controller.SecondFragmentController;
import com.shipdream.lib.android.mvc.view.viewpager.controller.TabControllerA;

import javax.inject.Inject;

public class TabFragmentA extends BaseTabFragment<TabControllerA> {
    private TextView textView;

    @Inject
    private NavigationManager navigationManager;

    @Override
    protected Class getControllerClass() {
        return TabControllerA.class;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_view_pager_tab;
    }

    @Inject
    private LifeCycleMonitorA lifeCycleMonitorA;
    @Override
    protected LifeCycleMonitor getLifeCycleMonitor() {
        return lifeCycleMonitorA;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        textView = (TextView) view.findViewById(R.id.fragment_view_pager_tab_text);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationManager.navigate(v).to(SecondFragmentController.class);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void update() {
        textView.setText(controller.getModel().getName());
    }
}
