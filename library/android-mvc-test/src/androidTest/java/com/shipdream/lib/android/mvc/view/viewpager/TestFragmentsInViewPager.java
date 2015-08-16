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

import com.shipdream.lib.android.mvc.view.BaseTestCase;
import com.shipdream.lib.android.mvc.view.test.R;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
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


}
