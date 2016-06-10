package com.shipdream.lib.android.mvc.samples.simple.mvp.presenter;

import com.shipdream.lib.android.mvp.NavigationManager;
import com.shipdream.lib.android.mvp.AbstractPresenter;

import javax.inject.Inject;

public class EntryPresenter extends AbstractPresenter {
    @Inject
    private NavigationManager navigationManager;

    public void startApp(Object sender) {
        navigationManager.navigate(sender).to(CounterBasicPresenter.class);
    }

    @Override
    public Class modelType() {
        return null;
    }
}
