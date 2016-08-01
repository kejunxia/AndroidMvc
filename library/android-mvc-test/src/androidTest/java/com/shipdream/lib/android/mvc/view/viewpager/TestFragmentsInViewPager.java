/*
 * Copyright 2016 Kejun Xia
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

import android.content.pm.ActivityInfo;
import android.util.Log;

import com.shipdream.lib.android.mvc.BaseTestCase;
import com.shipdream.lib.android.mvc.view.LifeCycle;
import com.shipdream.lib.android.mvc.view.test.R;
import com.shipdream.lib.android.mvc.view.viewpager.controller.SecondFragmentController;

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
    public void test_should_call_onViewReady_in_tab_fragments_when_resumed_hosting_fragment_pops_out() throws Throwable {
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
        navigationManager.navigate(this).to(SecondFragmentController.class);
        waitTest();

        lifeCycleValidator.expect(LifeCycle.onPushToBackStack, LifeCycle.onDestroyView);
        lifeCycleValidatorA.expect(LifeCycle.onPushToBackStack,LifeCycle.onDestroyView);
        lifeCycleValidatorB.expect(LifeCycle.onPushToBackStack,LifeCycle.onDestroyView);
        lifeCycleValidatorC.expect();

        bringBack(pressHome());

        lifeCycleValidator.expect();

        lifeCycleValidatorA.expect();

        lifeCycleValidatorB.expect();

        lifeCycleValidatorC.expect();

        //=============================> At A
        navigateBackByFragment();
        waitTest();

        lifeCycleValidator.expect(
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyPopOut,
                LifeCycle.onPoppedOutToFront);

        lifeCycleValidatorA.expect(
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyPopOut,
                LifeCycle.onPoppedOutToFront);

        lifeCycleValidatorB.expect(
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyPopOut,
                LifeCycle.onPoppedOutToFront);

        lifeCycleValidatorC.expect();
    }

    @Test
    public void test_should_call_onViewReady_in_tab_fragments_when_restored_hosting_fragment_pops_out() throws Throwable {
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
        navigationManager.navigate(this).to(SecondFragmentController.class);
        waitTest();

        lifeCycleValidator.expect(LifeCycle.onPushToBackStack, LifeCycle.onDestroyView);
        lifeCycleValidatorA.expect(LifeCycle.onPushToBackStack, LifeCycle.onDestroyView);
        lifeCycleValidatorB.expect(LifeCycle.onPushToBackStack, LifeCycle.onDestroyView);
        lifeCycleValidatorC.expect();

        bringBack(pressHome());

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

        //=============================> Back to home
        navigateBackByFragment();
        waitTest();

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

        navigationManager.navigate(this).back(null);
        navigateBackByFragment();
        waitTest();
    }

    @Test
    public void test_should_call_onViewReady_in_tab_fragments_when_comes_back_from_another_activity() throws Throwable {
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
        startActivity(getActivity().launchAnotherActivity());

        pressBack();
        waitTest();
        lifeCycleValidatorA.expect(LifeCycle.onReturnForeground);

        onView(withId(R.id.viewpager)).perform(swipeLeft());
        synchronized (this) {
            wait(1000);
        }
        onView(withText("Tab B")).check(matches(not(isDisplayed())));
        onView(withText("Tab C")).check(matches(isDisplayed()));
        lifeCycleValidatorA.expect(LifeCycle.onDestroyView);

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Tab C")).check(matches(not(isDisplayed())));
        onView(withText("Tab B")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Tab B")).check(matches(not(isDisplayed())));
        onView(withText("Restored TabA")).check(matches(isDisplayed()));

        lifeCycleValidatorA.expect(
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyFirstTime);
    }

    @Test
    public void test_should_call_onViewReady_in_tab_fragments_when_comes_back_from_another_activity_after_being_killed() throws Throwable {
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
        startActivity(getActivity().launchAnotherActivity());

        waitActivityResume(getActivity());
        pressBack();

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
        lifeCycleValidatorA.expect(LifeCycle.onDestroyView);

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Tab C")).check(matches(not(isDisplayed())));
        onView(withText("Tab B")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Tab B")).check(matches(not(isDisplayed())));
        onView(withText("Restored TabA")).check(matches(isDisplayed()));

        lifeCycleValidatorA.expect(
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyFirstTime);
    }

    @Test
    public void test_should_call_onViewReady_with_pops_out_on_home_page_on_back_navigation() throws Throwable {
        //=============================> At Sub Fragment
        navigationManager.navigate(this).to(SecondFragmentController.class);
        waitTest();

        lifeCycleValidator.reset();

        //=============================> At A
        navigateBackByFragment();

        lifeCycleValidator.expect(
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyPopOut,
                LifeCycle.onPoppedOutToFront);
    }

    @Test
    public void test_call_tab_controller_update_on_rotation() throws Throwable {
        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab C")).check(matches(isDisplayed()));

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        onView(withText("Tab C")).check(matches(isDisplayed()));
    }

    @Test
    public void test_call_tab_controller_update_on_swipe() throws Throwable {
        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab C")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab C")).check(matches(isDisplayed()));
    }

    @Test
    public void test_should_display_restored_text_when_fragment_A_destroyed_by_adapter() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "TestFragmentsInViewPager not tested as Don't Keep Activities setting is disabled");
            return;
        }

        //=============================> At Home
        onView(withText("Tab A")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab B")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab C")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Tab B")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Restored TabA")).check(matches(isDisplayed()));

        bringBack(pressHome());
        onView(withText("Restored TabA")).check(matches(isDisplayed()));

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        onView(withText("Restored TabA")).check(matches(isDisplayed()));

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        onView(withText("Restored TabA")).check(matches(isDisplayed()));
    }

    @Test
    public void test_should_display_restored_text_when_fragment_A_destroyed_OS_in_background() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "TestFragmentsInViewPager not tested as Don't Keep Activities setting is disabled");
            return;
        }

        //=============================> At Home
        onView(withText("Tab A")).check(matches(isDisplayed()));

        bringBack(pressHome());
        onView(withText("Restored TabA")).check(matches(isDisplayed()));

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        onView(withText("Restored TabA")).check(matches(isDisplayed()));

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        onView(withText("Restored TabA")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab B")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab C")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Tab B")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Restored TabA")).check(matches(isDisplayed()));
    }

    @Test
    public void test_should_display_restored_text_when_fragment_A_destroyed_by_pushed_into_back_stack() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "TestFragmentsInViewPager not tested as Don't Keep Activities setting is disabled");
            return;
        }

        //=============================> At Home
        onView(withText("Tab A")).check(matches(isDisplayed()));

        navigationManager.navigate(this).to(SecondFragmentController.class);
        navigationManager.navigate(this).back();

        onView(withText("Tab A")).check(matches(isDisplayed()));

        bringBack(pressHome());
        navigationManager.navigate(this).to(SecondFragmentController.class);
        navigationManager.navigate(this).back();

        onView(withText("Restored TabA")).check(matches(isDisplayed()));
    }

    @Test
    public void test_should_display_restored_text_when_fragment_A_destroyed_by_pushed_into_back_stack2() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "TestFragmentsInViewPager not tested as Don't Keep Activities setting is disabled");
            return;
        }

        //=============================> At Home
        onView(withText("Tab A")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab B")).check(matches(isDisplayed()));

        navigationManager.navigate(this).to(SecondFragmentController.class);
        String key = pressHome();
        bringBack(key);
        navigationManager.navigate(this).back();

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Restored TabA")).check(matches(isDisplayed()));
    }

    @Test
    public void test_should_display_restored_text_when_fragment_A_destroyed_by_pushed_into_back_stack3() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "TestFragmentsInViewPager not tested as Don't Keep Activities setting is disabled");
            return;
        }

        //=============================> At Home
        onView(withText("Tab A")).check(matches(isDisplayed()));

        onView(withId(R.id.viewpager)).perform(swipeLeft());
        onView(withText("Tab B")).check(matches(isDisplayed()));

        bringBack(pressHome());

        onView(withId(R.id.viewpager)).perform(swipeRight());
        onView(withText("Restored TabA")).check(matches(isDisplayed()));
    }

    @Override
    protected Class<ViewPagerTestActivity> getActivityClass() {
        return ViewPagerTestActivity.class;
    }
}
