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

import com.shipdream.lib.android.mvc.Injector;
import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.android.mvc.view.AndroidMvc;
import com.shipdream.lib.android.mvc.view.BaseTestCase;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Consumer;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.ScopeCache;

import org.junit.Test;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestCaseNavigationFromController extends BaseTestCase <MvcTestActivityNavigation> {
    @Inject
    private NavigationController navigationController;

    private Comp comp;
    private DisposeCheckerE disposeCheckerEMock;

    public TestCaseNavigationFromController() {
        super(MvcTestActivityNavigation.class);
    }

    @Override
    protected void waitTest() throws InterruptedException {
        waitTest(200);
    }

    public static class Comp extends Component{
        TestCaseNavigationFromController testCaseNavigation;

        Comp(ScopeCache scopeCache) {
            super(scopeCache);
        }

        @Singleton
        @Provides
        public DisposeCheckerE providesDisposeCheckerE() {
            return testCaseNavigation.disposeCheckerEMock;
        }
    }

    @Override
    protected void injectDependencies(ScopeCache mvcSingletonCache) {
        super.injectDependencies(mvcSingletonCache);

        disposeCheckerEMock = mock(DisposeCheckerE.class);
        comp = new Comp(mvcSingletonCache);
        comp.testCaseNavigation = this;
        AndroidMvc.graph().register(comp);
    }

    @Override
    protected void cleanDependencies() {
        super.cleanDependencies();
        AndroidMvc.graph().unregister(comp);
    }

    @Test
    public void testShouldReleaseInjectionsAfterFragmentsArePoppedOut() throws Throwable {
        onView(withText(NavFragmentA.class.getSimpleName())).check(matches(isDisplayed()));

        final String val = "Value = " + new Random().nextInt();

        Injector.getGraph().use(ControllerE.class, new Consumer<ControllerE>() {
            @Override
            public void consume(ControllerE instance) {
                instance.setValue(val);
                navigationController.navigateTo(this, MvcTestActivityNavigation.Loc.E);
            }
        });

        //The value set to controller e in Injector.getGraph().use should be retained during the
        //navigation
        onView(withText(val)).check(matches(isDisplayed()));

        //The controller should not be disposed yet
        verify(disposeCheckerEMock, times(0)).onDisposed();

        navigationController.navigateBack(this);

        //Controller should be disposed after navigated away from fragment E
        waitTest();
        verify(disposeCheckerEMock, times(1)).onDisposed();
    }

}
