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

import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitor;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitorB;
import com.shipdream.lib.android.mvc.view.test.R;
import com.shipdream.lib.android.mvc.view.viewpager.controller.TabControllerB;

import javax.inject.Inject;

public class TabFragmentB extends BaseTabFragment<TabControllerB> {
    private TextView textView;

    @Override
    protected Class<TabControllerB> getControllerClass() {
        return TabControllerB.class;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_view_pager_tab;
    }

    @Inject
    private LifeCycleMonitorB lifeCycleMonitorB;
    @Override
    protected LifeCycleMonitor getLifeCycleMonitor() {
        return lifeCycleMonitorB;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        textView = (TextView) view.findViewById(R.id.fragment_view_pager_tab_text);

    }

    @Override
    public void update() {
        textView.setText("Tab B");
    }
}
