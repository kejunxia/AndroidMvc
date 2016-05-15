package com.shipdream.lib.android.mvc.samples.simple.mvp.presenter;

import com.shipdream.lib.android.mvc.manager.NavigationManager;

import javax.inject.Inject;

public class EntryPresenter {
    @Inject
    private NavigationManager navigationManager;

    public void startApp(Object sender) {
        navigationManager.navigate(sender).to("LocationA");
    }
}
