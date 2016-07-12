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

package com.shipdream.lib.android.mvc.view.injection;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.FragmentController;
import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.NavLocation;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitor;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerA;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerB;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerC;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerD;
import com.shipdream.lib.android.mvc.view.test.R;

import java.util.List;

import javax.inject.Inject;

public abstract class FragmentInjection<C extends FragmentController> extends MvcFragment<C> {
    private Spinner spinner;
    private Button buttonGo;
    private Toolbar toolbar;
    protected TextView textViewA;
    protected TextView textViewB;
    protected TextView textViewC;

    @Inject
    private NavigationManager navigationManager;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_injection;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        getLifeCycleMonitor().onCreateView(view, savedInstanceState);
        getLifeCycleMonitor().onViewCreated(view, savedInstanceState);
        super.onViewReady(view, savedInstanceState, reason);
        getLifeCycleMonitor().onViewReady(view, savedInstanceState, reason);

        spinner = (Spinner) view.findViewById(R.id.spinner);
        buttonGo = (Button) view.findViewById(R.id.buttonGo);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        textViewA = (TextView) view.findViewById(R.id.textA);
        textViewB = (TextView) view.findViewById(R.id.textB);
        textViewC = (TextView) view.findViewById(R.id.textC);

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.injection_test_locations, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);

        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = spinner.getSelectedItemPosition();
                Class loc = null;
                CharSequence item = adapter.getItem(position);
                if (item.equals("A")) {
                    loc = ControllerA.class;
                } else if (item.equals("B")) {
                    loc = ControllerB.class;
                } else if (item.equals("C")) {
                    loc = ControllerC.class;
                }else if (item.equals("D")) {
                    loc = ControllerD.class;
                }
                navigationManager.navigate(view).to(loc);
            }
        });

        setUpData();

        toolbar.setTitle("Loc:" + printHistory());
    }

    @Override
    public void onResume() {
        super.onResume();
        getLifeCycleMonitor().onResume();
    }

    private String printHistory() {
        NavLocation curLoc = navigationManager.getModel().getCurrentLocation();
        String history = getLocName(curLoc);
        while(curLoc != null) {
            if(curLoc.getPreviousLocation() != null) {
                history = getLocName(curLoc.getPreviousLocation()) + "-> " + history;
            }
            curLoc = curLoc.getPreviousLocation();
        }
        return history;
    }

    private String getLocName(NavLocation navLocation) {
        if(navLocation == null) {
            return "";
        } else {
            String id = navLocation.getLocationId();
            return id.substring(id.length() - 1);
        }
    }

    protected void displayTags(TextView textView, List<String> tags) {
        String str = "";
        for(String s : tags) {
            if(!str.isEmpty()) {
                str += "\n";
            }
            str += s;
        }
        textView.setText(str);
    }

    protected abstract void setUpData();

    protected abstract LifeCycleMonitor getLifeCycleMonitor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifeCycleMonitor().onCreate(savedInstanceState);

        Log.d("MvcTesting", "onCreated " + getClass().getSimpleName());
    }

    @Override
    protected void onReturnForeground() {
        super.onReturnForeground();
        getLifeCycleMonitor().onReturnForeground();
    }

    @Override
    protected void onPushToBackStack() {
        super.onPushToBackStack();
        getLifeCycleMonitor().onPushToBackStack();
    }

    @Override
    protected void onPopAway() {
        super.onPopAway();
        getLifeCycleMonitor().onPopAway();
    }

    @Override
    protected void onPoppedOutToFront() {
        super.onPoppedOutToFront();
        getLifeCycleMonitor().onPoppedOutToFront();
    }

    @Override
    protected void onOrientationChanged(int lastOrientation, int currentOrientation) {
        super.onOrientationChanged(lastOrientation, currentOrientation);
        getLifeCycleMonitor().onOrientationChanged(lastOrientation, currentOrientation);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLifeCycleMonitor().onDestroyView();
    }

    @Override
    public void onDestroy() {
        getLifeCycleMonitor().onDestroy();
        super.onDestroy();
    }
}
