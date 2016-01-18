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

package com.shipdream.lib.android.mvc.event;

/**
 * Base event.
 */
public abstract class BaseEvent {
    private final Object sender;

    /**
     * Construct an event
     * @param sender Who initially sent the request that triggers this event
     */
    public BaseEvent(Object sender) {
        this.sender = sender;
    }

    /**
     * Gets the sender that tracks who initially sends a command leading to trigger this event. It's
     * useful when the origin of the event is important. For example, in a login controller there is
     * a login function - login(sender). It could be called by a button click operation or an
     * automatic call on app start up. Then the event handler may needs to handle the successful
     * login differently based on different login requesters when sender is useful to differentiate
     * these scenarios.
     *
     * @return Who initially sent the request that triggers this event.
     */
    public Object getSender(){
        return sender;
    }
}
