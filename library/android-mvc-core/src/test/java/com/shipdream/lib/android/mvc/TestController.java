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
import com.shipdream.lib.poke.Provides;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestController extends BaseTest{
    private UiThreadRunner uiThreadRunner;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        uiThreadRunner = mock(UiThreadRunner.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = (Runnable) invocation.getArguments()[0];
                runnable.run();
                return null;
            }
        }).when(uiThreadRunner).post(any(Runnable.class));

        eventBusC = mock(EventBus.class);

        eventBusV = mock(EventBus.class);

        MvcComponent component = new MvcComponent("");
        component.register(new Object(){
            @Provides
            UiThreadRunner uiThreadRunner() {
                return uiThreadRunner;
            }

            @Provides
            @EventBusC
            EventBus eventBus2C() {
                return eventBusC;
            }

            @Provides
            @EventBusV
            EventBus eventBus2V() {
                return eventBusV;
            }
        });
        graph.getRootComponent().attach(component, true);
    }

    @Test
    public void should_post_event_to_event2v_channel_old() throws Exception {
        Controller controller = new Controller() {
            @Override
            public Class modelType() {
                return null;
            }
        };
        graph.inject(controller);

        String event = ";";
        controller.postEvent(event);

        verify(eventBusV).post(eq(event));
    }

    @Test
    public void should_post_event_to_event2v_channel() throws Exception {
        Controller controller = new Controller() {
            @Override
            public Class modelType() {
                return null;
            }
        };
        graph.inject(controller);

        String event = ";";
        controller.postEvent2V(event);

        verify(eventBusV).post(eq(event));
    }

    @Test
    public void should_post_event_to_event2c_channel() throws Exception {
        Controller controller = new Controller() {
            @Override
            public Class modelType() {
                return null;
            }
        };
        graph.inject(controller);

        String event = ";";
        controller.postEvent2C(event);

        verify(eventBusC).post(eq(event));
    }

    @Test
    public void should_post_wrapped_MvcGraphException_when_run_async_task() {
        Controller controller = new Controller() {
            @Override
            public Class modelType() {
                return null;
            }
        };

        graph.inject(controller);

        boolean exceptionCaught = false;
        final MvcGraphException exp = new MvcGraphException("");
        try {
            controller.runTask(new Task() {
                @Override
                public Object execute(Monitor monitor) throws Exception {
                    throw exp;
                }
            });
        } catch (Exception e) {
            exceptionCaught = true;
            Assert.assertTrue(e.getCause() == exp);
        }

        Assert.assertTrue(exceptionCaught);
    }

    @Test
    public void should_post_wrapped_other_exception_when_run_async_task_without_callback() {
        Controller controller = new Controller() {
            @Override
            public Class modelType() {
                return null;
            }
        };

        graph.inject(controller);

        boolean exceptionCaught = false;
        final Exception exp = new Exception("");
        try {
            controller.runTask(new Task() {
                @Override
                public Object execute(Monitor monitor) throws Exception {
                    throw exp;
                }
            });
        } catch (Exception e) {
            exceptionCaught = true;
            Assert.assertTrue(e.getCause() == exp);
        }

        Assert.assertTrue(exceptionCaught);
    }
}

