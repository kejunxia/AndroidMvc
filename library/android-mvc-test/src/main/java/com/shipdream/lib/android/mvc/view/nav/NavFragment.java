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

package com.shipdream.lib.android.mvc.view.nav;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.android.mvc.view.MvcFragment;
import com.shipdream.lib.android.mvc.view.test.R;

import javax.inject.Inject;


public abstract class NavFragment extends MvcFragment {
    private Button next;
    private Button clear;

    @Inject
    private NavigationController navigationController;

    @Inject
    private AnotherController anotherController;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_mvc_test_nav;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        next = (Button) view.findViewById(R.id.button_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationController.navigate(v).to(getNextFragmentLocId()).go();
            }
        });

        clear = (Button) view.findViewById(R.id.button_clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationController.navigate(view).back(null).go();
            }
        });

        anotherController.populateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbar = (Toolbar) getView().findViewById(R.id.toolbar);
        toolbar.setTitle(getClass().getSimpleName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected abstract String getNextFragmentLocId();
}
