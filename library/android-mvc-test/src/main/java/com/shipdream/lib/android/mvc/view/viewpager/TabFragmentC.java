package com.shipdream.lib.android.mvc.view.viewpager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.view.MvcFragment;
import com.shipdream.lib.android.mvc.view.test.R;

public class TabFragmentC extends MvcFragment {
    private TextView textView;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_view_pager_tab;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        textView = (TextView) view.findViewById(R.id.fragment_view_pager_tab_text);
        textView.setText("Tab C");
    }
}
