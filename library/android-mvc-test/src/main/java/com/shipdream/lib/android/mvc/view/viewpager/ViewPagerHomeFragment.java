package com.shipdream.lib.android.mvc.view.viewpager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.shipdream.lib.android.mvc.view.MvcApp;
import com.shipdream.lib.android.mvc.view.MvcFragment;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitor;
import com.shipdream.lib.android.mvc.view.test.R;

public class ViewPagerHomeFragment extends MvcFragment {
    ViewPager viewPager;

    private PagerAdapter pagerAdapter;

    private LifeCycleMonitor lifeCycleMonitor = MvcApp.lifeCycleMonitorFactory.provideLifeCycleMonitor();
    
    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_view_pager_home;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        lifeCycleMonitor.onCreateView(view, savedInstanceState);
        lifeCycleMonitor.onViewCreated(view, savedInstanceState);
        super.onViewReady(view, savedInstanceState, reason);
        lifeCycleMonitor.onViewReady(view, savedInstanceState, reason);

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        if (reason == Reason.FIRST_TIME || reason == Reason.RESTORE) {
            pagerAdapter = new PagerAdapter(getChildFragmentManager());
        }

        viewPager.setAdapter(pagerAdapter);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifeCycleMonitor.onCreate(savedInstanceState);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        lifeCycleMonitor.onResume();
    }

    @Override
    protected void onReturnForeground() {
        super.onReturnForeground();
        lifeCycleMonitor.onReturnForeground();
    }

    @Override
    protected void onPushingToBackStack() {
        super.onPushingToBackStack();
        lifeCycleMonitor.onPushingToBackStack();
    }

    @Override
    protected void onPoppedOutToFront() {
        super.onPoppedOutToFront();
        lifeCycleMonitor.onPoppedOutToFront();
    }

    @Override
    protected void onOrientationChanged(int lastOrientation, int currentOrientation) {
        super.onOrientationChanged(lastOrientation, currentOrientation);
        lifeCycleMonitor.onOrientationChanged(lastOrientation, currentOrientation);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        lifeCycleMonitor.onDestroyView();
    }

    @Override
    public void onDestroy() {
        lifeCycleMonitor.onDestroy();
        super.onDestroy();
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        private Class<? extends MvcFragment>[] tabs = new Class[]{
                TabFragmentA.class,
                TabFragmentB.class,
                TabFragmentC.class
        };


        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Tab A";
                case 1:
                    return "Tab B";
                case 2:
                default:
                    return "Tab C";
            }
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            try {
                Class<?> clazz = Class.forName(tabs[position].getName());
                fragment = (Fragment) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Can't instantiate fragment - "
                        + fragment.getClass().getName(), e);
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return tabs.length;
        }
    }
}
