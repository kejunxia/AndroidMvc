package com.shipdream.lib.android.mvc.samples.simple.controller;

import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.samples.simple.manager.CounterManager;

import javax.inject.Inject;

public class CounterDetailController extends AbstractController<CounterDetailController.View> {
    @Override
    public Class modelType() {
        return null;
    }

    public interface View {
        void onCounterUpdated(int count, String countInEnglish);
    }

    @Inject
    private NavigationManager navigationManager;

    @Inject
    private CounterManager counterManager;

    public int getCount() {
        return counterManager.getModel().getCount();
    }

    public void increment(Object sender) {
        int count = counterManager.getModel().getCount();
        counterManager.setCount(sender, ++count);
    }

    public void decrement(Object sender) {
        int count = counterManager.getModel().getCount();
        counterManager.setCount(sender, --count);
    }

    public void goBackToBasicView(Object sender) {
        navigationManager.navigate(sender).back();
    }

    private void onEvent(CounterManager.Event2C.OnCounterUpdated event) {
        view.update();
    }
}
