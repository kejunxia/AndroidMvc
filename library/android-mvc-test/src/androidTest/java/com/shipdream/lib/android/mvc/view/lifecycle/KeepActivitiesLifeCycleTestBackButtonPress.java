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

package com.shipdream.lib.android.mvc.view.lifecycle;

import com.shipdream.lib.android.mvc.Mvc;
import com.shipdream.lib.android.mvc.MvcComponent;
import com.shipdream.lib.poke.Provides;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class KeepActivitiesLifeCycleTestBackButtonPress extends BaseTestCaseLifeCycle {
    private MvcTestFragment.Controller controller;
    private MvcComponent component;

    @Override
    protected void prepareDependencies(MvcComponent testComponent) throws Exception {
        super.prepareDependencies(testComponent);
        component = new MvcComponent("BackButtonPressTestComponent");

        component.register(new Object(){
            @Provides
            public MvcTestFragment.Controller homeController() {
                controller = mock(MvcTestFragment.Controller.class);
                return controller;
            }
        });

        Mvc.graph().getRootComponent().attach(component, true);
    }

    @Override
    public void tearDown() throws Exception {
        Mvc.graph().getRootComponent().detach(component);
        super.tearDown();
    }

    @Test
    public void test_back_button_pressed_should_be_passed_down_to_controller() throws Exception {
        verify(controller, times(0)).onBackButtonPressed();

        activity.onBackPressed();

        verify(controller, times(1)).onBackButtonPressed();
    }
}
