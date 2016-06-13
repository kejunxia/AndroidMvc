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

package com.shipdream.lib.android.mvp.view.nav;

import android.content.pm.ActivityInfo;

import com.google.gson.Gson;
import com.shipdream.lib.android.mvp.BaseTestCase;
import com.shipdream.lib.android.mvp.Forwarder;
import com.shipdream.lib.android.mvp.Mvp;
import com.shipdream.lib.android.mvp.NavigationManager;
import com.shipdream.lib.android.mvp.view.injection.presenter.PresenterA;
import com.shipdream.lib.android.mvp.view.injection.presenter.PresenterB;
import com.shipdream.lib.android.mvp.view.injection.presenter.PresenterC;
import com.shipdream.lib.android.mvp.view.injection.presenter.PresenterD;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestCaseNavigationBasic extends BaseTestCase <MvpTestActivityNavigation> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private AnotherPresenter anotherPresenter;

    private Comp comp;
    private DisposeCheckerA disposeCheckerAMock;
    private DisposeCheckerB disposeCheckerBMock;
    private DisposeCheckerC disposeCheckerCMock;
    private DisposeCheckerD disposeCheckerDMock;

    public TestCaseNavigationBasic() {
        super(MvpTestActivityNavigation.class);
    }

    @Override
    protected void waitTest() throws InterruptedException {
        waitTest(300);
    }

    public static class Comp {
        TestCaseNavigationBasic testCaseNavigation;

        @Singleton
        @Provides
        public DisposeCheckerA providesDisposeCheckerA() {
            return testCaseNavigation.disposeCheckerAMock;
        }

        @Singleton
        @Provides
        public DisposeCheckerB providesDisposeCheckerB() {
            return testCaseNavigation.disposeCheckerBMock;
        }

        @Singleton
        @Provides
        public DisposeCheckerC providesDisposeCheckerC() {
            return testCaseNavigation.disposeCheckerCMock;
        }

        @Singleton
        @Provides
        public DisposeCheckerD providesDisposeCheckerD() {
            return testCaseNavigation.disposeCheckerDMock;
        }
    }

    @Override
    protected void injectDependencies() throws ProvideException, ProviderConflictException {
        disposeCheckerAMock = mock(DisposeCheckerA.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                logger.debug("Dispose checker A");
                return null;
            }
        }).when(disposeCheckerAMock).onDisposed();
        disposeCheckerBMock = mock(DisposeCheckerB.class);
        disposeCheckerCMock = mock(DisposeCheckerC.class);
        disposeCheckerDMock = mock(DisposeCheckerD.class);
        comp = new Comp();
        comp.testCaseNavigation = this;
        Mvp.graph().getRootComponent().register(comp);
    }

    @Override
    protected void cleanDependencies() throws ProviderMissingException {
        super.cleanDependencies();
        Mvp.graph().getRootComponent().unregister(comp);
    }

    private NavigationManager.Model getNavManagerModel() throws PokeException {
        NavigationManager navigationManager = Mvp.graph().reference(NavigationManager.class, null);
        NavigationManager.Model model = navigationManager.getModel();
        Mvp.graph().dereference(navigationManager, NavigationManager.class, null);
        return model;
    }

    @Test
    public void testShouldReleaseInjectionsAfterFragmentsArePoppedOut() throws Throwable {
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        verify(disposeCheckerAMock, times(0)).onDisposed();
        verify(disposeCheckerBMock, times(0)).onDisposed();
        verify(disposeCheckerCMock, times(0)).onDisposed();
        verify(disposeCheckerDMock, times(0)).onDisposed();

        testNavigateToA();
        testNavigateToB();
        waitTest(1000);
        verify(disposeCheckerAMock, times(0)).onDisposed();
        verify(disposeCheckerBMock, times(0)).onDisposed();
        verify(disposeCheckerCMock, times(0)).onDisposed();
        verify(disposeCheckerDMock, times(0)).onDisposed();
        testNavigateToC();
        waitTest(1000);
        verify(disposeCheckerAMock, times(0)).onDisposed();
        verify(disposeCheckerBMock, times(0)).onDisposed();
        verify(disposeCheckerCMock, times(0)).onDisposed();
        verify(disposeCheckerDMock, times(0)).onDisposed();
        testNavigateToD();
        waitTest(1000);
        verify(disposeCheckerAMock, times(0)).onDisposed();
        verify(disposeCheckerBMock, times(0)).onDisposed();
        verify(disposeCheckerCMock, times(0)).onDisposed();
        verify(disposeCheckerDMock, times(0)).onDisposed();
        navigationManager.navigate(this).back();
        waitTest();
        waitTest(2000);
        verify(disposeCheckerAMock, times(0)).onDisposed();
        verify(disposeCheckerBMock, times(0)).onDisposed();
        verify(disposeCheckerCMock, times(0)).onDisposed();
        verify(disposeCheckerDMock, times(1)).onDisposed();

        //A->B->C
        reset(disposeCheckerAMock);
        reset(disposeCheckerBMock);
        reset(disposeCheckerCMock);
        reset(disposeCheckerDMock);
        testNavigateToD();
        waitTest(1000);
        verify(disposeCheckerAMock, times(0)).onDisposed();
        verify(disposeCheckerBMock, times(0)).onDisposed();
        verify(disposeCheckerCMock, times(0)).onDisposed();
        verify(disposeCheckerDMock, times(0)).onDisposed();

        //A->B->C->D
        reset(disposeCheckerAMock);
        reset(disposeCheckerBMock);
        reset(disposeCheckerCMock);
        reset(disposeCheckerDMock);
        navigationManager.navigate(this).back(null);
        waitTest(1000);
        verify(disposeCheckerAMock, times(0)).onDisposed();
        verify(disposeCheckerBMock, times(1)).onDisposed();
        verify(disposeCheckerCMock, times(1)).onDisposed();
        verify(disposeCheckerDMock, times(1)).onDisposed();

        //A
        reset(disposeCheckerAMock);
        reset(disposeCheckerBMock);
        reset(disposeCheckerCMock);
        reset(disposeCheckerDMock);
        navigationManager.navigate(this).back();
        waitTest(2000);
        verify(disposeCheckerAMock, times(1)).onDisposed();
        verify(disposeCheckerBMock, times(0)).onDisposed();
        verify(disposeCheckerCMock, times(0)).onDisposed();
        verify(disposeCheckerDMock, times(0)).onDisposed();
    }

    @Test
    public void testNavigateAmongFragments() throws Throwable {
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        testNavigateToB();

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        waitTest();

        testNavigateToC();

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        waitTest();

        testNavigateToD();

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        waitTest();

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        waitTest();

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        testNavigateToD();

        pressHome();
        waitTest();
        bringBack();
        waitTest();
        onView(withText(NavFragmentD.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        testNavigateToA();
        testNavigateToD();
        testNavigateToA();
        testNavigateToB();
        testNavigateToA();
        testNavigateToC();

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        waitTest();

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        pressHome();
        waitTest();
        bringBack();
        waitTest();

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentD.class.getSimpleName())).check(matches(isDisplayed()));

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        waitTest();

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigateAmongFragmentsClearingHistory() throws Throwable {
        testNavigateToA();
        testNavigateToD();
        testNavigateToC();
        testNavigateToB();

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        waitTest();

        testNavigateToD();
        testNavigateToA();

        navigationManager.navigate(this).to(PresenterC.class, new Forwarder().clearTo(PresenterB.class));
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        waitTest();

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        waitTest();

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentD.class.getSimpleName())).check(matches(isDisplayed()));

        pressHome();
        waitTest();
        bringBack();
        waitTest();

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        waitTest();

        testNavigateToC();
        testNavigateToB();
        testNavigateToD();
        testNavigateToA();
        navigationManager.navigate(this).to(PresenterC.class, new Forwarder().clearAll());
        waitTest();

        testNavigateToB();
        testNavigateToD();

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        waitTest();

        testNavigateToA();
        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentD.class.getSimpleName())).check(matches(isDisplayed()));

        pressHome();
        waitTest();
        bringBack();
        waitTest();

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        waitTest();

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigateBackFragmentsClearingHistory() throws Throwable {
        testNavigateToA();
        testNavigateToD();
        testNavigateToC();
        testNavigateToB();

        pressHome();
        waitTest();
        bringBack();
        waitTest();

        testNavigateToD();
        testNavigateToA();

        navigationManager.navigate(this).back(PresenterB.class);
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));

        pressHome();
        waitTest();
        bringBack();
        waitTest();

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentD.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        testNavigateToC();
        testNavigateToB();
        testNavigateToD();

        pressHome();
        waitTest();
        bringBack();
        waitTest();

        testNavigateToA();
        navigationManager.navigate(this).back(null);
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        testNavigateToB();
        testNavigateToD();
        testNavigateToA();
        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentD.class.getSimpleName())).check(matches(isDisplayed()));

        pressHome();
        waitTest();
        bringBack();
        waitTest();

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));
    }

    @Test
    public void testShouldRetainAndRestoreNavigateModelCorrectly() throws Throwable {
        testNavigateToA();
        testNavigateToB();
        testNavigateToC();

        NavigationManager.Model originalModel = getNavManagerModel();

        pressHome();
        waitTest();
        bringBack();
        waitTest();

        NavigationManager.Model currentModel = getNavManagerModel();
        Gson gson = new Gson();
        Assert.assertEquals(gson.toJson(originalModel), gson.toJson(currentModel));

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        waitTest();
        currentModel = getNavManagerModel();
        Assert.assertEquals(gson.toJson(originalModel), gson.toJson(currentModel));

        pressHome();
        waitTest();
        bringBack();
        waitTest();
        pressHome();
        waitTest();
        bringBack();
        waitTest();

        currentModel = getNavManagerModel();
        Assert.assertEquals(gson.toJson(originalModel), gson.toJson(currentModel));

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        waitTest();

        currentModel = getNavManagerModel();
        Assert.assertEquals(gson.toJson(originalModel), gson.toJson(currentModel));

        pressHome();
        waitTest();
        bringBack();
        waitTest();
        currentModel = getNavManagerModel();
        Assert.assertEquals(gson.toJson(originalModel), gson.toJson(currentModel));

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        pressHome();
        waitTest();
        bringBack();
        waitTest();

        currentModel = getNavManagerModel();
        Assert.assertEquals(gson.toJson(originalModel), gson.toJson(currentModel));
    }

    @Test
    public void test_should_not_push_fragments_to_back_stack_with_interim_nav_location() throws InterruptedException {
        navigationManager.navigate(this).to(PresenterA.class);
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).to(PresenterB.class, new Forwarder().setInterim(true));
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).to(PresenterC.class);
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));
    }

    @Test
    public void test_should_be_able_to_skip_interim_item_with_clear_history_nav_location() throws InterruptedException {
        navigationManager.navigate(this).to(PresenterA.class);
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).to(PresenterB.class);
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).to(PresenterC.class, new Forwarder().setInterim(true));
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).to(PresenterD.class,
                new Forwarder().clearTo(PresenterB.class));
        waitTest();
        onView(withText(NavFragmentD.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));
    }

    @Test
    public void test_should_pass_nav_location_when_clear_history_land_on_interim_location() throws InterruptedException {
        navigationManager.navigate(this).to(PresenterA.class);
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).to(PresenterB.class);
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).to(PresenterC.class, new Forwarder().setInterim(true));
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).to(PresenterD.class,
                new Forwarder().clearTo(PresenterC.class));
        waitTest();
        onView(withText(NavFragmentD.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));
    }

    @Test
    public void test_nav_back_from_interim_location_should_be_same_as_from_non_interim_locaiton() throws InterruptedException {
        navigationManager.navigate(this).to(PresenterA.class);
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).to(PresenterB.class);
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).to(PresenterC.class, new Forwarder().setInterim(true));
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).to(PresenterC.class);
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));
    }

    private void testNavigateToA() throws InterruptedException {
        navigationManager.navigate(this).to(PresenterA.class);
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));
    }

    private void testNavigateToB() throws InterruptedException {
        navigationManager.navigate(this).to(PresenterB.class);
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));
    }

    private void testNavigateToC() throws InterruptedException {
        navigationManager.navigate(this).to(PresenterC.class);
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));
    }

    private void testNavigateToD() throws InterruptedException {
        navigationManager.navigate(this).to(PresenterD.class);
        waitTest();
        onView(withText(NavFragmentD.class.getSimpleName())).check(matches(isDisplayed()));
    }
}
