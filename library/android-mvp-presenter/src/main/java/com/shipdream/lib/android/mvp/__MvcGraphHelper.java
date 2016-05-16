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

import com.shipdream.lib.poke.ScopeCache;

import java.util.Collection;

/**
 * Helper class to work with MvcGraph. Internal use only. Don't use it in your app.
 */
public class __MvcGraphHelper {
    /**
     * Internal use. Gets all cached items this cache still manages
     * @return The collection of cached times
     */
    public static Collection<ScopeCache.CachedItem> getAllCachedInstances(MvcGraph mvcGraph) {
        return mvcGraph.singletonScopeCache.getCachedItems();
    }

}
