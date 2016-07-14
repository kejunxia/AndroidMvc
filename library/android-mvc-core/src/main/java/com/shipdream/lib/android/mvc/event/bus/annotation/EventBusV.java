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

package com.shipdream.lib.android.mvc.event.bus.annotation;

import com.shipdream.lib.android.mvc.event.bus.EventBus;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Indicates the annotated event bus is for events sent to Android views. To send or receive events
 * for core components such as controllers, managers, core services, use {@link EventBusC} to annotate
 * the injecting {@link EventBus}.
 *
 * <p>
 * In Android app events through the event bus annotated by this annotation will be guaranteed to be
 * received <b>on Android's UI thread</b> automatically.
 * </p>
 *
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface EventBusV {
}
