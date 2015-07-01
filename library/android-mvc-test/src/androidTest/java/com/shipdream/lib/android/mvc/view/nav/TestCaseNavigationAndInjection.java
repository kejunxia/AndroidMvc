/*
 * Copyright 2015 Kejun Xia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shipdream.lib.android.mvc.view.nav;

import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.shipdream.lib.android.mvc.view.AndroidMvc;
import com.shipdream.lib.android.mvc.view.BaseTestCase;
import com.shipdream.lib.poke.Graph;
import com.shipdream.lib.poke.ScopeCache;

import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

public class TestCaseNavigationAndInjection extends BaseTestCase <MvcTestActivityNavigation> {
    @Inject
    private AnotherController anotherController;

    private int fragAInjectCount = 0;
    private int fragBInjectCount = 0;
    private int fragCInjectCount = 0;
    private int fragDInjectCount = 0;
    private int fragAReleaseCount = 0;
    private int fragBReleaseCount = 0;
    private int fragCReleaseCount = 0;
    private int fragDReleaseCount = 0;

    public TestCaseNavigationAndInjection() {
        super(MvcTestActivityNavigation.class);
    }

    @Override
    protected void waitTest() throws InterruptedException {
        waitTest(800);
    }

    @Override
    protected void injectDependencies(ScopeCache mvcSingletonCache) {
        super.injectDependencies(mvcSingletonCache);
        resetGraphMonitorCounts();

        AndroidMvc.graph().registerMonitor(new Graph.Monitor() {
            @Override
            public void onInject(Object target) {
                if (target instanceof NavFragmentA) {
                    fragAInjectCount++;
                } else if (target instanceof NavFragmentB) {
                    fragBInjectCount++;
                } else if (target instanceof NavFragmentC) {
                    fragCInjectCount++;
                } else if (target instanceof NavFragmentD) {
                    fragDInjectCount++;
                }
            }

            @Override
            public void onRelease(Object target) {
                if (target instanceof NavFragmentA) {
                    fragAReleaseCount++;
                } else if (target instanceof NavFragmentB) {
                    fragBReleaseCount++;
                } else if (target instanceof NavFragmentC) {
                    fragCReleaseCount++;
                } else if (target instanceof NavFragmentD) {
                    fragDReleaseCount++;
                }
            }
        });
    }

    @Test
    public void test_should_reinject_last_fragment_and_release_top_fragment_on_single_step_back_navigation() throws Throwable {
        prepareAndCheckStack();
        //->A->B->C->D
        FragmentManager fm = activity.getRootFragmentManager();

        resetGraphMonitorCounts();
        navigationController.navigateBack(this);
        //->A->B->C
        waitTest(1000);
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
        Assert.assertTrue(fm.getFragments().get(2) instanceof NavFragmentC);
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 3);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(MvcTestActivityNavigation.Loc.A));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(MvcTestActivityNavigation.Loc.B));
        Assert.assertTrue(fm.getBackStackEntryAt(2).getName().contains(MvcTestActivityNavigation.Loc.C));
        Assert.assertEquals(fragAInjectCount, 0);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 1);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationController.navigateBack(this);
        //->A->B
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 2);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(MvcTestActivityNavigation.Loc.A));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(MvcTestActivityNavigation.Loc.B));
        Assert.assertEquals(fragAInjectCount, 0);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 1);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 1);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigationController.navigateBack(this);
        //->A
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertNull(fm.getFragments().get(1));
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(MvcTestActivityNavigation.Loc.A));
        Assert.assertEquals(fragAInjectCount, 1);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 1);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigationController.navigateBack(this);
        //quit
        waitTest(2000);
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertNull(fm.getFragments().get(0));
        Assert.assertNull(fm.getFragments().get(1));
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertEquals(fragAInjectCount, 0);
        Assert.assertEquals(fragAReleaseCount, 1);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 0);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_new_fragment_on_forward_nav_with_fast_rewind_to_specific_location() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        resetGraphMonitorCounts();
        //Now clear history up to A and put C on it. Then A should pop out without re
        navigationController.navigateTo(this, MvcTestActivityNavigation.Loc.C, MvcTestActivityNavigation.Loc.A);
        //->A->C
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentC);
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 2);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(MvcTestActivityNavigation.Loc.A));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(MvcTestActivityNavigation.Loc.C));
        Assert.assertEquals(fragAInjectCount, 1);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 1);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 1);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_new_fragment_on_forward_nav_with_clearing_all_history_locations() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        resetGraphMonitorCounts();
        //Now clear history up to A and put C on it. Then A should pop out without re
        navigationController.navigateTo(this, MvcTestActivityNavigation.Loc.B, null);
        //->B
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentB);
        Assert.assertNull(fm.getFragments().get(1));
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(MvcTestActivityNavigation.Loc.B));
        Assert.assertEquals(fragAInjectCount, 0);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 1);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 1);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_home_fragment_with_fast_rewind_to_specific_location() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        resetGraphMonitorCounts();
        navigationController.navigateBack(this, MvcTestActivityNavigation.Loc.B);
        //->A->B
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 2);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(MvcTestActivityNavigation.Loc.A));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(MvcTestActivityNavigation.Loc.B));
        Assert.assertEquals(fragAInjectCount, 0);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 1);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 1);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_home_fragment_when_clear_all_history_to_home_location_without_duplicate_history_items() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        resetGraphMonitorCounts();
        navigationController.navigateBack(this, null);
        //->A
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertNull(fm.getFragments().get(1));
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(MvcTestActivityNavigation.Loc.A));
        Assert.assertEquals(fragAInjectCount, 1);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 1);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_home_fragment_when_clear_all_history_to_home_location_with_duplicate_history_items() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        navigationController.navigateTo(this, MvcTestActivityNavigation.Loc.A);
        //->A->B->C->D->A
        waitTest();
        navigationController.navigateTo(this, MvcTestActivityNavigation.Loc.C);
        //->A->B->C->D->A->C
        waitTest();

        resetGraphMonitorCounts();
        navigationController.navigateBack(this, null);
        //->A
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 6);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertNull(fm.getFragments().get(1));
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertNull(fm.getFragments().get(4));
        Assert.assertNull(fm.getFragments().get(5));
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(MvcTestActivityNavigation.Loc.A));
        Assert.assertEquals(2, fragAInjectCount);
        Assert.assertTrue(fragAInjectCount == fragAReleaseCount + 1);
        Assert.assertEquals(1, fragBReleaseCount);
        Assert.assertTrue(fragBInjectCount == fragBReleaseCount);
        Assert.assertEquals(1, fragCInjectCount);
        Assert.assertTrue(fragCReleaseCount == fragCInjectCount + 1);
        Assert.assertEquals(1, fragDInjectCount);
        Assert.assertTrue(fragDInjectCount == fragDReleaseCount);
    }

    @Test
    public void test_should_release_and_inject_properly_on_single_step_back_navigation_after_being_killed_by_OS() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), getClass().getSimpleName() + " not tested as Don't Keep Activities setting is disabled");
            return;
        }

        prepareAndCheckStack();
        //->A->B->C->D

        resetGraphMonitorCounts();
        pressHome();
        waitTest();
        bringBack();
        waitTest(2000);

        Assert.assertEquals(fragAInjectCount, 1);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 1);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 1);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 1);
        Assert.assertEquals(fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationController.navigateBack(this);
        //->A->B->C
        waitTest(1000);
        Assert.assertEquals(fragAInjectCount, 0);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationController.navigateBack(this);
        //->A->B
        waitTest();
        Assert.assertEquals(fragAInjectCount, 0);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 1);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigationController.navigateBack(this);
        //->A
        waitTest();
        Assert.assertEquals(fragAInjectCount, 0);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 1);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigationController.navigateBack(this);
        //quit
        waitTest();
        Assert.assertEquals(fragAInjectCount, 0);
        Assert.assertEquals(fragAReleaseCount, 1);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 0);
    }

    @Test
    public void test_should_release_and_inject_properly_on_fast_rewind_back_navigation_after_being_killed_by_OS() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), getClass().getSimpleName() + " not tested as Don't Keep Activities setting is disabled");
            return;
        }

        prepareAndCheckStack();
        //->A->B->C->D

        resetGraphMonitorCounts();
        pressHome();
        waitTest(1000);
        bringBack();
        waitTest(1000);

        Assert.assertEquals(fragAInjectCount, 1);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 1);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 1);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 1);
        Assert.assertEquals(fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationController.navigateBack(this, null);
        //->A->B->C
        waitTest();
        Assert.assertEquals(fragAInjectCount, 0);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 1);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 1);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationController.navigateBack(this);
        //quit
        waitTest();
        Assert.assertEquals(fragAInjectCount, 0);
        Assert.assertEquals(fragAReleaseCount, 1);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 0);
    }

    @Test
    public void test_should_release_and_inject_properly_on_fast_rewind_forward_navigation_after_being_killed_by_OS() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), getClass().getSimpleName() + " not tested as Don't Keep Activities setting is disabled");
            return;
        }

        prepareAndCheckStack();
        //->A->B->C->D

        resetGraphMonitorCounts();
        pressHome();
        waitTest();
        bringBack();
        waitTest(2000);

        Assert.assertEquals(fragAInjectCount, 1);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 1);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 1);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 1);
        Assert.assertEquals(fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationController.navigateTo(this, MvcTestActivityNavigation.Loc.A, MvcTestActivityNavigation.Loc.B);
        //->A->B->A
        waitTest();
        Assert.assertEquals(fragAInjectCount, 1);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 1);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationController.navigateBack(this);
        //->A->B
        waitTest();
        Assert.assertEquals(fragAInjectCount, 0);
        Assert.assertEquals(fragAReleaseCount, 1);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigationController.navigateBack(this);
        //->A
        waitTest();
        Assert.assertEquals(fragAInjectCount, 0);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 1);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigationController.navigateBack(this);
        //quit
        waitTest();
        Assert.assertEquals(fragAInjectCount, 0);
        Assert.assertEquals(fragAReleaseCount, 1);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 0);
    }

    private void prepareAndCheckStack() throws InterruptedException {
        //The activity will navigate to fragment a on launch
        Assert.assertEquals(fragAInjectCount, 1);
        Assert.assertEquals(fragAReleaseCount, 0);
        Assert.assertEquals(fragBInjectCount, 0);
        Assert.assertEquals(fragBReleaseCount, 0);
        Assert.assertEquals(fragCInjectCount, 0);
        Assert.assertEquals(fragCReleaseCount, 0);
        Assert.assertEquals(fragDInjectCount, 0);
        Assert.assertEquals(fragDReleaseCount, 0);

        FragmentManager fm = activity.getRootFragmentManager();
        //->A
        //should not take effect to navigate to the same location
        navigationController.navigateTo(this, MvcTestActivityNavigation.Loc.A);
        //->A
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 1);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(MvcTestActivityNavigation.Loc.A));

        Assert.assertEquals(fragAInjectCount, 1);
        Assert.assertEquals(fragAReleaseCount, 0);

        navigationController.navigateTo(this, MvcTestActivityNavigation.Loc.B);
        //->A->B
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 2);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
        Assert.assertEquals(fm.getBackStackEntryCount(), 2);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(MvcTestActivityNavigation.Loc.A));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(MvcTestActivityNavigation.Loc.B));
        Assert.assertEquals(fragAInjectCount, 1);
        Assert.assertEquals(fragAReleaseCount, 1);
        Assert.assertEquals(fragBInjectCount, 1);
        Assert.assertEquals(fragBReleaseCount, 0);

        navigationController.navigateTo(this, MvcTestActivityNavigation.Loc.C);
        //->A->B->C
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 3);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
        Assert.assertTrue(fm.getFragments().get(2) instanceof NavFragmentC);
        Assert.assertEquals(fm.getBackStackEntryCount(), 3);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(MvcTestActivityNavigation.Loc.A));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(MvcTestActivityNavigation.Loc.B));
        Assert.assertTrue(fm.getBackStackEntryAt(2).getName().contains(MvcTestActivityNavigation.Loc.C));
        Assert.assertEquals(fragAInjectCount, 1);
        Assert.assertEquals(fragAReleaseCount, 1);
        Assert.assertEquals(fragBInjectCount, 1);
        Assert.assertEquals(fragBReleaseCount, 1);
        Assert.assertEquals(fragCInjectCount, 1);
        Assert.assertEquals(fragCReleaseCount, 0);

        navigationController.navigateTo(this, MvcTestActivityNavigation.Loc.D);
        //->A->B->C->D
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
        Assert.assertTrue(fm.getFragments().get(2) instanceof NavFragmentC);
        Assert.assertTrue(fm.getFragments().get(3) instanceof NavFragmentD);
        Assert.assertEquals(fm.getBackStackEntryCount(), 4);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(MvcTestActivityNavigation.Loc.A));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(MvcTestActivityNavigation.Loc.B));
        Assert.assertTrue(fm.getBackStackEntryAt(2).getName().contains(MvcTestActivityNavigation.Loc.C));
        Assert.assertTrue(fm.getBackStackEntryAt(3).getName().contains(MvcTestActivityNavigation.Loc.D));
        Assert.assertEquals(fragAInjectCount, 1);
        Assert.assertEquals(fragAReleaseCount, 1);
        Assert.assertEquals(fragBInjectCount, 1);
        Assert.assertEquals(fragBReleaseCount, 1);
        Assert.assertEquals(fragCInjectCount, 1);
        Assert.assertEquals(fragCReleaseCount, 1);
        Assert.assertEquals(fragDInjectCount, 1);
        Assert.assertEquals(fragDReleaseCount, 0);
    }

    private void resetGraphMonitorCounts () {
        fragAInjectCount = 0;
        fragBInjectCount = 0;
        fragCInjectCount = 0;
        fragDInjectCount = 0;
        fragAReleaseCount = 0;
        fragBReleaseCount = 0;
        fragCReleaseCount = 0;
        fragDReleaseCount = 0;
    }

}
