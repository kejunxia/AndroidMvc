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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.samples.simple.mvp.R;
import com.shipdream.lib.android.mvc.samples.simple.mvp.presenter.CounterDetailPresenter;
import com.shipdream.lib.android.mvc.samples.simple.mvp.view.service.CountService;
import com.shipdream.lib.android.mvc.view.MvcFragment;

import javax.inject.Inject;

public class CounterDetailView extends MvcFragment implements CounterDetailPresenter.View{
    private class ContinuousCounter implements Runnable {
        private final boolean incrementing;
        private boolean canceled = false;
        private static final long INTERVAL = 200;

        public ContinuousCounter(boolean incrementing) {
            this.incrementing = incrementing;
        }

        @Override
        public void run() {
            if (!canceled) {
                if (incrementing) {
                    presenter.increment(this);
                } else {
                    presenter.decrement(this);
                }

                handler.postDelayed(this, INTERVAL);
            }
        }

        private void cancel() {
            canceled = true;
        }
    }

    @Inject
    private CounterDetailPresenter presenter;

    private TextView display;
    private Button increment;
    private Button decrement;
    private Button autoIncrement;
    private Handler handler;
    private ContinuousCounter incrementCounter;
    private ContinuousCounter decrementCounter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_counter_detail;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        presenter.view = this;

        display = (TextView) view.findViewById(R.id.fragment_b_counterDisplay);
        increment = (Button) view.findViewById(R.id.fragment_b_buttonIncrement);
        decrement = (Button) view.findViewById(R.id.fragment_b_buttonDecrement);
        autoIncrement = (Button) view.findViewById(R.id.fragment_b_buttonAutoIncrement);

        increment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startContinuousIncrement();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        stopContinuousIncrement();
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
                        startContinuousDecrement();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        stopContinuousDecrement();
                        break;
                }
                return false;
            }
        });

        autoIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CountService.class);
                getActivity().startService(intent);
            }
        });

        updateCountDisplay(presenter.getCount());
    }

    @Override
    public boolean onBackButtonPressed() {
        //Use counterController to manage navigation back make navigation testable
        presenter.goBackToBasicView(this);
        //Return true to not pass the back button pressed event to upper level handler.
        return true;
        //Or we can let the fragment manage back navigation back automatically where we don't
        //override this method which will call NavigationManager.navigateBack(Object sender)
        //automatically
    }

    @Override
    public void onCounterUpdated(int count, String countInEnglish) {
        updateCountDisplay(presenter.getCount());
    }

    private void startContinuousIncrement() {
        stopContinuousIncrement();
        incrementCounter = new ContinuousCounter(true);
        incrementCounter.run();
    }

    private void stopContinuousIncrement() {
        if (incrementCounter != null) {
            incrementCounter.cancel();
        }
    }

    private void startContinuousDecrement() {
        stopContinuousDecrement();
        decrementCounter = new ContinuousCounter(false);
        decrementCounter.run();
    }

    private void stopContinuousDecrement() {
        if (decrementCounter != null) {
            decrementCounter.cancel();
        }
    }

    private void updateCountDisplay(int count) {
        display.setText(String.valueOf(count));
    }
}
