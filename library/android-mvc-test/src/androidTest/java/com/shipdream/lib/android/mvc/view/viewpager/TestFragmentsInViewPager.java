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

package com.shipdream.lib.android.mvc.view.viewpager;

import android.util.Log;

import com.shipdream.lib.android.mvc.view.BaseTestCase;
import com.shipdream.lib.android.mvc.view.LifeCycle;
import com.shipdream.lib.android.mvc.view.test.R;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

public class TestFragmentsInViewPager extends BaseTestCase <ViewPagerTestActivity> {
    public TestFragmentsInViewPager() {
        super(ViewPagerTestActivity.class);
    }

    @Test
    public void should_restore_controller_of_tab_a_after_swipe_away_then_swipe_back_to_tab_a() throws Throwable {
        onView(withText("Tab A")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab A")).check(matches(not(isDisplayed())));
        onView(withText("Tab B")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab B")).check(matches(not(isDisplayed())));
        onView(withText("Tab C")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Tab C")).check(matches(not(isDisplayed())));
        onView(withText("Tab B")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Tab B")).check(matches(not(isDisplayed())));
        onView(withText(TabFragmentA.RESTORE_TEXT)).check(matches(isDisplayed()));
    }

    @Test
    public void should_call_onViewReady_in_tab_fragments_when_resumed_hosting_fragment_pops_out() throws Throwable {
        if (isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "TestFragmentsInViewPager not tested as Don't Keep Activities setting is not disabled");
            return;
        }

        //=============================> At Home
        lifeCycleValidator.expect(
                LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyFirstTime);

        lifeCycleValidatorA.expect(
                LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyFirstTime);

        lifeCycleValidatorB.expect(
                LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyFirstTime);

        lifeCycleValidatorC.expect();

        //=============================> At Sub Fragment
        navigationController.navigateTo(this, SubFragment.class.getSimpleName());
        waitTest(1200);

        lifeCycleValidator.expect(LifeCycle.onPushingToBackStack, LifeCycle.onDestroyView);
        lifeCycleValidatorA.expect(LifeCycle.onDestroyView);
        lifeCycleValidatorB.expect(LifeCycle.onDestroyView);
        lifeCycleValidatorC.expect();

        pressHome();
        waitTest(1200);

        bringBack();
        waitTest(1200);
        lifeCycleValidator.expect();

        lifeCycleValidatorA.expect();

        lifeCycleValidatorB.expect();

        lifeCycleValidatorC.expect();

        //=============================> At A
        navigationController.navigateBack(this);
        waitTest(1200);
        lifeCycleValidator.expect(
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyFirstTime,
                LifeCycle.onViewReadyPopOut,
                LifeCycle.onPoppedOutToFront);

        lifeCycleValidatorA.expect(
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyFirstTime,
                LifeCycle.onViewReadyPopOut,
                LifeCycle.onPoppedOutToFront);

        lifeCycleValidatorB.expect(
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyFirstTime,
                LifeCycle.onViewReadyPopOut,
                LifeCycle.onPoppedOutToFront);

        lifeCycleValidatorC.expect();
    }

    @Test
    public void should_call_onViewReady_in_tab_fragments_when_restored_hosting_fragment_pops_out() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "TestFragmentsInViewPager not tested as Don't Keep Activities setting is disabled");
            return;
        }

        //=============================> At Home
        lifeCycleValidator.expect(
                LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyFirstTime);

        lifeCycleValidatorA.expect(
                LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyFirstTime);

        lifeCycleValidatorB.expect(
                LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyFirstTime);

        lifeCycleValidatorC.expect();

        //=============================> At Sub Fragment
        navigationController.navigateTo(this, SubFragment.class.getSimpleName());
        waitTest(1200);

        lifeCycleValidator.expect(LifeCycle.onPushingToBackStack, LifeCycle.onDestroyView);
        lifeCycleValidatorA.expect(LifeCycle.onDestroyView);
        lifeCycleValidatorB.expect(LifeCycle.onDestroyView);
        lifeCycleValidatorC.expect();

        pressHome();
        waitTest(1200);

        bringBack();
        waitTest(1200);
        lifeCycleValidator.expect(
                LifeCycle.onDestroy,
                LifeCycle.onCreateNotNull);

        lifeCycleValidatorA.expect(
                LifeCycle.onDestroy,
                LifeCycle.onCreateNotNull);

        lifeCycleValidatorB.expect(
                LifeCycle.onDestroy,
                LifeCycle.onCreateNotNull);

        lifeCycleValidatorC.expect();

        //=============================> At A
        navigationController.navigateBack(this);
        waitTest(1200);

        lifeCycleValidator.expect(
                LifeCycle.onCreateViewNotNull,
                LifeCycle.onViewCreatedNotNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyRestore,
                LifeCycle.onViewReadyPopOut,
                LifeCycle.onPoppedOutToFront);

        lifeCycleValidatorA.expect(
                LifeCycle.onCreateViewNotNull,
                LifeCycle.onViewCreatedNotNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyRestore,
                LifeCycle.onViewReadyPopOut,
                LifeCycle.onPoppedOutToFront);

        lifeCycleValidatorB.expect(
                LifeCycle.onCreateViewNotNull,
                LifeCycle.onViewCreatedNotNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyRestore,
                LifeCycle.onViewReadyPopOut,
                LifeCycle.onPoppedOutToFront);

        lifeCycleValidatorC.expect();
    }

