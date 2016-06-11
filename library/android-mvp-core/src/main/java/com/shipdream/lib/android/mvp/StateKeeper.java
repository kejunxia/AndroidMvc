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

package com.shipdream.lib.android.mvp;

import com.shipdream.lib.poke.Provider;

/**
 * StateKeeper saves and restores state of providers. If provider
 */
public interface StateKeeper {
    /**
     * Saves the provider's cached instance into this {@link StateKeeper}
     * @param provider The provider whose cached instance will be saved
     */
    void saveState(Provider provider);

    /**
     * Retrieves the provider's cached instanced previously saved
     * @return null if the provider's cached instance
     */
    <T> T retrieveInstance(String providerTypeKey);
}
