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

package com.shipdream.lib.android.mvc.view.nav;

import android.content.pm.ActivityInfo;

import com.google.gson.Gson;
import com.shipdream.lib.android.mvc.Injector;
import com.shipdream.lib.android.mvc.manager.NavigationManager;
import com.shipdream.lib.android.mvc.manager.internal.NavigationManagerImpl;
import com.shipdream.lib.android.mvc.manager.internal.Navigator;
import com.shipdream.lib.android.mvc.view.AndroidMvc;
import com.shipdream.lib.android.mvc.view.BaseTestCase;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.ScopeCache;
import com.shipdream.lib.poke.exception.PokeException;

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

public class TestCaseNavigationBasic extends BaseTestCase <MvcTestActivityNavigation> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private AnotherController anotherController;

    private Comp comp;
    private DisposeCheckerA disposeCheckerAMock;
    private DisposeCheckerB disposeCheckerBMock;
    private DisposeCheckerC disposeCheckerCMock;
    private DisposeCheckerD disposeCheckerDMock;

    public TestCaseNavigationBasic() {
        super(MvcTestActivityNavigation.class);
    }

    @Override
    protected void waitTest() throws InterruptedException {
        waitTest(300);
    }

    public static class Comp extends Component{
        TestCaseNavigationBasic testCaseNavigation;

        Comp(ScopeCache scopeCache) {
            super(scopeCache);
        }

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
    protected void injectDependencies(ScopeCache mvcSingletonCache) {
        super.injectDependencies(mvcSingletonCache);

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
        comp = new Comp(mvcSingletonCache);
        comp.testCaseNavigation = this;
        AndroidMvc.graph().register(comp);
    }

    @Override
    protected void cleanDependencies() {
        super.cleanDependencies();
        AndroidMvc.graph().unregister(comp);
    }

    private NavigationManager.Model getNavManagerModel() throws PokeException {
        NavigationManager navigationManager = Injector.getGraph().reference(NavigationManager.class, null);
        NavigationManager.Model model = ((NavigationManagerImpl) navigationManager).getModel();
        Injector.getGraph().dereference(navigationManager, NavigationManager.class, null);
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

        navigationManager.navigate(this).to(MvcTestActivityNavigation.Loc.C, MvcTestActivityNavigation.Loc.B);
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
        navigationManager.navigate(this).to(MvcTestActivityNavigation.Loc.C, new Navigator.Forwarder().clearAll());
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

        navigationManager.navigate(this).back(MvcTestActivityNavigation.Loc.B);
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

    private void testNavigateToA() throws InterruptedException {
        navigationManager.navigate(this).to(MvcTestActivityNavigation.Loc.A);
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));
    }

    private void testNavigateToB() throws InterruptedException {
        navigationManager.navigate(this).to(MvcTestActivityNavigation.Loc.B);
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));
    }

    private void testNavigateToC() throws InterruptedException {
        navigationManager.navigate(this).to(MvcTestActivityNavigation.Loc.C);
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));
    }

    private void testNavigateToD() throws InterruptedException {
        navigationManager.navigate(this).to(MvcTestActivityNavigation.Loc.D);
        waitTest();
        onView(withText(NavFragmentD.class.getSimpleName())).check(matches(isDisplayed()));
    }
}
