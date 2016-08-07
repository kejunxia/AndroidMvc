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

package com.shipdream.lib.android.mvc.samples.simple.mvp.controller.internal;

import com.shipdream.lib.android.mvc.Mvc;
import com.shipdream.lib.android.mvc.MvcComponent;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusV;
import com.shipdream.lib.android.mvc.event.bus.internal.EventBusImpl;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Provides;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseTest {
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

    protected EventBus eventBusC;
    protected EventBus eventBusV;
    protected ExecutorService executorService;
    private MvcComponent overridingComponent;

    /**
     * Prepare the injecting providers for unit test. Register mocking objects to the overriding
     * component
     * @param overridingComponent The overriding component used to register mocking objects
     * @throws Exception Exception may be thrown out
     */
    protected void prepareGraph(MvcComponent overridingComponent) throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        eventBusC = new EventBusImpl();
        eventBusV = new EventBusImpl();

        //Mock executor service so that all async tasks run on non-UI thread in app will
        //run on the testing thread (main thread for testing) to avoid multithreading headache
        executorService = mock(ExecutorService.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Callable runnable = (Callable) invocation.getArguments()[0];
                runnable.call();
                Future future = mock(Future.class);
                when(future.isDone()).thenReturn(true); //by default execute immediately succeed.
                when(future.isCancelled()).thenReturn(false);
                return future;
            }
        }).when(executorService).submit(any(Callable.class));

        overridingComponent = new MvcComponent("TestOverridingComponent");
        overridingComponent.register(new Object(){
            @Provides
            @EventBusC
            public EventBus createEventBusC() {
                return eventBusC;
            }

            @Provides
            @EventBusV
            public EventBus createEventBusV() {
                return eventBusV;
            }

            @Provides
            public ExecutorService createExecutorService() {
                return executorService;
            }
        });

        //For base test class, allow sub test cases to register overriding providers
        prepareGraph(overridingComponent);

        Component rootComponent = Mvc.graph().getRootComponent();

        //overriding indicates providers of this component attached to the root component will override
        //existing providers managing to provide instances with the same type and qualifier.
        boolean overriding = true;
        rootComponent.attach(overridingComponent, overriding);

        Mvc.graph().inject(this);
    }

    @After
    public void tearDown() throws Exception {
        Component rootComponent = Mvc.graph().getRootComponent();
        rootComponent.detach(overridingComponent);
    }
}
