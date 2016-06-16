package com.shipdream.lib.android.mvc.samples.simple.controller;

import com.shipdream.lib.android.mvc.samples.simple.manager.CounterManager;
import com.shipdream.lib.android.mvc.Controller;
import com.shipdream.lib.android.mvc.NavigationManager;

import javax.inject.Inject;

public class CounterBasicController extends Controller {
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

    public View view;

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

    public void goToDetailView(Object sender) {
        navigationManager.navigate(sender).to(CounterDetailController.class);
    }

    private void onEvent(CounterManager.Event2C.OnCounterUpdated event) {
        view.onCounterUpdated(event.getCount(), counterManager.convertNumberToEnglish(event.getCount()));
    }

}
