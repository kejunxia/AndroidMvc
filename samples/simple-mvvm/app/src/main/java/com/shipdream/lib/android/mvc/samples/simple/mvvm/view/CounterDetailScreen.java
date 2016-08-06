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

package com.shipdream.lib.android.mvc.samples.simple.mvvm.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.R;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.controller.CounterDetailController;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.view.service.CountService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class CounterDetailScreen extends AbstractFragment<CounterDetailController> {
    @BindView(R.id.fragment_detail_counterDisplay)
    TextView countDisplay;

    @Override
    protected Class<CounterDetailController> getControllerClass() {
        return CounterDetailController.class;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_counter_detail;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);
        ButterKnife.bind(this, view);
    }

    @Override
    public void update() {
        //Bind model to view here
        countDisplay.setText(controller.getModel().getCount());
    }

    private void onEvent(CounterDetailController.Event.OnCountUpdated event) {
        countDisplay.setText(event.getCount());
    }

    @OnClick(R.id.fragment_detail_buttonAutoIncrement)
    void onClick(View v) {
        Intent intent = new Intent(getActivity(), CountService.class);
        getActivity().startService(intent);
    }

    @OnTouch(R.id.fragment_detail_buttonIncrement)
    boolean onTouchIncrement(final View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                controller.startContinuousIncrement();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                controller.stopContinuousIncrement();
                break;
        }
        return false;
    }

    @OnTouch(R.id.fragment_detail_buttonDecrement)
    boolean onTouch(final View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                controller.startContinuousDecrement();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                controller.stopContinuousDecrement();
                break;
        }
        return false;
    }

    @Override
    public boolean onBackButtonPressed() {
        //Use counterController to manage navigation back make navigation testable
        controller.goBackToBasicView(this);
        //Return true to not pass the back button pressed event to upper level handler.
        return true;
        //Or we can let the fragment manage back navigation back automatically where we don't
        //override this method which will call NavigationManager.navigateBack(Object sender)
        //automatically
    }
}
