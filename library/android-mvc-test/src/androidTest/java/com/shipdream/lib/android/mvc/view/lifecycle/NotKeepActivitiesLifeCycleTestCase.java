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

package com.shipdream.lib.android.mvc.view.lifecycle;

import android.content.pm.ActivityInfo;
import android.util.Log;

import com.shipdream.lib.android.mvc.view.LifeCycle;
import com.shipdream.lib.android.mvc.view.test.R;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class NotKeepActivitiesLifeCycleTestCase extends BaseTestCaseLifeCycle {

    @Test
    public void testGoBackgroundAndBroughtBack() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), getClass().getSimpleName() + " not tested as Don't Keep Activities setting is disabled");
            return;
        }

        //Activity/Fragment launched.
        onView(withText("MvcTest")).check(matches(isDisplayed()));

        lifeCycleValidator.expect(LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyNewInstance, LifeCycle.onViewReadyFirstTime);

        String ticket = pressHome();
        lifeCycleValidator.expect(LifeCycle.onDestroyView, LifeCycle.onDestroy);

        bringBack(ticket);
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onCreateNotNull,
                LifeCycle.onCreateViewNotNull, LifeCycle.onViewCreatedNotNull,
                LifeCycle.onViewReadyNewInstance, LifeCycle.onViewReadyRestore);

        ticket = pressHome();
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onDestroyView,
                LifeCycle.onDestroy);

        bringBack(ticket);
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onCreateNotNull,
                LifeCycle.onCreateViewNotNull, LifeCycle.onViewCreatedNotNull,
                LifeCycle.onViewReadyNewInstance, LifeCycle.onViewReadyRestore);

        ticket = pressHome();
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onDestroyView,
                LifeCycle.onDestroy);
    }

    @Test
    public void testRotations() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "testLifeCyclesWhenKeepActivities not tested as Don't Keep Activities setting is disabled");
            return;
        }

        //Activity/Fragment launched.
        onView(withText("MvcTest")).check(matches(isDisplayed()));

        lifeCycleValidator.expect(LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyNewInstance, LifeCycle.onViewReadyFirstTime);

        //If not on portrait mode rotate it to portrait
        int currentOrientation = activity.getResources().getConfiguration().orientation;
        if(currentOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            waitTest();

            lifeCycleValidator.expect(LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
                    LifeCycle.onViewReadyRotate,
                    LifeCycle.onOrientationChanged,
                    LifeCycle.onDestroyView);
        }

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged,
                LifeCycle.onDestroyView);

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        lifeCycleValidator.expect(LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged,
                LifeCycle.onDestroyView);

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged,
                LifeCycle.onDestroyView);

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged,
                LifeCycle.onDestroyView);

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged,
                LifeCycle.onDestroyView);

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged,
                LifeCycle.onDestroyView);

        String ticket = pressHome();
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onDestroyView, LifeCycle.onDestroy);

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        bringBack(ticket);
        lifeCycleValidator.expect(LifeCycle.onCreateNotNull, LifeCycle.onCreateViewNotNull,
                LifeCycle.onViewCreatedNotNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyRestore,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged);

        ticket = pressHome();
        lifeCycleValidator.expect(LifeCycle.onDestroyView, LifeCycle.onDestroy);

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        bringBack(ticket);
        lifeCycleValidator.expect(LifeCycle.onCreateNotNull, LifeCycle.onCreateViewNotNull,
                LifeCycle.onViewCreatedNotNull,
                LifeCycle.onViewReadyNewInstance,
                LifeCycle.onViewReadyRestore,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged);

        onView(withText(R.string.mvc_fragment_text)).check(matches(isDisplayed()));

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        waitTest();

        onView(withText(R.string.mvc_fragment_text)).check(matches(isDisplayed()));

        rotateMainActivity(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