    @Test
    public void should_call_onViewReady_in_tab_fragments_when_comes_back_from_another_activity() throws Throwable {
        if (isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "TestFragmentsInViewPager not tested as Don't Keep Activities setting is enabled");
            return;
        }

        //=============================> At Home
        lifeCycleValidator.expect(
                LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyFirstTime);

        lifeCycleValidatorA.expect(
                LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyFirstTime);

        lifeCycleValidatorB.expect(
                LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyFirstTime);

        lifeCycleValidatorC.expect();

        onView(withText("Tab A")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab A")).check(matches(not(isDisplayed())));
        onView(withText("Tab B")).check(matches(isDisplayed()));

        //=============================> At Sub Fragment
        getActivity().launchAnotherActivity();
        waitTest(1200);
        pressBack();
        waitTest(1200);
        lifeCycleValidatorA.expect(LifeCycle.onReturnForeground);

        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab B")).check(matches(not(isDisplayed())));
        onView(withText("Tab C")).check(matches(isDisplayed()));
        waitTest(1000);
        lifeCycleValidatorA.expect(LifeCycle.onDestroyView, LifeCycle.onDestroy);

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Tab C")).check(matches(not(isDisplayed())));
        onView(withText("Tab B")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Tab B")).check(matches(not(isDisplayed())));
        onView(withText(TabFragmentA.RESTORE_TEXT)).check(matches(isDisplayed()));

        lifeCycleValidatorA.expect(
                LifeCycle.onCreateNotNull,
                LifeCycle.onCreateViewNotNull,
                LifeCycle.onViewCreatedNotNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyRestore);
    }

    @Test
    public void should_call_onViewReady_in_tab_fragments_when_comes_back_from_another_activity_after_being_killed() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "TestFragmentsInViewPager not tested as Don't Keep Activities setting is disabled");
            return;
        }

        //=============================> At Home
        lifeCycleValidator.expect(
                LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyFirstTime);

        lifeCycleValidatorA.expect(
                LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyFirstTime);

        lifeCycleValidatorB.expect(
                LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyFirstTime);

        lifeCycleValidatorC.expect();

        onView(withText("Tab A")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab A")).check(matches(not(isDisplayed())));
        onView(withText("Tab B")).check(matches(isDisplayed()));

        //=============================> At Sub Fragment
        getActivity().launchAnotherActivity();
        waitTest(1200);
        pressBack();
        waitTest(1200);
        lifeCycleValidatorA.expect(
                LifeCycle.onDestroyView,
                LifeCycle.onDestroy,
                LifeCycle.onCreateNotNull,
                LifeCycle.onCreateViewNotNull,
                LifeCycle.onViewCreatedNotNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyRestore);

        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab B")).check(matches(not(isDisplayed())));
        onView(withText("Tab C")).check(matches(isDisplayed()));
        waitTest(1000);
        lifeCycleValidatorA.expect(LifeCycle.onDestroyView, LifeCycle.onDestroy);

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Tab C")).check(matches(not(isDisplayed())));
        onView(withText("Tab B")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Tab B")).check(matches(not(isDisplayed())));
        onView(withText(TabFragmentA.RESTORE_TEXT)).check(matches(isDisplayed()));

        lifeCycleValidatorA.expect(
                LifeCycle.onCreateNotNull,
                LifeCycle.onCreateViewNotNull,
                LifeCycle.onViewCreatedNotNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyRestore);
    }

    @Test
    public void should_call_onViewReady_with_pops_out_on_home_page_on_back_navigation() throws Throwable {
        //=============================> At Sub Fragment
        navigationController.navigateTo(this, SubFragment.class.getSimpleName());
        waitTest(1200);

        lifeCycleValidator.reset();

        //=============================> At A
        navigationController.navigateBack(this);
        waitTest(1200);

        lifeCycleValidator.expect(
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyFirstTime,
                LifeCycle.onViewReadyPopOut,
                LifeCycle.onPoppedOutToFront);
    }

}
