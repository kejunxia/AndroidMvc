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

package com.shipdream.lib.android.mvp.view.injection;

import android.util.Log;

import com.shipdream.lib.android.mvp.view.BaseTestCase;
import com.shipdream.lib.android.mvp.view.LifeCycle;
import com.shipdream.lib.android.mvp.view.nav.MvcTestActivityNavigation;
import com.shipdream.lib.android.mvp.view.test.R;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class TestInjectionAndLifeCycle extends BaseTestCase<InjectionTestActivity> {

    @Override
    protected void waitTest() throws InterruptedException {
        waitTest(1000);
    }

    public TestInjectionAndLifeCycle() {
        super(InjectionTestActivity.class);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        waitTest();
    }

    @Test
    public void testShouldRetainInjectionsOfFragmentAAfterNavigatedToFragmentB() throws Throwable {
        //=============================> At A
        lifeCycleValidatorA.expect(LifeCycle.onCreateNull,
            LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
            LifeCycle.onViewReadyNewInstance, LifeCycle.onViewReadyFirstTime);
        onView(withId(R.id.textA)).check(matches(withText("Added by FragmentA")));
        onView(withId(R.id.textB)).check(matches(withText("Added by FragmentA")));
        onView(withId(R.id.textC)).check(matches(withText("")));

        navigationManager.navigate(this).to(MvcTestActivityNavigation.Loc.B);
        waitTest();
        //=============================> At B
        //onDestroyView is always called when a fragment is pushed to back stack
        //BUT onDestroy of previous Fragment(FragmentA) is not called when it's pushed to back stack
        lifeCycleValidatorA.expect(LifeCycle.onPushingToBackStack, LifeCycle.onDestroyView);
        lifeCycleValidatorB.expect(LifeCycle.onCreateNull,
            LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
            LifeCycle.onViewReadyNewInstance, LifeCycle.onViewReadyFirstTime);
        onView(withId(R.id.textA)).check(matches(withText("Added by FragmentA\n" +
            "Added by FragmentB")));
        onView(withId(R.id.textB)).check(matches(withText("Added by FragmentA\n" +
            "Added by FragmentB")));
        onView(withId(R.id.textC)).check(matches(withText("")));

        navigationManager.navigate(this).to(MvcTestActivityNavigation.Loc.C);
        waitTest();
        //=============================> At C
        lifeCycleValidatorB.expect(LifeCycle.onPushingToBackStack, LifeCycle.onDestroyView);
        lifeCycleValidatorC.expect(LifeCycle.onCreateNull, LifeCycle.onCreateViewNull,
            LifeCycle.onViewCreatedNull,
            LifeCycle.onViewReadyNewInstance, LifeCycle.onViewReadyFirstTime);
        onView(withId(R.id.textA)).check(matches(withText("")));
        onView(withId(R.id.textB)).check(matches(withText("Added by FragmentA\n" +
            "Added by FragmentB\n" +
            "Added by FragmentC")));
        onView(withId(R.id.textC)).check(matches(withText("Added by FragmentC")));

        navigationManager.navigate(this).back();
        waitTest(1000);
        //=============================> At B
        lifeCycleValidatorC.expect(LifeCycle.onDestroyView, LifeCycle.onDestroy);
        //View is newly created again
        //onPoppedOutToFront is called when the fragment pops out from back stack
        lifeCycleValidatorB.expect(
            LifeCycle.onCreateViewNull,
            LifeCycle.onViewCreatedNull,
            LifeCycle.onViewReadyPopOut,
            LifeCycle.onPoppedOutToFront);
        onView(withId(R.id.textA)).check(matches(withText("Added by FragmentA\n" +
            "Added by FragmentB\n" +
            "Added by FragmentB")));
        onView(withId(R.id.textB)).check(matches(withText("Added by FragmentA\n" +
            "Added by FragmentB\n" +
            "Added by FragmentC\n" +
            "Added by FragmentB")));
        onView(withId(R.id.textC)).check(matches(withText("")));

        navigationManager.navigate(this).back();
        waitTest(1000);
        //=============================> At A
        //onDestroy of previous Fragment(FragmentB) is not called until it's removed out from back stack
        lifeCycleValidatorB.expect(LifeCycle.onDestroyView, LifeCycle.onDestroy);
        //View is newly created again
        //onPoppedOutToFront is called when the fragment pops out from back stack
        lifeCycleValidatorA.expect(
            LifeCycle.onCreateViewNull,
            LifeCycle.onViewCreatedNull,
            LifeCycle.onViewReadyPopOut,
            LifeCycle.onPoppedOutToFront);
        onView(withId(R.id.textA)).check(matches(withText("Added by FragmentA\n" +
            "Added by FragmentB\n" +
            "Added by FragmentB\n" +
            "Added by FragmentA")));
        onView(withId(R.id.textB)).check(matches(withText("Added by FragmentA\n" +
            "Added by FragmentB\n" +
            "Added by FragmentC\n" +
            "Added by FragmentB\n" +
            "Added by FragmentA")));
        onView(withId(R.id.textC)).check(matches(withText("")));
    }

    @Test
    public void test_should_delay_call_on_view_ready_on_sub_fragments_after_dependencies_injected_when_restore_from_kill() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "testLifeCyclesWhenKeepActivities not tested as Don't Keep Activities setting is disabled");
            return;
        }

        //=============================> At A
        onView(withId(R.id.textA)).check(matches(withText("Added by FragmentA")));
        onView(withId(R.id.textB)).check(matches(withText("Added by FragmentA")));
        onView(withId(R.id.textC)).check(matches(withText("")));

        navigationManager.navigate(this).to(MvcTestActivityNavigation.Loc.B);
        waitTest();
        //=============================> At B
        onView(withId(R.id.textA)).check(matches(withText("Added by FragmentA\n" +
            "Added by FragmentB")));
        onView(withId(R.id.textB)).check(matches(withText("Added by FragmentA\n" +
            "Added by FragmentB")));
        onView(withId(R.id.textC)).check(matches(withText("")));

        pressHome();
        waitTest(1200);

        bringBack();
        waitTest(1200);
        onView(withId(R.id.textA)).check(matches(withText("Added by FragmentA\n" +
            "Added by FragmentB\nAdded by FragmentB")));
        onView(withId(R.id.textB)).check(matches(withText("Added by FragmentA\n" +
            "Added by FragmentB\nAdded by FragmentB")));
        onView(withId(R.id.textC)).check(matches(withText("")));
    }

    @Test
    public void should_not_set_first_time_flag_for_creating_view_reason_on_back_nav_without_kill() throws Throwable {
        if (isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "should_not_set_first_time_flag_for_creating_view_reason_on_back_nav not tested as Don't Keep Activities setting is enabled");
            return;
        }

        waitTest();
        lifeCycleValidatorA.expect(LifeCycle.onCreateNull,
            LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
            LifeCycle.onViewReadyNewInstance, LifeCycle.onViewReadyFirstTime);

        navigationManager.navigate(this).to(MvcTestActivityNavigation.Loc.B);
        waitTest();

        lifeCycleValidatorA.expect(LifeCycle.onPushingToBackStack, LifeCycle.onDestroyView);

        lifeCycleValidatorB.expect(LifeCycle.onCreateNull,
            LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
            LifeCycle.onViewReadyNewInstance, LifeCycle.onViewReadyFirstTime);

        navigationManager.navigate(this).back();
        waitTest();

        lifeCycleValidatorB.expect(LifeCycle.onDestroyView, LifeCycle.onDestroy);

        lifeCycleValidatorA.expect(LifeCycle.onCreateViewNull,
            LifeCycle.onViewCreatedNull,
            LifeCycle.onViewReadyPopOut,
            LifeCycle.onPoppedOutToFront);
    }

    @Test
    public void should_not_set_first_time_flag_for_creating_view_reason_on_back_nav_with_kill() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "should_not_set_first_time_flag_for_creating_view_reason_on_back_nav not tested as Don't Keep Activities setting is disabled");
            return;
        }

        waitTest();
        lifeCycleValidatorA.expect(LifeCycle.onCreateNull,
            LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
            LifeCycle.onViewReadyNewInstance, LifeCycle.onViewReadyFirstTime);

        navigationManager.navigate(this).to(MvcTestActivityNavigation.Loc.B);
        waitTest();
        lifeCycleValidatorA.expect(LifeCycle.onPushingToBackStack,
            LifeCycle.onDestroyView);

        lifeCycleValidatorB.expect(LifeCycle.onCreateNull,
            LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
            LifeCycle.onViewReadyNewInstance, LifeCycle.onViewReadyFirstTime);

        pressHome();
        waitTest(1200);

        lifeCycleValidatorA.expect(LifeCycle.onDestroy);

        lifeCycleValidatorB.expect(
            LifeCycle.onDestroyView,
            LifeCycle.onDestroy);

        bringBack();
        waitTest(1200);

        lifeCycleValidatorB.expect(LifeCycle.onCreateNotNull,
            LifeCycle.onCreateViewNotNull,
            LifeCycle.onViewReadyNewInstance,
            LifeCycle.onViewCreatedNotNull,
            LifeCycle.onViewReadyRestore);

        //Fragment itself is recreated
        lifeCycleValidatorA.expect(LifeCycle.onCreateNotNull);

        navigationManager.navigate(this).back();
        waitTest();

        lifeCycleValidatorA.expect(
            LifeCycle.onCreateViewNotNull,
            LifeCycle.onViewCreatedNotNull,
            LifeCycle.onViewReadyNewInstance,
            LifeCycle.onViewReadyRestore,
            LifeCycle.onViewReadyPopOut,
            LifeCycle.onPoppedOutToFront);

        lifeCycleValidatorB.expect(LifeCycle.onDestroyView, LifeCycle.onDestroy);
    }

}
