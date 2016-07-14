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

package com.shipdream.lib.android.mvc.samples.simple.controller.internal;

import com.shipdream.lib.android.mvc.Mvc;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.TestUtil;
import com.shipdream.lib.android.mvc.samples.simple.controller.CounterMasterController;
import com.shipdream.lib.android.mvc.samples.simple.controller.CounterDetailController;
import com.shipdream.lib.android.mvc.samples.simple.manager.CounterManager;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestCounterBasicController extends BaseTest {
    @Inject
    private CounterManager counterManager;

    @Inject
    private NavigationManager navigationManager;

    private CounterMasterController controller;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        controller = new CounterMasterController();
        Mvc.graph().inject(controller);
        controller.onCreated();
    }

    @Test
    public void increment_should_post_counter_update_event_with_incremented_value() {
        //1. Prepare
        //prepare event monitor
        CounterMasterController.View view = mock(CounterMasterController.View.class);
        TestUtil.assignControllerView(controller, view);

        //mock controller model for count value
        int value = new Random().nextInt();
        CounterManager.Model counterModel = new CounterManager.Model();
        counterModel.setCount(value);
        //Mock the model of manager
        counterManager.bindModel(this, counterModel);

        //2. Act
        controller.increment(this);

        //3. Verify
        verify(view, times(1)).update();
        Assert.assertEquals(String.valueOf(value + 1), controller.getModel().getCount());

    }

    @Test
    public void should_navigate_to_locationB_when_go_to_advance_view_and_back_to_locationA_after_go_to_basic_view() {
        //Prepare
        //Simulate navigating to location
        navigationManager.navigate(this).to(CounterMasterController.class);
        //Verify: location should be changed to LocationA
        Assert.assertEquals(CounterMasterController.class.getName(),
                navigationManager.getModel().getCurrentLocation().getLocationId());

        //Act: CounterController now goes to advanced view underlining logic is navigating to locationB
        controller.goToDetailView(this);

        //Verify: Current location should be LocationB
        Assert.assertEquals(CounterDetailController.class.getName(),
                navigationManager.getModel().getCurrentLocation().getLocationId());
    }

}
