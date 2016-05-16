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

import com.shipdream.lib.android.mvp.BaseTestCase;
import com.shipdream.lib.android.mvp.view.test.R;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class TestInjectionAndLifeCycleForRootFragment extends BaseTestCase<InjectionTestActivityTestRootFragment> {

    @Override
    protected void waitTest() throws InterruptedException {
        waitTest(1000);
    }

    public TestInjectionAndLifeCycleForRootFragment() {
        super(InjectionTestActivityTestRootFragment.class);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        waitTest();
    }

    @Test
    public void test_should_delay_call_on_view_ready_on_root_fragment_after_dependencies_injected_when_restore_from_kill() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), "Not tested as Don't Keep Activities setting is disabled");
            return;
        }

        //=============================> At A
        onView(withId(R.id.textA)).check(matches(withText("Added by FragmentA")));
        onView(withId(R.id.textB)).check(matches(withText("Added by FragmentA")));
        onView(withId(R.id.textC)).check(matches(withText("")));

        pressHome();
        waitTest();

        bringBack();
        waitTest();
        onView(withId(R.id.textA)).check(matches(withText("Added by FragmentA\nOK\nAdded by FragmentA")));
        onView(withId(R.id.textB)).check(matches(withText("Added by FragmentA\nAdded by FragmentA")));
    }

}
