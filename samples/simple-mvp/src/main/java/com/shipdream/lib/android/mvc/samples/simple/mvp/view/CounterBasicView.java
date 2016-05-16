package com.shipdream.lib.android.mvc.samples.simple.mvp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.shipdream.lib.android.mvp.manager.NavigationManager;
import com.shipdream.lib.android.mvc.samples.simple.mvp.R;
import com.shipdream.lib.android.mvc.samples.simple.mvp.presenter.CounterBasicPresenter;
import com.shipdream.lib.android.mvp.view.MvcFragment;

import javax.inject.Inject;

public class CounterBasicView extends MvcFragment implements CounterBasicPresenter.View {
    @Inject
    private NavigationManager navigationManager;

    @Inject
    private CounterBasicPresenter presenter;

    private TextView display;
    private Button increment;
    private Button decrement;
    private Button buttonShowAdvancedView;

    /**
     * @return Layout id used to inflate the view of this MvcFragment.
     */
    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_counter_entry;
    }

    /**
     * Lifecycle similar to onViewCreated by with more granular control with an extra argument to
     * indicate why this view is created: 1. first time created, or 2. rotated or 3. restored
     * @param view The root view of the fragment
     * @param savedInstanceState The savedInstanceState when the fragment is being recreated after
     *                           its enclosing activity is killed by OS, otherwise null including on
     *                           rotation
     * @param reason Indicates the {@link Reason} why the onViewReady is called.
     */
    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        presenter.view = this;

        display = (TextView) view.findViewById(R.id.fragment_a_counterDisplay);
        increment = (Button) view.findViewById(R.id.fragment_a_buttonIncrement);
        decrement = (Button) view.findViewById(R.id.fragment_a_buttonDecrement);
        buttonShowAdvancedView = (Button) view.findViewById(R.id.fragment_a_buttonShowAdvancedView);

        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.increment(v);
            }
        });

        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.decrement(v);
            }
        });

        buttonShowAdvancedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Use counterController to manage navigation to make navigation testable
                presenter.goToDetailView(v);
                //Or we can use NavigationManager directly though it's harder to unit test on
                //controller level.
                //example:
                //navigationManager.navigateTo(v, "LocationB");
            }
        });

        if (reason.isFirstTime()) {
            CounterSubView f = new CounterSubView();

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_a_anotherFragmentContainer, f).commit();
        }

        updateCountDisplay(presenter.getCount());
    }

    /**
     * Callback when the fragment is popped out by back navigation
     */
    @Override
    protected void onPoppedOutToFront() {
        super.onPoppedOutToFront();
        updateCountDisplay(presenter.getCount());
    }

    /**
     * Update the text view of count number
     * @param count The number of count
     */
    private void updateCountDisplay(int count) {
        display.setText(String.valueOf(count));
    }

    @Override
    public void onCounterUpdated(int count, String countInEnglish) {
        updateCountDisplay(count);
    }
}
