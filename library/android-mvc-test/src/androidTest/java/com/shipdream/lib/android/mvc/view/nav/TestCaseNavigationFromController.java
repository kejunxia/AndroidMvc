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

import com.shipdream.lib.android.mvc.BaseTestCase;
import com.shipdream.lib.android.mvc.Mvc;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.Preparer;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

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

public class TestCaseNavigationFromController extends BaseTestCase <MvcTestActivityNavigation> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private NavigationManager navigationManager;

    private Comp comp;
    private DisposeCheckerE disposeCheckerEMock;
    private DisposeCheckerF disposeCheckerFMock;
    private DisposeCheckerG disposeCheckerGMock;

    public TestCaseNavigationFromController() {
        super(MvcTestActivityNavigation.class);
    }

    @Override
    protected void waitTest() throws InterruptedException {
        waitTest(300);
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
    protected void injectDependencies() throws ProvideException, ProviderConflictException {

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
        comp = new Comp();
        comp.testCaseNavigation = this;
        Mvc.graph().getRootComponent().register(comp);
    }

    @Override
    protected void cleanDependencies() throws ProviderMissingException {
        super.cleanDependencies();
        Mvc.graph().getRootComponent().unregister(comp);
    }

    @Test
    public void test_should_release_injected_object_by_pure_navigation_controller_navigation() throws Throwable {
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        final String val = "Value = " + new Random().nextInt();

        navigationManager.navigate(this).with(ControllerE.class, new Preparer<ControllerE>() {
            @Override
            public void prepare(ControllerE instance) {
                instance.setValue(val);
            }
        }).to(ControllerE.class);

        //The value set to controller e in Injector.graph().use should be retained during the
        //navigation
        onView(withText(val)).check(matches(isDisplayed()));

        //The controller should not be disposed yet
        verify(disposeCheckerEMock, times(0)).onDestroy();

        navigationManager.navigate(this).back();

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
        navigationManager.navigate(this).with(ControllerE.class, new Preparer<ControllerE>() {
            @Override
            public void prepare(ControllerE instance) {
                instance.setValue(valE);
            }
        }).to(ControllerE.class);
        waitTest();
        onView(withText(valE)).check(matches(isDisplayed()));
        verify(disposeCheckerEMock, times(0)).onDestroy();
        verify(disposeCheckerFMock, times(0)).onDestroy();
        verify(disposeCheckerGMock, times(0)).onDestroy();

        resetDisposeCheckers();
        navigationManager.navigate(this).with(ControllerF.class, new Preparer<ControllerF>() {
            @Override
            public void prepare(ControllerF instance) {
                instance.setValue(valF);
            }
        }).to(ControllerF.class);
        waitTest();
        onView(withText(valF)).check(matches(isDisplayed()));
        verify(disposeCheckerEMock, times(0)).onDestroy();
        verify(disposeCheckerFMock, times(0)).onDestroy();
        verify(disposeCheckerGMock, times(0)).onDestroy();

        resetDisposeCheckers();
        navigationManager.navigate(this).with(ControllerG.class, new Preparer<ControllerG>() {
            @Override
            public void prepare(ControllerG instance) {
                instance.setValue(valG);
            }
        }).to(ControllerG.class);
        waitTest();
        onView(withText(valG)).check(matches(isDisplayed()));
        verify(disposeCheckerEMock, times(0)).onDestroy();
        verify(disposeCheckerFMock, times(0)).onDestroy();
        verify(disposeCheckerGMock, times(0)).onDestroy();

        resetDisposeCheckers();
        //The value set to controller e in Injector.graph().use should be retained during the
        //navigation
        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(valF)).check(matches(isDisplayed()));
        verify(disposeCheckerEMock, times(0)).onDestroy();
        verify(disposeCheckerFMock, times(0)).onDestroy();
        verify(disposeCheckerGMock, times(0)).onDestroy();

        resetDisposeCheckers();
        navigationManager.navigate(this).back();
        waitTest();
        onView(withText(valE)).check(matches(isDisplayed()));
        verify(disposeCheckerEMock, times(0)).onDestroy();
        verify(disposeCheckerFMock, times(1)).onDestroy();
        //__MvcGraphHelper retaining all instances is dangerous. Try to only retain relevant injected instances.
        verify(disposeCheckerGMock, times(0)).onDestroy();

        resetDisposeCheckers();
        navigationManager.navigate(this).back();
        waitTest();
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
