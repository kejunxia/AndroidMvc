package com.shipdream.lib.android.mvc.samples.simple.view;

import android.os.Bundle;

import com.shipdream.lib.android.mvc.UiView;
import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.samples.simple.controller.AbstractController;

public abstract class AbstractFragment<C extends AbstractController>
        extends MvcFragment<C> implements UiView {
    @Override
    public void onViewReady(android.view.View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);
    }
}
