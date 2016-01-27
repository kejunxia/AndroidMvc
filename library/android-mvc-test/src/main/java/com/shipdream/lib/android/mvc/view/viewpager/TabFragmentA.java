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

import com.shipdream.lib.android.mvc.manager.NavigationManager;
import com.shipdream.lib.android.mvc.view.MvcApp;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitor;
import com.shipdream.lib.android.mvc.view.test.R;
import com.shipdream.lib.android.mvc.view.viewpager.controller.TabController;

import javax.inject.Inject;

public class TabFragmentA extends BaseTabFragment {
    static final String INIT_TEXT = "Tab A";
    static final String RESTORE_TEXT = "Restored TabA";

    @Inject
    TabController tabController;

    private TextView textView;

    @Inject
    private NavigationManager navigationManager;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_view_pager_tab;
    }

    @Override
    protected LifeCycleMonitor getLifeCycleMonitor() {
        return MvcApp.lifeCycleMonitorFactory.provideLifeCycleMonitorA();
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        textView = (TextView) view.findViewById(R.id.fragment_view_pager_tab_text);
        if (reason.isFirstTime()) {
            textView.setText(INIT_TEXT);
            tabController.setName(RESTORE_TEXT);
        } else if (reason.isRestored()) {
            textView.setText(tabController.getModel().getName());
        }

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationManager.navigate(v).to(SubFragment.class.getSimpleName());
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
