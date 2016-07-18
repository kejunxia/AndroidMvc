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

package com.shipdream.lib.android.mvc.view.nav;

import android.support.v4.app.FragmentManager;

import com.shipdream.lib.android.mvc.BaseTestCase;
import com.shipdream.lib.android.mvc.Forwarder;
import com.shipdream.lib.android.mvc.MvcComponent;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.Preparer;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerA;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerB;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerC;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerD;
import com.shipdream.lib.poke.Provides;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestCaseNavigationFromController extends BaseTestCase<MvcTestActivityNavigation> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private NavigationManager navigationManager;

    private Comp modudle;
    private DisposeCheckerE disposeCheckerEMock;
    private DisposeCheckerF disposeCheckerFMock;
    private DisposeCheckerG disposeCheckerGMock;

    public TestCaseNavigationFromController() {
        super(MvcTestActivityNavigation.class);
    }

    public static class Comp {
        TestCaseNavigationFromController testCaseNavigation;

        @Singleton
        @Provides
        public DisposeCheckerE providesDisposeCheckerE() {
            return testCaseNavigation.disposeCheckerEMock;
        }

        @Singleton
        @Provides
        public DisposeCheckerF providesDisposeCheckerF() {
            return testCaseNavigation.disposeCheckerFMock;
        }

        @Singleton
        @Provides
        public DisposeCheckerG providesDisposeCheckerG() {
            return testCaseNavigation.disposeCheckerGMock;
        }
    }

    @Override
    protected void prepareDependencies(MvcComponent testComponent) throws Exception {
        disposeCheckerEMock = mock(DisposeCheckerE.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                logger.debug("Dispose checker E");
                return null;
            }
        }).when(disposeCheckerEMock).onDestroy();
        disposeCheckerFMock = mock(DisposeCheckerF.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                logger.debug("Dispose checker F");
                return null;
            }
        }).when(disposeCheckerFMock).onDestroy();
        disposeCheckerGMock = mock(DisposeCheckerG.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                logger.debug("Dispose checker G");
                return null;
            }
        }).when(disposeCheckerGMock).onDestroy();
        modudle = new Comp();
        modudle.testCaseNavigation = this;

        testComponent.register(modudle);
    }

    @Override
    protected Class<MvcTestActivityNavigation> getActivityClass() {
        return MvcTestActivityNavigation.class;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        navTo(ControllerA.class);
    }

    @Test
    public void test_back_navigation_should_skip_interim_location() throws Throwable {
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));
        assertFragmentsCount(1);

        navigationManager.navigate(this).to(ControllerB.class, new Forwarder().setInterim(true));
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));
        assertFragmentsCount(2);

        navigationManager.navigate(this).to(ControllerC.class, new Forwarder().setInterim(true));
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));
        assertFragmentsCount(3);

        navigationManager.navigate(this).to(ControllerD.class);
        waitTest();
        onView(withText(NavFragmentD.class.getSimpleName())).check(matches(isDisplayed()));
        assertFragmentsCount(4);

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));
        onView(withText(NavFragmentB.class.getSimpleName())).check(doesNotExist());
        onView(withText(NavFragmentC.class.getSimpleName())).check(doesNotExist());
        onView(withText(NavFragmentD.class.getSimpleName())).check(doesNotExist());
        assertFragmentsCount(1);

        navigationManager.navigate(this).to(ControllerB.class, new Forwarder().setInterim(true));
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));
        assertFragmentsCount(2);

        navigationManager.navigate(this).to(ControllerC.class, new Forwarder().setInterim(true));
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));
        assertFragmentsCount(3);

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));
        onView(withText(NavFragmentB.class.getSimpleName())).check(doesNotExist());
        onView(withText(NavFragmentC.class.getSimpleName())).check(doesNotExist());
        onView(withText(NavFragmentD.class.getSimpleName())).check(doesNotExist());
        assertFragmentsCount(1);

        navigationManager.navigate(this).to(ControllerB.class);
        waitTest();
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));
        assertFragmentsCount(2);

        navigationManager.navigate(this).to(ControllerC.class, new Forwarder().setInterim(true));
        waitTest();
        onView(withText(NavFragmentC.class.getSimpleName())).check(matches(isDisplayed()));
        assertFragmentsCount(3);

        navigationManager.navigate(this).to(ControllerD.class);
        waitTest();
        onView(withText(NavFragmentD.class.getSimpleName())).check(matches(isDisplayed()));
        assertFragmentsCount(4);

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(doesNotExist());
        onView(withText(NavFragmentB.class.getSimpleName())).check(matches(isDisplayed()));
        onView(withText(NavFragmentC.class.getSimpleName())).check(doesNotExist());
        onView(withText(NavFragmentD.class.getSimpleName())).check(doesNotExist());
        assertFragmentsCount(2);

        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));
        onView(withText(NavFragmentB.class.getSimpleName())).check(doesNotExist());
        onView(withText(NavFragmentC.class.getSimpleName())).check(doesNotExist());
        onView(withText(NavFragmentD.class.getSimpleName())).check(doesNotExist());
        assertFragmentsCount(1);
    }

    private void assertFragmentsCount(int count) {
        int actualFrags = 0;
        int actualStackCount = 0;
        FragmentManager fm = activity.getDelegateFragment().getChildFragmentManager();
        if (fm.getFragments() != null) {
            for (int i = 0; i < fm.getFragments().size(); ++i) {
                if (fm.getFragments().get(i) != null) {
                    ++actualFrags;
                }
            }

            actualStackCount = fm.getBackStackEntryCount();
        }
        Assert.assertEquals(count, actualFrags);
        Assert.assertEquals(count, actualStackCount);
    }

    @Test
    public void test_should_release_injected_object_by_pure_navigation_controller_navigation() throws Throwable {
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        final String val = "Value = " + new Random().nextInt();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                navigationManager.navigate(this).with(ControllerE.class, new Preparer<ControllerE>() {
                    @Override
                    public void prepare(ControllerE instance) {
                        instance.setValue(val);
                    }
                }).to(ControllerE.class);
            }
        });

        //The value set to controller e in Injector.graph().use should be retained during the
        //navigation
        onView(withText(val)).check(matches(isDisplayed()));

        //The controller should not be disposed yet
        verify(disposeCheckerEMock, times(0)).onDestroy();

        navigateBackByFragment();

        //Controller should be disposed after navigated away from fragment E
        waitTest();
        verify(disposeCheckerEMock, times(1)).onDestroy();
    }

    @Test
    public void test_should_release_injected_object_by_chained_navigation_controller_navigation() throws Throwable {
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        final String valE = "ValueE = " + new Random().nextInt();
        final String valF = "ValueF = " + new Random().nextInt();
        final String valG = "ValueG = " + new Random().nextInt();

        resetDisposeCheckers();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                navigationManager.navigate(this).with(ControllerE.class, new Preparer<ControllerE>() {
                    @Override
                    public void prepare(ControllerE instance) {
                        instance.setValue(valE);
                    }
                }).to(ControllerE.class);
            }
        });

        onView(withText(valE)).check(matches(isDisplayed()));
        verify(disposeCheckerEMock, times(0)).onDestroy();
        verify(disposeCheckerFMock, times(0)).onDestroy();
        verify(disposeCheckerGMock, times(0)).onDestroy();

        resetDisposeCheckers();
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                navigationManager.navigate(this).with(ControllerF.class, new Preparer<ControllerF>() {
                    @Override
                    public void prepare(ControllerF instance) {
                        instance.setValue(valF);
                    }
                }).to(ControllerF.class);
            }
        });
        waitTest();
        onView(withText(valF)).check(matches(isDisplayed()));
        verify(disposeCheckerEMock, times(0)).onDestroy();
        verify(disposeCheckerFMock, times(0)).onDestroy();
        verify(disposeCheckerGMock, times(0)).onDestroy();

        resetDisposeCheckers();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                navigationManager.navigate(this).with(ControllerG.class, new Preparer<ControllerG>() {
                    @Override
                    public void prepare(ControllerG instance) {
                        instance.setValue(valG);
                    }
                }).to(ControllerG.class);
            }
        });
        waitTest();
        onView(withText(valG)).check(matches(isDisplayed()));
        verify(disposeCheckerEMock, times(0)).onDestroy();
        verify(disposeCheckerFMock, times(0)).onDestroy();
        verify(disposeCheckerGMock, times(0)).onDestroy();

        resetDisposeCheckers();
        //The value set to controller e in Injector.graph().use should be retained during the
        //navigation
        navigateBackByFragment();
        onView(withText(valF)).check(matches(isDisplayed()));
        verify(disposeCheckerEMock, times(0)).onDestroy();
        verify(disposeCheckerFMock, times(0)).onDestroy();
        verify(disposeCheckerGMock, times(0)).onDestroy();

        resetDisposeCheckers();
        navigateBackByFragment();
        onView(withText(valE)).check(matches(isDisplayed()));
        verify(disposeCheckerEMock, times(0)).onDestroy();
        verify(disposeCheckerFMock, times(1)).onDestroy();
        //__MvcGraphHelper retaining all instances is dangerous. Try to only retain relevant injected instances.
        verify(disposeCheckerGMock, times(0)).onDestroy();

        resetDisposeCheckers();
        navigateBackByFragment();
        verify(disposeCheckerEMock, times(1)).onDestroy();
        verify(disposeCheckerFMock, times(0)).onDestroy();
        verify(disposeCheckerGMock, times(1)).onDestroy();
    }

    private void resetDisposeCheckers() {
        reset(disposeCheckerEMock);
        reset(disposeCheckerFMock);
        reset(disposeCheckerGMock);
    }

}
