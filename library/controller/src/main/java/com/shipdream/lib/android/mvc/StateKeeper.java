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

package com.shipdream.lib.android.mvc;

/**
 * StateKeeper can save state of objects implementing {@link StateManaged} into it. The state can
 * be got back from the keeper later on.
 */
public interface StateKeeper {
    /**
     * Save state into this {@link StateKeeper}
     * @param state The state to save
     * @param type The class type of the state
     * @param <T>
     */
    <T> void saveState(T state, Class<T> type);

    /**
     * Get saved state from the keeper
     * @param type The class type of the state
     * @param <T>
     * @return null if the state with the given type has not been saved or a null state was saved,
     * otherwise state saved previously
     */
    <T> T getState(Class<T> type);
}
