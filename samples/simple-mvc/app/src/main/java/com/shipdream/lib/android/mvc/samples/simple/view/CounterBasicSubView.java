package com.shipdream.lib.android.mvc.samples.simple.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.samples.simple.R;
import com.shipdream.lib.android.mvc.samples.simple.controller.CounterBasicSubController;

public class CounterBasicSubView extends AbstractFragment<CounterBasicSubController> {
    private TextView txtCountInEnglish;

    @Override
    protected Class<CounterBasicSubController> getControllerClass() {
        return CounterBasicSubController.class;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_a_sub;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        txtCountInEnglish = (TextView) view.findViewById(R.id.fragment_a_sub_countInEnglish);
    }

    @Override
    public void update() {
        txtCountInEnglish.setText(controller.getCountInEnglish());
    }
}
