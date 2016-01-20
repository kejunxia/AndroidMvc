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

package com.shipdream.lib.android.mvc.samples.simple.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.android.mvc.samples.simple.R;
import com.shipdream.lib.android.mvc.samples.simple.controller.CounterController;
import com.shipdream.lib.android.mvc.view.MvcFragment;

import javax.inject.Inject;

public class FragmentA extends MvcFragment {
    @Inject
    private NavigationController navigationController;

    @Inject
    private CounterController counterController;

    private TextView display;
    private Button increment;
    private Button decrement;
    private Button buttonShowAdvancedView;

    /**
     * @return Layout id used to inflate the view of this MvcFragment.
     */
    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_a;
    }

    /**
     * Lifecycle similar to onViewCreated by with more granular control with an extra argument to
     * indicate why this view is created: 1. first time created, or 2. rotated or 3. restored
     * @param view The root view of the fragment
     * @param savedInstanceState The savedInstanceState when the fragment is being recreated after
     *                           its enclosing activity is killed by OS, otherwise null including on
     *                           rotation
     * @param reason Indicates the {@link Reason} why the onViewReady is called.
     */
    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        display = (TextView) view.findViewById(R.id.fragment_a_counterDisplay);
        increment = (Button) view.findViewById(R.id.fragment_a_buttonIncrement);
        decrement = (Button) view.findViewById(R.id.fragment_a_buttonDecrement);
        buttonShowAdvancedView = (Button) view.findViewById(R.id.fragment_a_buttonShowAdvancedView);

        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counterController.increment(v);
            }
        });

        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counterController.decrement(v);
            }
        });

        buttonShowAdvancedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Use counterController to manage navigation to make navigation testable
                counterController.goToAdvancedView(v);
                //Or we can use NavigationController directly though it's harder to unit test on
                //controller level.
                //example:
                //navigationController.navigateTo(v, "LocationB");
            }
        });

        if (reason.isFirstTime()) {
            FragmentA_SubFragment f = new FragmentA_SubFragment();

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_a_anotherFragmentContainer, f).commit();
        }

        updateCountDisplay(counterController.getModel().getCount());
    }

    /**
     * Callback when the fragment is popped out by back navigation
     */
    @Override
    protected void onPoppedOutToFront() {
        super.onPoppedOutToFront();
        updateCountDisplay(counterController.getModel().getCount());
    }

    //Define event handler by method named as onEvent with single parameter of the event type
    //to respond event CounterController.EventC2V.OnCounterUpdated
    private void onEvent(CounterController.EventC2V.OnCounterUpdated event) {
        updateCountDisplay(event.getCount());
    }

    /**
     * Update the text view of count number
     * @param count The number of count
     */
    private void updateCountDisplay(int count) {
        display.setText(String.valueOf(count));
    }
}
