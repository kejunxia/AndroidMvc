package com.shipdream.lib.android.mvc.view.viewpager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.view.MvcFragment;
import com.shipdream.lib.android.mvc.view.test.R;
import com.shipdream.lib.android.mvc.view.viewpager.controller.TabController;

import javax.inject.Inject;

public class TabFragmentA extends MvcFragment {
    static final String INIT_TEXT = "Tab A";
    static final String RESTORE_TEXT = "Restored TabA";

    @Inject
    TabController tabController;

    private TextView textView;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_view_pager_tab;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        textView = (TextView) view.findViewById(R.id.fragment_view_pager_tab_text);
        if (reason == Reason.FIRST_TIME) {
            textView.setText(INIT_TEXT);
            tabController.setName(RESTORE_TEXT);
        } else if (reason == Reason.RESTORE) {
            textView.setText(tabController.getModel().getName());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
