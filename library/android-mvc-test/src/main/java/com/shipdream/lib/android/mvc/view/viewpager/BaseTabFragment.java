package com.shipdream.lib.android.mvc.view.viewpager;

import android.os.Bundle;
import android.view.View;

import com.shipdream.lib.android.mvc.view.MvcFragment;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitor;

public abstract class BaseTabFragment extends MvcFragment {
    protected abstract LifeCycleMonitor getLifeCycleMonitor();

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, MvcFragment.Reason reason) {
        getLifeCycleMonitor().onCreateView(view, savedInstanceState);
        getLifeCycleMonitor().onViewCreated(view, savedInstanceState);
        super.onViewReady(view, savedInstanceState, reason);
        getLifeCycleMonitor().onViewReady(view, savedInstanceState, reason);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifeCycleMonitor().onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLifeCycleMonitor().onResume();
    }

    @Override
    protected void onReturnForeground() {
        super.onReturnForeground();
        getLifeCycleMonitor().onReturnForeground();
    }

    @Override
    protected void onPushingToBackStack() {
        super.onPushingToBackStack();
        getLifeCycleMonitor().onPushingToBackStack();
    }

    @Override
    protected void onPoppedOutToFront() {
        super.onPoppedOutToFront();
        getLifeCycleMonitor().onPoppedOutToFront();
    }

    @Override
    protected void onOrientationChanged(int lastOrientation, int currentOrientation) {
        super.onOrientationChanged(lastOrientation, currentOrientation);
        getLifeCycleMonitor().onOrientationChanged(lastOrientation, currentOrientation);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLifeCycleMonitor().onDestroyView();
    }

    @Override
    public void onDestroy() {
        getLifeCycleMonitor().onDestroy();
        super.onDestroy();
    }
}
