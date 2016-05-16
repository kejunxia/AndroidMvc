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

package com.shipdream.lib.android.mvp.controller;

import com.shipdream.lib.android.mvp.Injector;
import com.shipdream.lib.android.mvp.MvcGraph;
import com.shipdream.lib.android.mvp.event.bus.EventBus;
import com.shipdream.lib.android.mvp.event.bus.internal.EventBusImpl;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.mock;

public class BaseTest {
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

    protected MvcGraph graph;

    @Before
    public void setUp() throws Exception {
        prepareGraph();
        graph = Injector.getGraph();
    }

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

    static class ControllerDependencies extends MvcGraph.BaseDependencies {
        private BaseTest baseTest;

        public ControllerDependencies(BaseTest baseTest) {
            this.baseTest = baseTest;
        }

        @Override
        public EventBus createEventBusC() {
            return baseTest.eventBusC;
        }

        @Override
        public EventBus createEventBusV() {
            return baseTest.eventBusV;
        }

        @Override
        public ExecutorService createExecutorService() {
            return baseTest.executorService;
        }
    }
}
