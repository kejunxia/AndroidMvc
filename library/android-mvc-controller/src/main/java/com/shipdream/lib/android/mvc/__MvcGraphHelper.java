package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.poke.Provider;
import com.shipdream.lib.poke.ScopeCache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
