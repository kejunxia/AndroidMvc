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

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.shipdream.lib.android.mvc.MvcActivity;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.samples.simple.mvp.R;
import com.shipdream.lib.android.mvc.samples.simple.mvp.controller.AppDelegateController;

public class AppDelegateFragment extends MvcActivity.DelegateFragment<AppDelegateController>
        implements AppDelegateController.View{
    private Toolbar toolbar;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_app;
    }

    @Override
    protected int getContentLayoutResId() {
        return R.id.fragment_app_main;
    }

    @Override
    protected Class getControllerClass() {
        return AppDelegateController.class;
    }

    @Override
    public void update() {
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.navigateBack(v);
            }
        });
    }

    /**
     * What to do when app starts for the first time
     */
    @Override
    protected void onStartUp() {
        controller.startApp(this);
    }

    @Override
    public void updateTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public void changeNavIcon(boolean showBackArrow) {
        if (showBackArrow) {
            toolbar.setNavigationIcon(R.drawable.ic_action_nav_back);
        } else {
            toolbar.setNavigationIcon(null);
        }
    }
}
