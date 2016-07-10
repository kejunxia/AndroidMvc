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

import com.shipdream.lib.android.mvc.BaseTestCase;
import com.shipdream.lib.android.mvc.Forwarder;
import com.shipdream.lib.android.mvc.Mvc;
import com.shipdream.lib.android.mvc.MvcComponent;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerA;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerB;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerC;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerD;
import com.shipdream.lib.poke.Graph;

import org.junit.Assert;
import org.junit.Test;

public class TestCaseNavigationAndInjection extends BaseTestCase<MvcTestActivityNavigation> {
    private CountMonitor monitor;

    public TestCaseNavigationAndInjection() {
        super(MvcTestActivityNavigation.class);
    }

    abstract class CountMonitor implements Graph.Monitor {
    }

    @Override
    protected void prepareDependencies(MvcComponent testComponent) throws Exception {
        super.prepareDependencies(testComponent);

        monitor = new CountMonitor() {
            @Override
            public void onInject(Object target) {
                if (target instanceof NavFragment) {
                    MvcTestActivityNavigation act = activity;
                    if (activity == null) {
                        act = ((MvcTestActivityNavigation) ((NavFragment) target).getActivity());
                    }

                    if (target instanceof NavFragmentA) {
                        act.fragAInjectCount++;
                    } else if (target instanceof NavFragmentB) {
                        act.fragBInjectCount++;
                    } else if (target instanceof NavFragmentC) {
                        act.fragCInjectCount++;
                    } else if (target instanceof NavFragmentD) {
                        act.fragDInjectCount++;
                    }
                }
            }

            @Override
            public void onRelease(Object target) {
                if (target instanceof NavFragment) {
                    MvcTestActivityNavigation act = activity;
                    if (activity == null) {
                        act = ((MvcTestActivityNavigation) ((NavFragment) target).getActivity());
                    }

                    if (target instanceof NavFragmentA) {
                        act.fragAReleaseCount++;
                    } else if (target instanceof NavFragmentB) {
                        act.fragBReleaseCount++;
                    } else if (target instanceof NavFragmentC) {
                        act.fragCReleaseCount++;
                    } else if (target instanceof NavFragmentD) {
                        act.fragDReleaseCount++;
                    }
                }
            }
        };

        Mvc.graph().registerMonitor(monitor);
    }

    @Override
    public void tearDown() throws Exception {
        Mvc.graph().unregisterMonitor(monitor);
        super.tearDown();
    }

