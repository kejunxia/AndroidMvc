package com.shipdream.lib.android.mvc.inject.testNameMapping.controller.internal;

import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.TimerController;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.TimerModel;

public class TimerControllerImpl extends BaseControllerImpl<TimerModel> implements TimerController{
    @Override
    public Class<TimerModel> modelType() {
        return TimerModel.class;
    }

    @Override
    public void setInitialValue(long value) {
        getModel().setInitialValue(value);
    }

    @Override
    public long getInitialValue() {
        return getModel().getInitialValue();
    }
}
