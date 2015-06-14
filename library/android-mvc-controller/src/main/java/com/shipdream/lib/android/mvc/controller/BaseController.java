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

package com.shipdream.lib.android.mvc.controller;

/**
 *
 */
public interface BaseController<MODEL> {

    /**
     * Gets the model which represents the state of the controller. <b>Don't change any values of
     * the model by its setter methods inside a view.</b> A view should only read the values of the
     * model of the controller through its getters. It's controllers' responsibility to change it's
     * own model with the bossiness logic in the controller.
     *
     * @return null when the controller doesn't need to get its state saved and restored
     * automatically. e.g. The controller always loads resource from remote services so that
     * its state can be thought persisted by the remote services. Otherwise return the instance of the
     * model whose state will be automatically saved and restored.
     */
    MODEL getModel();

    /**
     * Initialized the controller and create an empty model. If a specific model needs to be bound
     * to this controller use {@link #bindModel(Object, Object)}
     */
    void init();

    /**
     * Bind a prepared non-null model to the controller
     * @param sender Who wants to bind it
     * @param model The model to bind to this controller. CANNOT be NULL otherwise a runtime
     *              exception will be thrown
     */
    void bindModel(Object sender, MODEL model);

}
