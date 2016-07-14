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
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.Controller;
import com.shipdream.lib.android.mvc.Forwarder;
import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.view.test.R;

import javax.inject.Inject;

public abstract class NavFragment extends MvcFragment {
    private Button next;
    private Button clear;
    private TextView textView;


    @Inject
    private NavigationManager navigationManager;

    @Inject
    private AnotherController anotherPresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_mvc_test_nav;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        textView = (TextView) view.findViewById(R.id.text);
        textView.setText(getClass().getSimpleName());

        next = (Button) view.findViewById(R.id.button_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean interim = getNextFragmentLocId().getName().contains("C");
                navigationManager.navigate(v).to(getNextFragmentLocId(), new Forwarder().setInterim(interim));
            }
        });

        clear = (Button) view.findViewById(R.id.button_clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationManager.navigate(view).back(null);
            }
        });

        anotherPresenter.populateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbar = (Toolbar) getView().findViewById(R.id.toolbar);
        toolbar.setTitle("Page: " + getClass().getSimpleName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected abstract Class<? extends Controller> getNextFragmentLocId();
}
