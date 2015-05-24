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

package com.shipdream.lib.android.mvc.samples.simple.view;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.samples.simple.R;
import com.shipdream.lib.android.mvc.samples.simple.controller.CounterController;
import com.shipdream.lib.android.mvc.view.MvcFragment;

import javax.inject.Inject;

public class FragmentB extends MvcFragment {
    private static final long INTERVAL = 500;

    @Inject
    private CounterController counterController;

    private TextView display;
    private Button increment;
    private Button decrement;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_b;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        display = (TextView) view.findViewById(R.id.fragment_b_counterDisplay);
        increment = (Button) view.findViewById(R.id.fragment_b_buttonIncrement);
        decrement = (Button) view.findViewById(R.id.fragment_b_buttonDecrement);

        increment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startContinuousIncrement(v);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        stopContinuousIncrement(v);
                        break;
                }
                return false;
            }
        });

        decrement.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startContinuousDecrement(v);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        stopContinuousDecrement(v);
                        break;
                }
                return false;
            }
        });

        updateCountDisplay(counterController.getModel().getCount());
    }

    @Override
    public boolean onBackButtonPressed() {
        //Use counterController to manage navigation back make navigation testable
        counterController.goBackToBasicView(this);
        //Return true to not pass the back button pressed event to upper level handler.
        return true;
        //Or we can let the fragment manage back navigation back automatically where we don't
        //override this method which will call NavigationController.navigateBack(Object sender)
        //automatically
    }

    private boolean isIncrementingOn = false;
    private boolean firstIncrementTriggered = false;
    private void startContinuousIncrement(final View v) {
        isIncrementingOn = true;
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isIncrementingOn) {
                    firstIncrementTriggered = true;
                    counterController.increment(v);
                    v.postDelayed(this, INTERVAL);
                }
            }
        }, INTERVAL);
    }

    private void stopContinuousIncrement(View v) {
        isIncrementingOn = false;
        if (!firstIncrementTriggered) {
            counterController.increment(v);
        }
        firstIncrementTriggered = false;
    }

    private boolean isDecrementingOn = false;
    private boolean firstDecrementTriggered = false;
    private void startContinuousDecrement(final View v) {
        isDecrementingOn = true;
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isDecrementingOn) {
                    counterController.decrement(v);
                    v.postDelayed(this, INTERVAL);
                }
            }
        }, INTERVAL);
    }

    private void stopContinuousDecrement(View v) {
        isDecrementingOn = false;
        if (!firstDecrementTriggered) {
            counterController.decrement(v);
        }
        firstDecrementTriggered = false;
    }

    private void onEvent(CounterController.EventC2V.OnCounterUpdated event) {
        updateCountDisplay(event.getCount());
    }

    private void updateCountDisplay(int count) {
        display.setText(String.valueOf(count));
    }
}
