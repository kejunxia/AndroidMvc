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

package com.shipdream.lib.android.mvc.event.bus.internal;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestEventBus {
    private EventBusImpl eventBus;

    @Before
    public void setUp() throws Exception {
        eventBus = new EventBusImpl();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionToRegisterNullSubscriber() {
        eventBus.register(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionToUnregisterNullSubscriber() {
        eventBus.unregister(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionToPostNullEvent() {
        eventBus.post(null);
    }

    @Test
    public void shouldBeAbleToRegisterEventsForSingleSubscriber() {
        //Arrange
        class Event1{}
        class Event2{}
        class Event3{}

        class Subscriber {
            public void onEvent(Event1 event1) {
            }
            void onEvent(Event2 event2) {
            }
            private void onEvent(Event3 event3) {
            }
        }

        Subscriber sub = new Subscriber();

        //Action
        eventBus.register(sub);

        //Assert
        Assert.assertEquals(eventBus.subscribers.size(), 3);
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event1.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event2.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event3.class));
    }

    @Test
    public void shouldBeAbleToRegisterEventsForMultipleSubscribers() {
        //Arrange
        class Event1{}
        class Event2{}
        class Event3{}

        class Subscriber {
            public void onEvent(Event1 event1) {
            }
            void onEvent(Event2 event2) {
            }
            private void onEvent(Event3 event3) {
            }
        }

        Subscriber sub = new Subscriber();
        Subscriber sub2 = new Subscriber();

        //Action
        eventBus.register(sub);
        eventBus.register(sub);

        //Assert
        Assert.assertEquals(eventBus.subscribers.size(), 3);
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event1.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event2.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event3.class));
        Assert.assertEquals(eventBus.subscribers.get(Event1.class).size(), 1);
        Assert.assertEquals(eventBus.subscribers.get(Event2.class).size(), 1);
        Assert.assertEquals(eventBus.subscribers.get(Event3.class).size(), 1);

        //Action
        eventBus.register(sub2);

        //Assert
        Assert.assertEquals(eventBus.subscribers.size(), 3);
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event1.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event2.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event3.class));
        Assert.assertEquals(eventBus.subscribers.get(Event1.class).size(), 2);
        Assert.assertEquals(eventBus.subscribers.get(Event2.class).size(), 2);
        Assert.assertEquals(eventBus.subscribers.get(Event3.class).size(), 2);
    }

    @Test
    public void shouldBeAbleToUnregisterEventsForSingleSubscriber() {
        //Arrange
        class Event1{}
        class Event2{}
        class Event3{}

        class Subscriber1 {
            public void onEvent(Event1 event1) {
            }
            void onEvent(Event2 event2) {
            }
            private void onEvent(Event3 event3) {
            }
        }

        Subscriber1 sub = new Subscriber1();

        //Action
        eventBus.register(sub);

        //Assert
        Assert.assertEquals(eventBus.subscribers.size(), 3);
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event1.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event2.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event3.class));

        //Action
        eventBus.unregister(sub);

        //Assert
        Assert.assertEquals(eventBus.subscribers.size(), 0);
    }

    @Test
    public void shouldBeAbleToUnregisterEventsForMultipleSubscribers() {
        //Arrange
        class Event1{}
        class Event2{}
        class Event3{}

        class Subscriber {
            public void onEvent(Event1 event1) {
            }
            void onEvent(Event2 event2) {
            }
            private void onEvent(Event3 event3) {
            }
        }

        Subscriber sub = new Subscriber();
        Subscriber sub2 = new Subscriber();

        //Action
        eventBus.register(sub);
        eventBus.register(sub);

        //Assert
        Assert.assertEquals(eventBus.subscribers.size(), 3);
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event1.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event2.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event3.class));
        Assert.assertEquals(eventBus.subscribers.get(Event1.class).size(), 1);
        Assert.assertEquals(eventBus.subscribers.get(Event2.class).size(), 1);
        Assert.assertEquals(eventBus.subscribers.get(Event3.class).size(), 1);

        //Action
        eventBus.register(sub2);

        //Assert
        Assert.assertEquals(eventBus.subscribers.size(), 3);
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event1.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event2.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event3.class));
        Assert.assertEquals(eventBus.subscribers.get(Event1.class).size(), 2);
        Assert.assertNotNull(eventBus.subscribers.get(Event1.class).get(sub));
        Assert.assertNotNull(eventBus.subscribers.get(Event1.class).get(sub2));
        Assert.assertEquals(eventBus.subscribers.get(Event2.class).size(), 2);
        Assert.assertNotNull(eventBus.subscribers.get(Event2.class).get(sub));
        Assert.assertNotNull(eventBus.subscribers.get(Event2.class).get(sub2));
        Assert.assertEquals(eventBus.subscribers.get(Event3.class).size(), 2);
        Assert.assertNotNull(eventBus.subscribers.get(Event3.class).get(sub));
        Assert.assertNotNull(eventBus.subscribers.get(Event3.class).get(sub2));

        //Action
        eventBus.unregister(sub);

        //Assert
        Assert.assertEquals(eventBus.subscribers.size(), 3);
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event1.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event2.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event3.class));
        Assert.assertEquals(eventBus.subscribers.get(Event1.class).size(), 1);
        Assert.assertTrue(!eventBus.subscribers.get(Event1.class).containsKey(sub));
        Assert.assertTrue(eventBus.subscribers.get(Event1.class).containsKey(sub2));
        Assert.assertNotNull(eventBus.subscribers.get(Event1.class).get(sub2));
        Assert.assertEquals(eventBus.subscribers.get(Event2.class).size(), 1);
        Assert.assertTrue(!eventBus.subscribers.get(Event2.class).containsKey(sub));
        Assert.assertTrue(eventBus.subscribers.get(Event2.class).containsKey(sub2));
        Assert.assertEquals(eventBus.subscribers.get(Event3.class).size(), 1);
        Assert.assertTrue(!eventBus.subscribers.get(Event2.class).containsKey(sub));
        Assert.assertTrue(eventBus.subscribers.get(Event2.class).containsKey(sub2));

        //Action
        eventBus.unregister(sub);

        //Assert
        Assert.assertEquals(eventBus.subscribers.size(), 3);
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event1.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event2.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event3.class));
        Assert.assertEquals(eventBus.subscribers.get(Event1.class).size(), 1);
        Assert.assertTrue(!eventBus.subscribers.get(Event1.class).containsKey(sub));
        Assert.assertTrue(eventBus.subscribers.get(Event1.class).containsKey(sub2));
        Assert.assertNotNull(eventBus.subscribers.get(Event1.class).get(sub2));
        Assert.assertEquals(eventBus.subscribers.get(Event2.class).size(), 1);
        Assert.assertTrue(!eventBus.subscribers.get(Event2.class).containsKey(sub));
        Assert.assertTrue(eventBus.subscribers.get(Event2.class).containsKey(sub2));
        Assert.assertEquals(eventBus.subscribers.get(Event3.class).size(), 1);
        Assert.assertTrue(!eventBus.subscribers.get(Event2.class).containsKey(sub));
        Assert.assertTrue(eventBus.subscribers.get(Event2.class).containsKey(sub2));

        //Action
        eventBus.unregister(sub2);

        //Assert
        Assert.assertEquals(eventBus.subscribers.size(), 0);
    }

    @Test
    public void shouldBeAbleToRegisterEventsWithInheritedMethods() {
        //Arrange
        class Event1{}
        class Event2{}
        class Event3{}
        class Event4{}
        class Event5{}


        class Subscriber1 {
            public void onEvent(Event1 event1) {
            }
            void onEvent(Event2 event2) {
            }
            private void onEvent(Event3 event3) {
            }
        }

        class Subscriber2 extends Subscriber1 {
            protected void onEvent(Event4 event4) {
            }
            void onEvent(Event5 event5) {
            }
            public void onEvent(Event1 event1) {
            }
        }

        Subscriber2 sub = new Subscriber2();

        //Action
        eventBus.register(sub);

        //Assert
        Assert.assertEquals(eventBus.subscribers.size(), 5);
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event1.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event2.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event3.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event4.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event5.class));
    }

    @Test
    public void shouldBeAbleToUnregisterEventsWithInheritedMethods() {
        //Arrange
        class Event1{}
        class Event2{}
        class Event3{}
        class Event4{}
        class Event5{}

        class Subscriber1 {
            public void onEvent(Event1 event1) {
            }
            void onEvent(Event2 event2) {
            }
            private void onEvent(Event3 event3) {
            }
        }

        class Subscriber2 extends Subscriber1 {
            @Override
            public void onEvent(Event1 event1) {
            }
            protected void onEvent(Event4 event4) {
            }
            void onEvent(Event5 event5) {
            }
        }

        Subscriber2 sub = new Subscriber2();

        //Action
        eventBus.register(sub);

        //Assert
        Assert.assertEquals(eventBus.subscribers.size(), 5);
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event1.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event2.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event3.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event4.class));
        Assert.assertTrue(eventBus.subscribers.keySet().contains(Event5.class));

        //Action
        eventBus.unregister(sub);

        //Assert
        Assert.assertEquals(eventBus.subscribers.size(), 0);
    }

    @Test
    public void shouldBeAbleToReceiveEventInRegisteredSubscriberButNotAfterUnregistration() {
        //Arrange
        class Event1{}
        class Subscriber {
            public void onEvent(Event1 event) {
            }
        }
        Subscriber sub = mock(Subscriber.class);
        Subscriber sub2 = mock(Subscriber.class);
        eventBus.register(sub);
        eventBus.register(sub2);

        Event1 event = new Event1();

        //Action
        eventBus.post(event);
        ArgumentCaptor<Event1> msg = ArgumentCaptor.forClass(Event1.class);

        //Assert
        verify(sub).onEvent(msg.capture());
        verify(sub2).onEvent(msg.capture());

        //Arrange
        reset(sub);
        reset(sub2);
        eventBus.unregister(sub);

        //Action
        eventBus.post(event);
        msg = ArgumentCaptor.forClass(Event1.class);

        //Assert
        verify(sub, times(0)).onEvent(msg.capture());
        verify(sub2).onEvent(msg.capture());

        //Arrange
        reset(sub);
        reset(sub2);
        eventBus.unregister(sub2);

        //Action
        eventBus.post(event);
        msg = ArgumentCaptor.forClass(Event1.class);

        //Assert
        verify(sub, times(0)).onEvent(msg.capture());
        verify(sub2, times(0)).onEvent(msg.capture());
    }

    @Test
    public void shouldBeAbleToReceiveEventInRegisteredSubscriberButNotAfterUnregistrationForInheritedMethod() {
        //Arrange
        class Event1{
        }

        class Event2{
        }

        class Event3{
        }

        class EventHandler {
            void handleEvent(Event1 event) {
            }
            void handleEvent(Event2 event) {
            }
            void handleEvent(Event3 event) {
            }
        }

        final EventHandler handler = mock(EventHandler.class);
        final EventHandler handler2 = mock(EventHandler.class);

        class Subscriber {
            public void onEvent(Event1 event) {
                handler.handleEvent(event);
            }

            public void onEvent(Event2 event) {
                handler.handleEvent(event);
            }
        }

        class Subscriber2 extends Subscriber {
            public void onEvent(Event1 event) {
                handler2.handleEvent(event);
            }

            public void onEvent(Event3 event) {
                handler2.handleEvent(event);
            }
        }

        Subscriber sub = new Subscriber();
        Subscriber sub2 = new Subscriber2();
        eventBus.register(sub);
        eventBus.register(sub2);

        Event1 event = new Event1();
        Event2 event2 = new Event2();
        Event3 event3 = new Event3();

        //Action
        eventBus.post(event);
        eventBus.post(event2);
        eventBus.post(event3);

        //Assert
        verify(handler, times(1)).handleEvent(any(Event1.class));
        //Time1: handled by sub
        //Time2: handled by sub2 which inherited onEvent(Event2) from sub1 and call handler.handlerEvent(Event2) again
        verify(handler, times(2)).handleEvent(any(Event2.class));

        verify(handler2, times(1)).handleEvent(any(Event1.class));
        verify(handler2, times(1)).handleEvent(any(Event3.class));
        verify(handler2, times(1)).handleEvent(any(Event3.class));

        //Arrange
        reset(handler);
        reset(handler2);
        eventBus.unregister(sub);

        //Action
        eventBus.post(event);
        eventBus.post(event2);
        eventBus.post(event3);

        //Assert
        verify(handler, times(0)).handleEvent(any(Event1.class));
        //Time1: handled by sub2 which inherited onEvent(Event2) from sub1 and call handler.handlerEvent(Event2) again
        verify(handler, times(1)).handleEvent(any(Event2.class));

        verify(handler2, times(1)).handleEvent(any(Event1.class));
        verify(handler2, times(1)).handleEvent(any(Event3.class));
        verify(handler2, times(1)).handleEvent(any(Event3.class));

        //Arrange
        reset(handler);
        reset(handler2);
        eventBus.unregister(sub2);

        //Action
        eventBus.post(event);
        eventBus.post(event2);
        eventBus.post(event3);

        //Assert
        verify(handler, times(0)).handleEvent(any(Event1.class));
        verify(handler, times(0)).handleEvent(any(Event2.class));

        verify(handler2, times(0)).handleEvent(any(Event1.class));
        verify(handler2, times(0)).handleEvent(any(Event3.class));
        verify(handler2, times(0)).handleEvent(any(Event3.class));
    }
}
