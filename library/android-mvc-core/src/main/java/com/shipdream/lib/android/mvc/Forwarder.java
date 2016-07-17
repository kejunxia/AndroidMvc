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

import org.jetbrains.annotations.NotNull;

/**
 * Configuration of forwarding navigation by setting
 * <ul>
 *     <li>{@link #setInterim(boolean)} indicating whether the location currently navigating to will
 *     be an interim in the navigation history, which means when {@link Navigator#back()} is called
 *     the interim locations will be skipped.</li>
 *     <li>{@link #clearTo(Class)} </li>
 *     <li>{@link #clearAll()}</li>
 * </ul>
 */
public class Forwarder {
    boolean interim = false;
    boolean clearHistory = false;
    String clearToLocationId;

    /**
     * Set whether this location navigating to is an interim location that won't be pushed to
     * history back stack.
     * @return
     */
    public Forwarder setInterim(boolean interim){
        this.interim = interim;
        return this;
    }

    /**
     * Indicates this location navigating to is an interim location that won't be pushed to
     * history back stack.
     * @return
     */
    public boolean isInterim() {
        return interim;
    }

    /**
     * Clear history to the first matched locationId. For example, current history is
     * A->B->A->C->B, clearToLocationId("A") will pop B and C and leave the back stack as A->B->A.
     *
     * <p>Note that, if {@link #clearAll()} is called, this method has no effect</p>
     * @param clearTo The presenter below the next location after clearing history
     * @return This instance
     */
    public Forwarder clearTo(@NotNull Class<? extends Controller> clearTo) {
        clearHistory = true;
        clearToLocationId = clearTo.getName();
        return this;
    }

    /**
     * Clear all history.
     *
     * <p>Note that, if this method is called, {@link #clearTo(Class)} will have no effect</p>
     * @return This instance
     */
    public Forwarder clearAll() {
        clearHistory = true;
        clearToLocationId = null;
        return this;
    }
}
