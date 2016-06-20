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

import android.app.Service;

import com.shipdream.lib.android.mvc.event.BaseEventV;
import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Android service can be thought as a kind of view sitting on top and driven by controller which
 * manage the state of the app.
 */
public abstract class MvcService<CONTROLLER extends Controller> extends Service implements View{
    private EventRegister eventRegister;
    protected CONTROLLER controller;

    /**
     * Specify the controller of this service
     * @return The class type of the controller
     */
    protected abstract Class<CONTROLLER> getControllerClass();

    /**
     * Callback of creating a service
     */
    @Override
    public void onCreate() {
        super.onCreate();

        if (getControllerClass() == null) {
            throw new IllegalArgumentException("Must specify the controller class type for " +
                    "fragment " + this.getClass().getName());
        }

        try {
            controller = Mvc.graph().reference(getControllerClass(), null);
        } catch (PokeException e) {
            throw new IllegalArgumentException("Unable to find controller "
                    + getControllerClass().getName() + ". Either create a controller with " +
                    "default constructor or register it to Mvc.graph().getRootComponent()");
        }

        controller.view = this;

        Mvc.graph().inject(this);

        eventRegister = new EventRegister(this);
        eventRegister.onCreate();
        eventRegister.registerEventBuses();
    }

    /**
     * Callback of destroying a service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        eventRegister.unregisterEventBuses();
        eventRegister.onDestroy();

        try {
            Mvc.graph().dereference(controller, getControllerClass(), null);
        } catch (ProviderMissingException e) {
            //should never happen
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.warn("Failed to dereference controller " + getControllerClass().getName(), e);
        }

        Mvc.graph().release(this);
    }

    /**
     * Handy method to post an event to other views directly. However, when possible, it's
     * recommended to post events from controllers to views.
     * @param event
     */
    protected void postEvent2V(BaseEventV event) {
        eventRegister.postEvent2V(event);
    }
}
