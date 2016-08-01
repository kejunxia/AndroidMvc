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

package com.shipdream.lib.android.mvc.event.bus.internal;

import com.shipdream.lib.android.mvc.event.bus.EventBus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class EventBusImpl implements EventBus {
    Map<Class<?>, Map<Object, Method>> subscribers = new LinkedHashMap<>();

    public void register(Object subscriber) {
        if(subscriber == null) {
            throw new IllegalArgumentException("Subscriber registering to an event bus must not be NULL");
        }

        Class<?> subscriberClass = subscriber.getClass();

        while (subscriberClass != null) {
            String name = subscriberClass.getName();
            if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
                // Ignore system classes
                break;
            }

            Method[] methods = subscriberClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals("onEvent") && method.getParameterTypes().length == 1) {
                    Class<?> eventType = method.getParameterTypes()[0];

                    Map<Object, Method> subs = subscribers.get(eventType);
                    if (subs == null) {
                        subs = new LinkedHashMap<>();
                        subscribers.put(eventType, subs);
                    }

                    subs.put(subscriber, method);
                }
            }

            subscriberClass = subscriberClass.getSuperclass();
        }
    }

    public void unregister(Object subscriber) {
        if(subscriber == null) {
            throw new IllegalArgumentException("Subscriber unregistering to an event bus must not be NULL");
        }

        Class<?> subscriberClass = subscriber.getClass();
        while (subscriberClass != null) {
            String name = subscriberClass.getName();
            if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
                // Ignore system classes
                break;
            }

            Method[] methods = subscriberClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals("onEvent") && method.getParameterTypes().length == 1) {
                    Class eventType = method.getParameterTypes()[0];
                    Map<Object, Method> subs = subscribers.get(eventType);

                    if(subs != null) {
                        subs.remove(subscriber);
                        if (subs.isEmpty()) {
                            subscribers.remove(eventType);
                        }
                    }
                }
            }

            subscriberClass = subscriberClass.getSuperclass();
        }
    }

    public void post(Object event) {
        if(event == null) {
            throw new IllegalArgumentException("Event2C bus can't post a NULL event");
        }

        Map<Object, Method> subs = subscribers.get(event.getClass());
        if(subs != null) {
            for (Map.Entry<Object, Method> entry : subs.entrySet()) {
                entry.getValue().setAccessible(true);
                try {
                    entry.getValue().invoke(entry.getKey(), event);
                } catch (IllegalAccessException e) {
                    //This should never happen since setAccessible has already opened the access
                    throw new RuntimeException("Not able to post event - "
                            + event.getClass().getName() + " due to IllegalAccessException: " + e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    String msg = e.getMessage();
                    if (msg == null || msg.isEmpty() && e.getCause() != null) {
                        msg = e.getCause().getMessage();
                    }
                    throw new RuntimeException("Not able to post event - "
                            + event.getClass().getName() + " due to error: " + msg, e);
                }
            }
        }
    }
}
