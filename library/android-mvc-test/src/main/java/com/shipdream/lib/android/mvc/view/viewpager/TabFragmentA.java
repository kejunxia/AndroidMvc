package com.shipdream.lib.android.mvc.view.viewpager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.android.mvc.view.MvcApp;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitor;
import com.shipdream.lib.android.mvc.view.test.R;
import com.shipdream.lib.android.mvc.view.viewpager.controller.TabController;

import javax.inject.Inject;

public class TabFragmentA extends BaseTabFragment {
    static final String INIT_TEXT = "Tab A";
    static final String RESTORE_TEXT = "Restored TabA";

    @Inject
    TabController tabController;

    private TextView textView;

    @Inject
    private NavigationController navigationController;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_view_pager_tab;
    }

    @Override
    protected LifeCycleMonitor getLifeCycleMonitor() {
        return MvcApp.lifeCycleMonitorFactory.provideLifeCycleMonitorA();
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        textView = (TextView) view.findViewById(R.id.fragment_view_pager_tab_text);
        if (reason.isFirstTime()) {
            textView.setText(INIT_TEXT);
            tabController.setName(RESTORE_TEXT);
        } else if (reason.isRestored()) {
            textView.setText(tabController.getModel().getName());
        }

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationController.navigate(v).to(SubFragment.class.getSimpleName()).go();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
