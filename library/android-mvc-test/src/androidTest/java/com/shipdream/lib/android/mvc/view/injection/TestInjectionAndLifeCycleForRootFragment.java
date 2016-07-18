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

package com.shipdream.lib.android.mvc.view.injection;

import android.util.Log;

import com.shipdream.lib.android.mvc.BaseTestCase;
import com.shipdream.lib.android.mvc.Forwarder;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerA;
import com.shipdream.lib.android.mvc.view.test.R;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class TestInjectionAndLifeCycleForRootFragment extends BaseTestCase<InjectionTestActivityTestRootFragment> {
    public TestInjectionAndLifeCycleForRootFragment() {
        super(InjectionTestActivityTestRootFragment.class);
    }

    @Override
    protected Class<InjectionTestActivityTestRootFragment> getActivityClass() {
        return InjectionTestActivityTestRootFragment.class;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        navTo(ControllerA.class, new Forwarder().clearAll());
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

        bringBack(pressHome());

        onView(withId(R.id.textA)).check(matches(withText("Added by FragmentA\nAdded by FragmentA\nOK")));
        onView(withId(R.id.textB)).check(matches(withText("Added by FragmentA\nAdded by FragmentA")));
    }

}
