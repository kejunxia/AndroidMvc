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

package com.shipdream.lib.android.mvp.controller;

import com.shipdream.lib.android.mvp.manager.internal.BaseManagerImpl;

/**
 * <p>
 * Base controller interface. A controller is a proxy between an Android view and core business
 * logic. The model of a controller represents the state of the view that the controller is tied to.
 * All UI interactions captured by a view should be translated into method calls that the
 * corresponding controller understands. Once the method finishes the process and updates model, it
 * sends an event back to notify the view. Then view updates based on the updated model or the data
 * encapsulated in the event.
 * </p>
 * <p>
 * Model of the controller will be saved and restored automatically by the MvcFramework. So at any
 * time the view can update its state based on the date retrieved from {@link #getModel()}
 * </p>
 *
 * <p>
 * All logic should be abstracted into controller from view as much as possible result in easier
 * unit testing. When multiple controllers share same logic and model, the shared logic could be
 * broken out into a shared manager. For example, a common scenario is multiple app pages need to
 * access the current logged in user status, therefore the corresponding controllers could refer
 * to an AccountManager which manages the logged in user. See {@link BaseManagerImpl}.
 *
 * </p>
 */
public interface BaseController<MODEL> {
    /**
     * Gets the model which represents the state of the controller. <b>Don't change any values of
     * the model by its setter methods inside a view.</b> A view should only read the values of the
     * model of the controller through its getters. It's controllers' responsibility to change it's
     * own model with the bossiness logic in the controller.
     *
     * @return null when the controller doesn't need to get its model saved and restored
     * automatically. e.g. The controller always loads resource from remote services so that
     * its model can be thought persisted by the remote services. Otherwise return the instance of the
     * model whose model will be automatically saved and restored.
     */
    MODEL getModel();

    /**
     * Binds a non-null model to the controller
     * @param sender Who wants to bind it
     * @param model The model to bind to this controller. CANNOT be NULL otherwise a runtime
     *              exception will be thrown
     */
    void bindModel(Object sender, MODEL model);
}
