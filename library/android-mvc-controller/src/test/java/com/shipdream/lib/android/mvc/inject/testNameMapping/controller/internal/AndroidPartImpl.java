package com.shipdream.lib.android.mvc.inject.testNameMapping.controller.internal;

import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.AndroidPart;

public class AndroidPartImpl implements AndroidPart {
    private Object context;

    public AndroidPartImpl(Object context) {
        this.context = context;
    }
}
