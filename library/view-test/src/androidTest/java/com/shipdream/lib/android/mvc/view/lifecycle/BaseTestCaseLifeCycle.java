package com.shipdream.lib.android.mvc.view.lifecycle;

import com.shipdream.lib.android.mvc.view.BaseTestCase;

public abstract class BaseTestCaseLifeCycle extends BaseTestCase <MvcTestActivity> {

    public BaseTestCaseLifeCycle() {
        super(MvcTestActivity.class);
    }

    @Override
    protected void waitTest() throws InterruptedException {
        waitTest(1500);
    }
}
