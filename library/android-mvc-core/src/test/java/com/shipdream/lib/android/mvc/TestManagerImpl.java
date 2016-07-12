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
import com.shipdream.lib.poke.Graph;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;

import org.junit.Test;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestManagerImpl extends BaseTest {
    @Test
    public void should_not_crash_when_manager_post_event_even_without_registering_controller_event_bus() {
        class MyManager extends com.shipdream.lib.android.mvc.Manager {
            void doPost(){
                postEvent2C(mock(Object.class));
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
    public void should_be_able_to_post_events_to_controller_event_bus_from_manager()
            throws ProvideException, ProviderConflictException, Graph.IllegalRootComponentException {
        class MyEvent{
            public MyEvent() { }
        }

        final MyEvent myEvent = mock(MyEvent.class);

        class MyManager extends Manager {
            void doPost(){
                postEvent2C(myEvent);
            }

            @Override
            public Class modelType() {
                return null;
            }
        }

        final EventBus bus = mock(EventBus.class);

        graph.setRootComponent((MvcComponent) new MvcComponent("Root").register(new Object() {
            @Provides
            @EventBusC
            protected EventBus createEventBusC() {
                return bus;
            }
        }));

        MyManager myManager = new MyManager();
        graph.inject(myManager);

        verify(bus, times(0)).post(anyObject());

        myManager.doPost();

        verify(bus, times(1)).post(myEvent);
    }
}
