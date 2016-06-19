package com.shipdream.lib.android.mvc.samples.simple.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.samples.simple.R;
import com.shipdream.lib.android.mvc.samples.simple.controller.CounterSubViewController;

public class CounterSubView extends MvcFragment<CounterSubViewController>
        implements CounterSubViewController.View {
    private TextView txtCountInEnglish;

    @Override
    protected Class<CounterSubViewController> getControllerClass() {
        return CounterSubViewController.class;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_a_sub;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        controller.view = this;

        txtCountInEnglish = (TextView) view.findViewById(R.id.fragment_a_sub_countInEnglish);
    }

    @Override
    protected void onPoppedOutToFront() {
        super.onPoppedOutToFront();
        controller.updateText();
    }

    @Override
    public void onCounterUpdated(String countInEnglish) {
        txtCountInEnglish.setText(countInEnglish);
    }
}
