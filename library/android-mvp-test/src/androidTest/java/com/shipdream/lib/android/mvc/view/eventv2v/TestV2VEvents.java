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

package com.shipdream.lib.android.mvc.view.eventv2v;

import com.shipdream.lib.android.mvc.view.BaseTestCase;
import com.shipdream.lib.android.mvp.view.eventv2v.EventBusV2VActivity;
import com.shipdream.lib.android.mvp.view.eventv2v.controller.V2VTestController;
import com.shipdream.lib.android.mvc.view.test.R;

import org.junit.Test;

import javax.inject.Inject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class TestV2VEvents extends BaseTestCase<EventBusV2VActivity> {
    @Inject
    private V2VTestController v2VTestController;

    public TestV2VEvents() {
        super(EventBusV2VActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        waitTest();
    }

    @Test
    public void test_should_be_able_to_send_and_receive_v2v_events_among_fragments_services_and_dialogFragments() throws Throwable {
        onView(withId(R.id.fragment_mvc_v2v_text)).check(matches(withText("Initial Text")));

        onView(withId(R.id.fragment_mvc_v2v_btnService)).perform(click());

        onView(withId(R.id.fragment_mvc_v2v_text)).check(matches(withText("Updated By Service")));

        onView(withId(R.id.fragment_mvc_v2v_btnDialog)).perform(click());

        onView(withId(R.id.fragment_mvc_v2v_dialog_text)).check(matches(withText("Initial Dialog Text")));

        v2VTestController.updateDialogButton(this, "Updated By Under Fragment via V2V event");

        onView(withId(R.id.fragment_mvc_v2v_dialog_text)).check(matches(withText("Updated By Under Fragment via V2V event")));

        onView(withId(R.id.fragment_mvc_v2v_dialog_button)).perform(click());

        onView(withId(R.id.fragment_mvc_v2v_text)).check(matches(withText("Dialog Closed")));
    }

}
