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

import com.shipdream.lib.android.mvp.Injector;
import com.shipdream.lib.android.mvp.MvcGraph;
import com.shipdream.lib.android.mvp.manager.NavigationManager;
import com.shipdream.lib.android.mvp.event.bus.EventBus;
import com.shipdream.lib.android.mvp.event.bus.internal.EventBusImpl;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Random;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestCounterController {
    @BeforeClass
    public static void beforeClass() {
        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String PATTERN = "%d [%p] %C{1}: %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.DEBUG);
        console.activateOptions();
        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(console);
    }

    //The graph used to inject
    private MvcGraph mvcGraph;

    private CounterControllerImpl counterController;

    protected EventBus eventBusC;
    protected EventBus eventBusV;
    protected ExecutorService executorService;

    private void prepareGraph() {
        eventBusC = new EventBusImpl();
        eventBusV = new EventBusImpl();
        executorService = mock(ExecutorService.class);

        Injector.configGraph(new MvcGraph.BaseDependencies() {
            @Override
            public EventBus createEventBusC() {
                return eventBusC;
            }

            @Override
            public EventBus createEventBusV() {
                return eventBusV;
            }

            @Override
            public ExecutorService createExecutorService() {
                return executorService;
            }
        });
    }

    @Before
    public void setUp() throws Exception {
        prepareGraph();
        mvcGraph = Injector.getGraph();

        //create instance of CounterController
        counterController = new CounterControllerImpl();

        //inject dependencies into controller
        mvcGraph.inject(counterController);

        //init controller
        counterController.onConstruct();
    }

    @Test
    public void increment_should_post_counter_update_event_with_incremented_value() {
        //1. Prepare
        //prepare event monitor
        class Monitor {
            void onEvent(CounterController.EventC2V.OnCounterUpdated event) {
            }
        }
        Monitor monitor = mock(Monitor.class);
        eventBusV.register(monitor);

        //mock controller model for count value
        int value = new Random().nextInt();
        CounterModel counterModel = mock(CounterModel.class);
        when(counterModel.getCount()).thenReturn(value);
        //Bind the mock model to the controller
        counterController.bindModel(this, counterModel);

        //2. Act
        counterController.increment(this);

        //3. Verify
        ArgumentCaptor<CounterController.EventC2V.OnCounterUpdated> updateEvent
                = ArgumentCaptor.forClass(CounterController.EventC2V.OnCounterUpdated.class);
        //event should be fired once
        verify(monitor, times(1)).onEvent(updateEvent.capture());
        //event should carry incremented value
        Assert.assertEquals(value + 1, updateEvent.getValue().getCount());
    }

    @Test
    public void should_navigate_to_locationB_when_go_to_advance_view_and_back_to_locationA_after_go_to_basic_view() {
        //Prepare
        NavigationManager navigationManager = ((CounterControllerImpl) counterController).navigationManager;
        NavigationManager.Model navModel = navigationManager.getModel();
        //App has not navigated to anywhere, current location should be null
        Assert.assertNull(navModel.getCurrentLocation());
        //Simulate navigating to location A
        navigationManager.navigate(this).to("LocationA");
        //Verify: location should be changed to LocationA
        Assert.assertEquals(navModel.getCurrentLocation().getLocationId(), "LocationA");

        //Act: CounterController now goes to advanced view underlining logic is navigating to locationB
        counterController.goToAdvancedView(this);

        //Verify: Current location should be LocationB
        Assert.assertEquals(navModel.getCurrentLocation().getLocationId(), "LocationB");

        //Act: CounterController now goes back to basic view underlining logic is navigating back to locationA
        counterController.goBackToBasicView(this);

        //Verify: Current location should be back to LocationA
        Assert.assertEquals(navModel.getCurrentLocation().getLocationId(), "LocationA");
    }

}
