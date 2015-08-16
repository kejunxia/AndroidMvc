package com.shipdream.lib.android.mvc.view.viewpager.controller.internal;

import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.view.viewpager.TabModel;
import com.shipdream.lib.android.mvc.view.viewpager.controller.TabController;

public class TabControllerImpl extends BaseControllerImpl<TabModel> implements TabController {
    @Override
    protected Class<TabModel> getModelClassType() {
        return TabModel.class;
    }

    @Override
    public void setName(String name) {
        getModel().setName(name);
    }
}
