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

package com.shipdream.lib.android.mvp;

import com.shipdream.lib.android.mvp.event.BaseEventC;
import com.shipdream.lib.android.mvp.event.bus.EventBus;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestManagerImpl {
    @After
    public void tearDown() throws Exception {
        Injector.mvpGraph = null;
    }

    @Test
    public void should_not_crash_when_manager_post_event_even_without_registering_controller_event_bus() {
        class MyManager extends AbstractManager {
            void doPost(){
                postEvent2C(mock(BaseEventC.class));
            }

            @Override
            public Class modelType() {
                return null;
            }
        };

        MyManager myManager = new MyManager();
        //Haven't registered controller event yet, but should not throw exception but just log
        myManager.doPost();
    }

    @Test
    public void should_be_able_to_post_events_to_controller_event_bus_from_manager() {
        class MyEvent extends BaseEventC{
            public MyEvent(Object sender) {
                super(sender);
            }
        }

        final MyEvent myEvent = mock(MyEvent.class);

        class MyManager extends AbstractManager {
            void doPost(){
                postEvent2C(myEvent);
            }

            @Override
            public Class modelType() {
                return null;
            }
        }

        final EventBus bus = mock(EventBus.class);

        Injector.configGraph(new MvpGraph.BaseDependencies() {
            @Override
            protected EventBus createEventBusC() {
                return bus;
            }

            @Override
            protected ExecutorService createExecutorService() {
                return null;
            }
        });

        MyManager myManager = new MyManager();
        Injector.getGraph().inject(myManager);

        verify(bus, times(0)).post(anyObject());

        myManager.doPost();

        verify(bus, times(1)).post(myEvent);
    }
}
