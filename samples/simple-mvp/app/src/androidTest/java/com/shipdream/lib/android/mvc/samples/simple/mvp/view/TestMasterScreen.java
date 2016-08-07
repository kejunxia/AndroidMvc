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

package com.shipdream.lib.android.mvc.samples.simple.mvp.view;

import android.content.Intent;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.shipdream.lib.android.mvc.Mvc;
import com.shipdream.lib.android.mvc.MvcComponent;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.samples.simple.mvp.MainActivity;
import com.shipdream.lib.android.mvc.samples.simple.mvp.R;
import com.shipdream.lib.android.mvc.samples.simple.mvp.dto.IpPayload;
import com.shipdream.lib.android.mvc.samples.simple.mvp.factory.ServiceFactory;
import com.shipdream.lib.android.mvc.samples.simple.mvp.http.IpService;
import com.shipdream.lib.poke.Provides;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Response;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestMasterScreen {
    private MvcComponent testComponent;
    private Call<IpPayload> ipServiceCallMock;
    @Inject
    private NavigationManager navigationManager;

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(
            MainActivity.class,
            false,    // initialTouchMode
            false);  // launchActivity. False to set intent per test);

    @Before
    public void prepareInjection() throws Exception {
        testComponent = new MvcComponent("TestComponent");
        testComponent.register(new Object(){
            /**
             * Prepare objects to mock http calls
             * @return
             * @throws IOException
             */
            @Provides
            public ServiceFactory serviceFactory() throws IOException {
                ipServiceCallMock = mock(Call.class);

                IpService ipServiceMock = mock(IpService.class);
                when(ipServiceMock.getIp(anyString())).thenReturn(ipServiceCallMock);

                ServiceFactory serviceFactoryMock = mock(ServiceFactory.class);
                when(serviceFactoryMock.createService(IpService.class)).thenReturn(ipServiceMock);
                return serviceFactoryMock;
            }
        });

        boolean overriding = true;
        Mvc.graph().getRootComponent().attach(testComponent, overriding);
        Mvc.graph().inject(this);

        rule.launchActivity(new Intent());
    }

    @After
    public void tearDown() throws Exception {
        //Important!!!
        //To clear fragment manager's back stack
        navigationManager.navigate(this).back(null);
        navigationManager.navigate(this).back();

        //Remove overriding component
        Mvc.graph().getRootComponent().detach(testComponent);
    }

    @Test
    public void should_be_able_to_increment_and_decrement_count() {
        //Initial value is 0
        onView(withId(R.id.screen_master_counterDisplay)).check(matches(withText("0")));

        //Click increment and check text view value
        onView(withId(R.id.screen_master_buttonIncrement)).perform(click());
        onView(withId(R.id.screen_master_counterDisplay)).check(matches(withText("1")));

        //Click decrement and check text view value
        onView(withId(R.id.screen_master_buttonDecrement)).perform(click());
        onView(withId(R.id.screen_master_counterDisplay)).check(matches(withText("0")));
    }

    @Test
    public void should_carry_count_to_detail_screen() {
        //Initial value is 0
        onView(withId(R.id.screen_master_counterDisplay)).check(matches(withText("0")));

        //Click increment and check text view value
        onView(withId(R.id.screen_master_buttonIncrement)).perform(click());
        onView(withId(R.id.screen_master_counterDisplay)).check(matches(withText("1")));

        //Navigate to detail screen
        onView(withId(R.id.fragment_master_buttonShowDetailScreen)).perform(click());

        //Check count on detail screen
        onView(withId(R.id.screen_detail_counterDisplay)).check(matches(withText("1")));

        //Click decrement and check text view value
        onView(withId(R.id.screen_detail_buttonIncrement)).perform(click());
        onView(withId(R.id.screen_detail_counterDisplay)).check(matches(withText("2")));

        navigationManager.navigate(this).back();
        onView(withId(R.id.screen_master_counterDisplay)).check(matches(withText("2")));
    }

    @Test
    public void should_be_able_to_get_ip() throws Exception{
        //Pre check
        onView(withId(R.id.fragment_master_ipValue)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));

        final String fakeIpResult = "abc.123.456.xyz";
        IpPayload payload = mock(IpPayload.class);
        when(payload.getIp()).thenReturn(fakeIpResult);
        when(ipServiceCallMock.execute()).thenReturn(Response.success(payload));

        //Action
        onView(withId(R.id.fragment_master_ipRefresh)).perform(click());

        //Check value
        onView(withId(R.id.fragment_master_ipValue)).check(matches(withText(fakeIpResult)));
    }

    @Test
    public void should_be_able_to_show_network_error_when_getting_ip_failed() throws Exception{
        //Pre check
        onView(withId(R.id.fragment_master_ipValue)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));

        //Prepare
        //Throw an IOException to simulate an network error
        IOException ioExceptionMock = mock(IOException.class);
        when(ipServiceCallMock.execute()).thenThrow(ioExceptionMock);

        //Action
        onView(withId(R.id.fragment_master_ipRefresh)).perform(click());

        //Check value
        onView(withText(R.string.network_error_to_get_ip)).inRoot(
                withDecorView(not(is(rule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
    }
}
