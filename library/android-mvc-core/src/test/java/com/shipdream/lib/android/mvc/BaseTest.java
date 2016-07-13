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

package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusV;
import com.shipdream.lib.android.mvc.event.bus.internal.EventBusImpl;
import com.shipdream.lib.poke.Provides;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class BaseTest {
    protected EventBus eventBusC;
    protected EventBus eventBusV;
    protected ExecutorService executorService;

    protected MvcGraph graph;

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
        graph = new MvcGraph();

        eventBusC = new EventBusImpl();
        eventBusV = new EventBusImpl();
        executorService = mock(ExecutorService.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = (Runnable) invocation.getArguments()[0];
                runnable.run();
                return null;
            }
        }).when(executorService).submit(any(Runnable.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Callable runnable = (Callable) invocation.getArguments()[0];
                return runnable.call();
            }
        }).when(executorService).submit(any(Callable.class));

        graph.setRootComponent((MvcComponent) new MvcComponent("TestRootComponent").register(new Object(){
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
            public ExecutorService executorService() {
                return executorService;
            }

            @Provides
            public UiThreadRunner uiThreadRunner() {
                return new UiThreadRunner() {
                    @Override
                    public boolean isOnUiThread() {
                        return true;
                    }

                    @Override
                    public void post(Runnable runnable) {
                        runnable.run();
                    }

                    @Override
                    public void postDelayed(Runnable runnable, long delayMs) {
                        runnable.run();
                    }
                };
            }
        }));
    }
}
