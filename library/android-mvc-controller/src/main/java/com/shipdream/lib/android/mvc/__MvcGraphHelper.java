package com.shipdream.lib.android.mvc;

import com.shipdream.lib.poke.Provider;
import com.shipdream.lib.poke.ScopeCache;

import java.util.Collection;

/**
 * Helper class to work with MvcGraph. Internal use only. Don't use it in your app.
 */
public class __MvcGraphHelper {
    /**
     * Internal use. Don't call it in app directly.
     */
    public static void retainCachedObjectsBeforeNavigation(MvcGraph mvcGraph) {
        mvcGraph.cachedInstancesBeforeNavigation.clear();
        //Retain all cached items before navigation.
        Collection<ScopeCache.CachedItem> cachedItems = mvcGraph.singletonScopeCache.getCachedItems();
        for (ScopeCache.CachedItem cachedItem : cachedItems) {
            Provider provider = cachedItem.getProvider();
            if (provider != null) {
                mvcGraph.cachedInstancesBeforeNavigation.add(provider);
                provider.retain();
            }
        }
    }

    /**
     * Internal use. Don't call it in app directly.
     */
    public static void releaseCachedItemsAfterNavigation(MvcGraph mvcGraph) {
        //Release all cached items after the fragment navigated to is ready to show.
        for (Provider provider : mvcGraph.cachedInstancesBeforeNavigation) {
            if (provider != null) {
                provider.release();
            }
        }
        mvcGraph.cachedInstancesBeforeNavigation.clear();
    }

    /**
     * Internal use. Gets all cached items this cache still manages
     * @return The collection of cached times
     */
    public static Collection<ScopeCache.CachedItem> getAllCachedInstances(MvcGraph mvcGraph) {
        return mvcGraph.singletonScopeCache.getCachedItems();
    }

}
