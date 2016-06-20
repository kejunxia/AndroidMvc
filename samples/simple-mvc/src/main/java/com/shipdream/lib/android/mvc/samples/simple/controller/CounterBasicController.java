package com.shipdream.lib.android.mvc.samples.simple.controller;

import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.samples.simple.manager.CounterManager;

import javax.inject.Inject;

public class CounterBasicController extends AbstractController<CounterBasicController.Model> {
    @Override
    public Class<Model> modelType() {
        return Model.class;
    }

    public static class Model {
        private String count;

        public String getCount() {
            return count;
        }
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

    public void goToDetailView(Object sender) {
        navigationManager.navigate(sender).to(CounterDetailController.class);
    }

    /**
     * Event subscriber: notified by counterManager
     * @param event
     */
    private void onEvent(CounterManager.Event2C.OnCounterUpdated event) {
        getModel().count = String.valueOf(event.getCount());
        view.update();
    }

}
