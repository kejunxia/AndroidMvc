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

package com.shipdream.lib.android.mvc.manager;

/**
 * This is the manager contract that needs to be extended by managers. When multiple controllers
 * share logic and data, extract them into a manager.
 * @param <MODEL>
 */
public interface BaseManager<MODEL> {
    /**
     * Gets the model which represents the state of the manager. <b>Don't change any values of
     * the model by its setter methods inside from controllers.</b> Only this manager can change its
     * own state directly.
     *
     * @return null when the manager doesn't need to get its model saved and restored
     * automatically.
     */
    MODEL getModel();

    /**
     * Binds a non-null model to the manager
     * @param sender Who wants to bind it
     * @param model The model to bind to this manager. CANNOT be NULL otherwise a runtime
     *              exception will be thrown
     */
    void bindModel(Object sender, MODEL model);
}
