package com.shipdream.lib.android.mvc.samples.simple.mvp.presenter;

import com.shipdream.lib.android.mvc.manager.NavigationManager;
import com.shipdream.lib.android.mvc.samples.simple.mvp.manager.CounterManager;

import javax.inject.Inject;

public class CounterPresenter {
    public interface View {
        void onCounterUpdated(int count, String countInEnglish);
    }

    @Inject
    private NavigationManager navigationManager;

    @Inject
    private CounterManager counterManager;

    @Inject
    private View view;

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

    private void onEvent(CounterManager.Event2C.OnCounterUpdated event) {
        view.onCounterUpdated(event.getCount(), convertNumberToEnglish(event.getCount()));
    }

    public String convertNumberToEnglish(int number) {
        if (number < -3) {
            return "Less than negative three";
        } else  if (number == -3) {
            return "Negative three";
        } else  if (number == -2) {
            return "Negative two";
        } else  if (number == -1) {
            return "Negative one";
        } else if (number == 0) {
            return "Zero";
        } else if (number == 1) {
            return "One";
        } else if (number == 2) {
            return "Two";
        } else if (number == 3) {
            return "Three";
        } else {
            return "Greater than three";
        }
    }

    public void goToAdvancedView(Object sender) {
        navigationManager.navigate(sender).to("LocationB");
    }

    public void goBackToBasicView(Object sender) {
        navigationManager.navigate(sender).back();
    }
}
