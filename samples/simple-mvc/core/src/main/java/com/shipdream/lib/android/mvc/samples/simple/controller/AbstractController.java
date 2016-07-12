package com.shipdream.lib.android.mvc.samples.simple.controller;

import com.shipdream.lib.android.mvc.FragmentController;
import com.shipdream.lib.android.mvc.UiView;

public abstract class AbstractController<MODEL, VIEW extends UiView>
        extends FragmentController<MODEL, VIEW> {
}
