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

import com.shipdream.lib.poke.Graph;
import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Android service can be thought as a kind of view sitting on top and driven by controller which
 * manage the state of the app.
 */
public abstract class MvcService<CONTROLLER extends Controller> extends Service implements UiView {
    private EventRegister eventRegister;
    protected CONTROLLER controller;
    private Graph.Monitor graphMonitor;

    /**
     * Specify the controller of this service. Returns null if this service doesn't need a controller
     * @return The class type of the controller
     */
    protected abstract Class<CONTROLLER> getControllerClass();

    /**
     * Callback of creating a service
     */
    @Override
    public void onCreate() {
        super.onCreate();

        graphMonitor = new Graph.Monitor() {
            @Override
            public void onInject(Object target) {
                if (controller != null && target == MvcService.this) {
                    controller.view = MvcService.this;
                }
            }

            @Override
            public void onRelease(Object target) {

            }
        };
        Mvc.graph().registerMonitor(graphMonitor);

        if (getControllerClass() != null) {
            try {
                controller = Mvc.graph().reference(getControllerClass(), null);
            } catch (CircularDependenciesException e) {
                e.printStackTrace();
            } catch (ProvideException e) {
                e.printStackTrace();
            } catch (ProviderMissingException e) {
                throw new IllegalStateException("Unable to inject "
                        + getControllerClass().getName() + ".\n" + e.getMessage(), e);
            }
        }

        Mvc.graph().inject(this);

        eventRegister = new EventRegister(this);
        eventRegister.registerEventBuses();
    }

    /**
     * Callback of destroying a service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        eventRegister.unregisterEventBuses();

        if (getControllerClass() != null) {
            try {
                Mvc.graph().dereference(controller, getControllerClass(), null);
            } catch (ProviderMissingException e) {
                //should never happen
                Logger logger = LoggerFactory.getLogger(getClass());
                logger.warn("Failed to dereference controller " + getControllerClass().getName(), e);
            }
        }

        Mvc.graph().release(this);

        Mvc.graph().unregisterMonitor(graphMonitor);
    }

    /**
     * Handy method to post an event to other views directly. However, when possible, it's
     * recommended to post events from controllers to views to keep views' logic simple.
     * @param event
     */
    protected void postEvent2V(Object event) {
        eventRegister.postEvent2V(event);
    }
}
