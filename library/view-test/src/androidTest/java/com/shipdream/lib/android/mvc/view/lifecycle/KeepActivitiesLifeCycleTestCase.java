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

public class KeepActivitiesLifeCycleTestCase extends BaseTestCaseLifeCycle {

    @Test
    public void testGoBackgroundAndBroughtBack() throws Throwable {
        if (isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), getClass().getSimpleName() + " not tested as Don't Keep Activities setting is enabled");
            return;
        }

        //Activity/Fragment launched.
        onView(withText("MvcTest")).check(matches(isDisplayed()));

        lifeCycleValidator.expect(LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull, LifeCycle.onViewReadyFirstTime);

        pressHome();
        waitTest();
        lifeCycleValidator.expect();

        bringBack();
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onReturnForeground);

        pressHome();
        waitTest();
        lifeCycleValidator.expect();

        bringBack();
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onReturnForeground);
    }

    @Test
    public void testRotations() throws Throwable {
        if (isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "testLifeCyclesWhenKeepActivities not tested as Don't Keep Activities setting is enabled");
            return;
        }

        //Activity/Fragment launched.
        onView(withText("MvcTest")).check(matches(isDisplayed()));

        lifeCycleValidator.expect(LifeCycle.onCreateNull,
                LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull, LifeCycle.onViewReadyFirstTime);

        //If not on portrait mode rotate it to portrait
        int currentOrientation = activity.getResources().getConfiguration().orientation;
        if(currentOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            waitTest();

            lifeCycleValidator.expect(LifeCycle.onCreateViewNull, LifeCycle.onViewCreatedNull,
                    LifeCycle.onViewReadyRotate, LifeCycle.onOrientationChanged, LifeCycle.onDestroyView);
        }

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged,
                LifeCycle.onDestroyView);

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged,
                LifeCycle.onDestroyView);

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged,
                LifeCycle.onDestroyView);

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged,
                LifeCycle.onDestroyView);

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged,
                LifeCycle.onDestroyView);

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged,
                LifeCycle.onDestroyView);

        pressHome();
        waitTest();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        bringBack();
        waitTest();
        lifeCycleValidator.expect(
                LifeCycle.onDestroyView,
                LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged);

        pressHome();
        waitTest();

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        bringBack();
        waitTest();
        lifeCycleValidator.expect(LifeCycle.onCreateViewNull,
                LifeCycle.onViewCreatedNull,
                LifeCycle.onViewReadyRotate,
                LifeCycle.onOrientationChanged,
                LifeCycle.onDestroyView);

        onView(withText(R.string.mvc_fragment_text)).check(matches(isDisplayed()));

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        waitTest();

        onView(withText(R.string.mvc_fragment_text)).check(matches(isDisplayed()));

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
