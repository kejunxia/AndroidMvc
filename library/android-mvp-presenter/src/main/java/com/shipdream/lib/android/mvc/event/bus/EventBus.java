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

package com.shipdream.lib.android.mvc.event.bus;

import com.shipdream.lib.android.mvc.event.bus.internal.EventBusImpl;

/**
 * Event2C bus post events based on event class type. Subscriber uses method naming pattern to handle
 * call back from raised events. To handle an event with class type [EventType], subscribers
 * just need to have method named as <b>onEvent([EventType] event)</b>. Accessibility of the method
 * doesn't matter, so private, protected, public and package access are all allowed. There is an
 * internal implementation {@link EventBusImpl}
 *
 * <p>Event2C will be posted on the <b>SAME</b> thread the caller is running on by calling
 * {@link #post(Object)}</p>
 *
 * <p>Example:</p>
 * <pre>
{@code
 public class EventBusExample {
     private EventBus eventBus;

     //Event2C definition
     public static class MyEvent {
        public String message;
     }

     //Subscriber using onEvent(MyEvent event) to respond the events with type MyEvent
     public static class Subscriber {
         //Use naming convention to notate this method is going to handle any posted events with
         //class type MyEvent
         public void onEvent(MyEvent event) {
            System.out.print("Hey, I got your message: " + event.message);
         }
     }

     public void showExample() {
         //Prepare an event bus
         eventBus = new EventBusImpl();

         //Prepare a subscriber
         Subscriber subscriber = new Subscriber();

         //Register the subscriber to the event bus
         eventBus.register(subscriber);

         //Generate an event with type MyEvent
         MyEvent event = new MyEvent();
         event.message = "New email received.";

         //Post this event to the event bus
         eventBus.post(event);

         //Now the subscriber should receive the event and print the message by its method
         //Subscriber.onEvent(MyEvent event)

         //Message printed in console as below
         //Hey, I got your message: New email received.
     }
 }
}
 * </pre>
 */
public interface EventBus {
    /**
     * Register the subscriber to this event bus. Duplicate register against same subscriber is
     * allowed but subsequent registration after the first one will be ignored.
     * @param subscriber The subscriber. Null is not allowed and will throw {@link IllegalArgumentException}
     */
    void register(Object subscriber);
    /**
     * Unregister the subscriber from this event bus. If the subscriber is never registered before,
     * this method won't take any effect
     * @param subscriber The subscriber. Null is not allowed and will throw {@link IllegalArgumentException}
     */
    void unregister(Object subscriber);

    /**
     * Post the given event to all registered subscribers.
     * <p>Event2C will be posted on the <b>SAME</b> thread the caller is running on</p>
     *
     * @param event The event. Null is not allowed and will throw {@link IllegalArgumentException}
     */
    void post(Object event);
}
