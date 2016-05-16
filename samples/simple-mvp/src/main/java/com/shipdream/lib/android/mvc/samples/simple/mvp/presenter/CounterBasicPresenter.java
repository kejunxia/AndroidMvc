package com.shipdream.lib.android.mvc.samples.simple.mvp.presenter;

import com.shipdream.lib.android.mvc.samples.simple.mvp.manager.CounterManager;
import com.shipdream.lib.android.mvp.AbstractPresenter;
import com.shipdream.lib.android.mvp.NavigationManager;

import javax.inject.Inject;

public class CounterBasicPresenter extends AbstractPresenter {
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
        navigationManager.navigate(sender).to("LocationB");
    }

    private void onEvent(CounterManager.Event2C.OnCounterUpdated event) {
        view.onCounterUpdated(event.getCount(), counterManager.convertNumberToEnglish(event.getCount()));
    }

}
