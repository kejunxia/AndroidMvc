package com.shipdream.lib.android.mvc.samples.simple.controller;

import com.shipdream.lib.android.mvc.samples.simple.manager.CounterManager;

import javax.inject.Inject;

public class CounterMasterInsideController extends AbstractController {
    @Override
    public Class modelType() {
        return null;
    }

    @Inject
    private CounterManager counterManager;

    public String getCount() {
        return String.valueOf(counterManager.getModel().getCount());
    }

    public String getCountInEnglish() {
        return counterManager.convertNumberToEnglish(counterManager.getModel().getCount());
    }

    private void onEvent(CounterManager.Event2C.OnCounterUpdated event) {
        view.update();
    }
}
