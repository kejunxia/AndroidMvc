package com.shipdream.lib.android.mvc.samples.simple.mvp.presenter;

import com.shipdream.lib.android.mvc.samples.simple.mvp.manager.CounterManager;
import com.shipdream.lib.android.mvp.AbstractPresenter;
import com.shipdream.lib.android.mvp.NavigationManager;

import javax.inject.Inject;

public class CounterSubViewPresenter extends AbstractPresenter {
    @Override
    public Class modelType() {
        return null;
    }

    public interface View {
        void onCounterUpdated(String countInEnglish);
    }

    @Inject
    private NavigationManager navigationManager;

    @Inject
    private CounterManager counterManager;

    public View view;

    public void updateText() {
        view.onCounterUpdated(counterManager.convertNumberToEnglish(counterManager.getModel().getCount()));
    }

    private void onEvent(CounterManager.Event2C.OnCounterUpdated event) {
        view.onCounterUpdated(counterManager.convertNumberToEnglish(event.getCount()));
    }
}
