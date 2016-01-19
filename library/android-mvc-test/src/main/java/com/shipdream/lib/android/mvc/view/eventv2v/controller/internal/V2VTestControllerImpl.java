package com.shipdream.lib.android.mvc.view.eventv2v.controller.internal;

import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.view.eventv2v.controller.V2VTestController;

public class V2VTestControllerImpl extends BaseControllerImpl implements V2VTestController{
    @Override
    public Class modelType() {
        return null;
    }

    @Override
    public void updateDialogButton(Object sender, String text) {
        postViewEvent(new EventC2V.OnButtonUpdated(sender, text));
    }
}
