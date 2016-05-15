package com.shipdream.lib.android.mvc.samples.simple.mvp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.samples.simple.R;
import com.shipdream.lib.android.mvc.view.MvcFragment;

import javax.inject.Inject;

public class EntrySubView extends MvcFragment {
    @Inject
    private CounterController counterController;

    private TextView countInEnglish;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_a_sub;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, MvcFragment.Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        countInEnglish = (TextView) view.findViewById(R.id.fragment_a_sub_countInEnglish);

        String text = counterController.convertNumberToEnglish(counterController.getModel().getCount());
        countInEnglish.setText(text);
    }

    private void onEvent(CounterController.EventC2V.OnCounterUpdated event) {
        countInEnglish.setText(event.getCountInEnglish());
    }
}
