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

package com.shipdream.lib.android.mvc.controller;

import com.shipdream.lib.android.mvc.event.bus.internal.EventBusImpl;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.MvcGraph;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class BaseControllerTest {
    protected MvcGraph graph;
    protected EventBus eventBusC2C;
    protected EventBus eventBusC2V;
    protected ExecutorService executorService;

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

    @Before
    public void setUp() throws Exception {
        eventBusC2C = new EventBusImpl();
        eventBusC2V = new EventBusImpl();
        executorService = mock(ExecutorService.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = (Runnable) invocation.getArguments()[0];
                runnable.run();
                return null;
            }
        }).when(executorService).submit(any(Runnable.class));

        graph = new MvcGraph(new ControllerDependencies(this));
    }

    static class ControllerDependencies extends MvcGraph.BaseDependencies {
        private BaseControllerTest baseControllerTest;

        public ControllerDependencies(BaseControllerTest baseControllerTest) {
            this.baseControllerTest = baseControllerTest;
        }

        @Override
        public EventBus createEventBusC2C() {
            return baseControllerTest.eventBusC2C;
        }

        @Override
        public EventBus createEventBusC2V() {
            return baseControllerTest.eventBusC2V;
        }

        @Override
        public ExecutorService createExecutorService() {
            return baseControllerTest.executorService;
        }
    }
}