    @Test
    public void test_should_reinject_last_fragment_and_release_top_fragment_on_single_step_back_navigation() throws Throwable {
        prepareAndCheckStack(true);
        //->A->B->C->D
        FragmentManager fm = activity.getRootFragmentManager();

        resetGraphMonitorCounts();
        navigateBackByFragment();
        //->A->B->C
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
        Assert.assertTrue(fm.getFragments().get(2) instanceof NavFragmentC);
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 3);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(ControllerB.class.getSimpleName()));
        Assert.assertTrue(fm.getBackStackEntryAt(2).getName().contains(ControllerC.class.getSimpleName()));
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 0);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 0);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 0);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigateBackByFragment();
        //->A->B
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 2);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(ControllerB.class.getSimpleName()));
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 0);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 0);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 1);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigateBackByFragment();
        //->A
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertNull(fm.getFragments().get(1));
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 0);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 1);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 0);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigateBackByFragment();

        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertNull(fm.getFragments().get(0));
        Assert.assertNull(fm.getFragments().get(1));
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 1);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 0);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 0);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 0);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_new_fragment_on_forward_nav_with_fast_rewind_to_specific_location() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        waitTest();
        resetGraphMonitorCounts();
        //Now clear history up to A and put C on it. Then A should pop out without re
        navTo(ControllerC.class, new Forwarder().clearTo(ControllerA.class));
        //->A->C
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentC);
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 2);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(ControllerC.class.getSimpleName()));
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 0);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 1);
        Assert.assertEquals(activity.fragCInjectCount, 1);
        Assert.assertEquals(activity.fragCReleaseCount, 1);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 1);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_new_fragment_on_forward_nav_with_clearing_all_history_locations() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        waitTest();
        resetGraphMonitorCounts();
        //Now clear history up to A and put C on it. Then A should pop out without re
        navTo(ControllerB.class, new Forwarder().clearAll());
        //->B
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentB);
        Assert.assertNull(fm.getFragments().get(1));
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerB.class.getSimpleName()));
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 1);
        Assert.assertEquals(activity.fragBInjectCount, 1);
        Assert.assertEquals(activity.fragBReleaseCount, 1);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 1);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 1);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_home_fragment_with_fast_rewind_to_specific_location() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        waitTest();
        resetGraphMonitorCounts();
        navigationManager.navigate(this).back(ControllerB.class);
        //->A->B
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 2);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(ControllerB.class.getSimpleName()));
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 0);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 0);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 1);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 1);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_home_fragment_when_clear_all_history_to_home_location_without_duplicate_history_items() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        waitTest();
        resetGraphMonitorCounts();
        navigationManager.navigate(this).back(null);
        //->A
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertNull(fm.getFragments().get(1));
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 0);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 1);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 1);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 1);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_home_fragment_when_clear_all_history_to_home_location_with_duplicate_history_items() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        navTo(ControllerA.class);
        //->A->B->C->D->A
        waitTest();
        navTo(ControllerC.class);
        //->A->B->C->D->A->C
        waitTest();

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back(null);
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
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 1);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 1);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 2);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 1);
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
        bringBack(pressHome());
        waitTest();
        Thread.sleep(1000);

        Assert.assertEquals(activity.fragAInjectCount, 1);
        Assert.assertEquals(activity.fragAReleaseCount, 1);
        Assert.assertEquals(activity.fragBInjectCount, 1);
        Assert.assertEquals(activity.fragBReleaseCount, 1);
        Assert.assertEquals(activity.fragCInjectCount, 1);
        Assert.assertEquals(activity.fragCReleaseCount, 1);
        Assert.assertEquals(activity.fragDInjectCount, 1);
        Assert.assertEquals(activity.fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigateBackByFragment();
        //->A->B->C
        waitTest();
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 0);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 0);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 0);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigateBackByFragment();
        //->A->B
        waitTest();
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 0);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 0);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 1);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigateBackByFragment();
        //->A
        waitTest();
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 0);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 1);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 0);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigateBackByFragment();
        //quit
        waitTest();
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 1);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 0);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 0);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 0);
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
        bringBack(pressHome());
        waitTest();
        Thread.sleep(1000);

        Assert.assertEquals(activity.fragAInjectCount, 1);
        Assert.assertEquals(activity.fragAReleaseCount, 1);
        Assert.assertEquals(activity.fragBInjectCount, 1);
        Assert.assertEquals(activity.fragBReleaseCount, 1);
        Assert.assertEquals(activity.fragCInjectCount, 1);
        Assert.assertEquals(activity.fragCReleaseCount, 1);
        Assert.assertEquals(activity.fragDInjectCount, 1);
        Assert.assertEquals(activity.fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back(null);
        Thread.sleep(800);
        //->A
        waitTest();
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 0);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 1);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 1);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigateBackByFragment();
        //quit
        waitTest();
        Thread.sleep(500);

        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 1);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 0);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 0);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 0);
    }

    @Test
    public void test_should_release_and_inject_properly_on_fast_rewind_forward_navigation_after_being_killed_by_OS() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), getClass().getSimpleName() + " not tested as Don't Keep Activities setting is disabled");
            return;
        }

        prepareAndCheckStack();
        //->A->B->C->D
        waitTest();
        resetGraphMonitorCounts();
        bringBack(pressHome());
        Thread.sleep(1000);

        Assert.assertEquals(activity.fragAInjectCount, 1);
        Assert.assertEquals(activity.fragAReleaseCount, 1);
        Assert.assertEquals(activity.fragBInjectCount, 1);
        Assert.assertEquals(activity.fragBReleaseCount, 1);
        Assert.assertEquals(activity.fragCInjectCount, 1);
        Assert.assertEquals(activity.fragCReleaseCount, 1);
        Assert.assertEquals(activity.fragDInjectCount, 1);
        Assert.assertEquals(activity.fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navTo(ControllerA.class, new Forwarder().clearTo(ControllerB.class));
        //->A->B->A

        Assert.assertEquals(activity.fragAInjectCount, 1);
        Assert.assertEquals(activity.fragAReleaseCount, 0);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 0);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 1);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigateBackByFragment();
        //->A->B
        waitTest();
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 1);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 0);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 0);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigateBackByFragment();
        //->A
        waitTest();
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 0);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 1);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 0);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigateBackByFragment();
        //quit
        waitTest();
        Assert.assertEquals(activity.fragAInjectCount, 0);
        Assert.assertEquals(activity.fragAReleaseCount, 1);
        Assert.assertEquals(activity.fragBInjectCount, 0);
        Assert.assertEquals(activity.fragBReleaseCount, 0);
        Assert.assertEquals(activity.fragCInjectCount, 0);
        Assert.assertEquals(activity.fragCReleaseCount, 0);
        Assert.assertEquals(activity.fragDInjectCount, 0);
        Assert.assertEquals(activity.fragDReleaseCount, 0);
    }

    private void prepareAndCheckStack() throws InterruptedException {
        prepareAndCheckStack(false);
    }

    private void prepareAndCheckStack(boolean check) throws InterruptedException {
        if (check) {
            //The activity will navigate to fragment a on launch
            Assert.assertEquals(activity.fragAInjectCount, 0);
            Assert.assertEquals(activity.fragAReleaseCount, 0);
            Assert.assertEquals(activity.fragBInjectCount, 0);
            Assert.assertEquals(activity.fragBReleaseCount, 0);
            Assert.assertEquals(activity.fragCInjectCount, 0);
            Assert.assertEquals(activity.fragCReleaseCount, 0);
            Assert.assertEquals(activity.fragDInjectCount, 0);
            Assert.assertEquals(activity.fragDReleaseCount, 0);
        }

        FragmentManager fm = activity.getRootFragmentManager();
        //->A
        //should not take effect to navigate to the same location
        navTo(ControllerA.class);
        //->A
        if (check) {
            waitTest();
            Assert.assertEquals(fm.getFragments().size(), 1);
            Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
            Assert.assertEquals(fm.getBackStackEntryCount(), 1);
            Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));

            Assert.assertEquals(activity.fragAInjectCount, 1);
            Assert.assertEquals(activity.fragAReleaseCount, 0);
        }

        navTo(ControllerB.class);
        //->A->B
        if (check) {
            waitTest();
            Assert.assertEquals(fm.getFragments().size(), 2);
            Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
            Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
            Assert.assertEquals(fm.getBackStackEntryCount(), 2);
            Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
            Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(ControllerB.class.getSimpleName()));
            Assert.assertEquals(activity.fragAInjectCount, 1);
            Assert.assertEquals(activity.fragAReleaseCount, 0);
            Assert.assertEquals(activity.fragBInjectCount, 1);
            Assert.assertEquals(activity.fragBReleaseCount, 0);
        }

        navTo(ControllerC.class);
        //->A->B->C
        if (check) {
            waitTest();
            Assert.assertEquals(fm.getFragments().size(), 3);
            Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
            Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
            Assert.assertTrue(fm.getFragments().get(2) instanceof NavFragmentC);
            Assert.assertEquals(fm.getBackStackEntryCount(), 3);
            Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
            Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(ControllerB.class.getSimpleName()));
            Assert.assertTrue(fm.getBackStackEntryAt(2).getName().contains(ControllerC.class.getSimpleName()));
            Assert.assertEquals(activity.fragAInjectCount, 1);
            Assert.assertEquals(activity.fragAReleaseCount, 0);
            Assert.assertEquals(activity.fragBInjectCount, 1);
            Assert.assertEquals(activity.fragBReleaseCount, 0);
            Assert.assertEquals(activity.fragCInjectCount, 1);
            Assert.assertEquals(activity.fragCReleaseCount, 0);
        }

        navTo(ControllerD.class);
        //->A->B->C->D
        if (check) {
            waitTest();
            Assert.assertEquals(fm.getFragments().size(), 4);
            Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
            Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
            Assert.assertTrue(fm.getFragments().get(2) instanceof NavFragmentC);
            Assert.assertTrue(fm.getFragments().get(3) instanceof NavFragmentD);
            Assert.assertEquals(fm.getBackStackEntryCount(), 4);
            Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
            Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(ControllerB.class.getSimpleName()));
            Assert.assertTrue(fm.getBackStackEntryAt(2).getName().contains(ControllerC.class.getSimpleName()));
            Assert.assertTrue(fm.getBackStackEntryAt(3).getName().contains(ControllerD.class.getSimpleName()));
            Assert.assertEquals(activity.fragAInjectCount, 1);
            Assert.assertEquals(activity.fragAReleaseCount, 0);
            Assert.assertEquals(activity.fragBInjectCount, 1);
            Assert.assertEquals(activity.fragBReleaseCount, 0);
            Assert.assertEquals(activity.fragCInjectCount, 1);
            Assert.assertEquals(activity.fragCReleaseCount, 0);
            Assert.assertEquals(activity.fragDInjectCount, 1);
            Assert.assertEquals(activity.fragDReleaseCount, 0);
        }
    }

    private void resetGraphMonitorCounts() {
        activity.fragAInjectCount = 0;
        activity.fragBInjectCount = 0;
        activity.fragCInjectCount = 0;
        activity.fragDInjectCount = 0;
        activity.fragAReleaseCount = 0;
        activity.fragBReleaseCount = 0;
        activity.fragCReleaseCount = 0;
        activity.fragDReleaseCount = 0;
    }

}
