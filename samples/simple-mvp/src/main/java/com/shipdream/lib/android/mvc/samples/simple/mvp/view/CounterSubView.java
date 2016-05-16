package com.shipdream.lib.android.mvc.samples.simple.mvp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.samples.simple.mvp.R;
import com.shipdream.lib.android.mvc.samples.simple.mvp.presenter.CounterSubViewPresenter;
import com.shipdream.lib.android.mvc.view.MvcFragment;

import javax.inject.Inject;

public class CounterSubView extends MvcFragment implements CounterSubViewPresenter.View {
    @Inject
    private CounterSubViewPresenter presenter;

    private TextView txtCountInEnglish;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_a_sub;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, MvcFragment.Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        presenter.view = this;

        txtCountInEnglish = (TextView) view.findViewById(R.id.fragment_a_sub_countInEnglish);
    }

    @Override
    protected void onPoppedOutToFront() {
        super.onPoppedOutToFront();
        presenter.updateText();
    }

    @Override
    public void onCounterUpdated(String countInEnglish) {
        txtCountInEnglish.setText(countInEnglish);
    }
}
