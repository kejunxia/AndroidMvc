package com.shipdream.lib.android.mvc.samples.simple.controller;

import com.shipdream.lib.android.mvc.NavigationManager;

import javax.inject.Inject;

public class EntryController extends AbstractController {
    @Inject
    private NavigationManager navigationManager;

    public void startApp(Object sender) {
        navigationManager.navigate(sender).to(CounterBasicController.class);
    }

    @Override
    public Class modelType() {
        return null;
    }
}
