package com.shipdream.lib.android.mvc.view.viewpager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.shipdream.lib.android.mvc.view.MvcFragment;
import com.shipdream.lib.android.mvc.view.test.R;

public class ViewPagerHomeFragment extends MvcFragment {
    ViewPager viewPager;

    private PagerAdapter pagerAdapter;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_view_pager_home;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        if (reason == Reason.FIRST_TIME || reason == Reason.RESTORE) {
            pagerAdapter = new PagerAdapter(getChildFragmentManager());
        }

        viewPager.setAdapter(pagerAdapter);
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
