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

package com.shipdream.lib.android.mvc.view.injection;

import android.util.Log;

import com.shipdream.lib.android.mvc.view.BaseTestCase;
import com.shipdream.lib.android.mvc.view.nav.MvcTestActivityNavigation;
import com.shipdream.lib.android.mvc.view.test.R;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class TestInjectedStateManagedObjects extends BaseTestCase<InjectionTestActivity> {

    public TestInjectedStateManagedObjects() {
        super(InjectionTestActivity.class);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        waitTest();
    }

    @Test
    public void should_manage_state_of_nested_stateManagedObjects() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "testLifeCyclesWhenKeepActivities not tested as Don't Keep Activities setting is disabled");
            return;
        }

        navigationController.navigate(this).to(MvcTestActivityNavigation.Loc.D);
        waitTest();

        onView(withId(R.id.textA)).check(matches(withText("")));

        onView(withId(R.id.fragment_injection_root)).perform(click());

        onView(withId(R.id.textA)).check(matches(withText("1:A")));

        onView(withId(R.id.fragment_injection_root)).perform(click());

        onView(withId(R.id.textA)).check(matches(withText("2:B")));

        pressHome();
        waitTest(2000);

        bringBack();
        waitTest(2000);

        onView(withId(R.id.fragment_injection_root)).perform(click());
        onView(withId(R.id.textA)).check(matches(withText("3:C")));
    }

}
