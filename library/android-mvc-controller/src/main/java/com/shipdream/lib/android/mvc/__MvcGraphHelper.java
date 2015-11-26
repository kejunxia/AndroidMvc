package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.poke.Provider;
import com.shipdream.lib.poke.ScopeCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to work with MvcGraph. Internal use only. Don't use it in your app.
 */
public class __MvcGraphHelper {
    private static Map<NavigationController.EventC2V.OnLocationForward, Collection<Provider>> retainedProviders = new HashMap<>();

    /**
     * Internal use. Don't call it in app directly.
     */
    public static void retainCachedObjectsBeforeNavigation(
            NavigationController.EventC2V.OnLocationForward navigationEvent, MvcGraph mvcGraph) {
        //Retain all cached items before navigation.
        Collection<ScopeCache.CachedItem> cachedItems = mvcGraph.singletonScopeCache.getCachedItems();

        List<Provider> providers = new ArrayList<>();
        retainedProviders.put(navigationEvent, providers);
        for (ScopeCache.CachedItem cachedItem : cachedItems) {
            Provider provider = cachedItem.getProvider();
            if (provider != null) {
                providers.add(provider);
                provider.retain();
            }
        }
    }

    /**
     * Internal use. Don't call it in app directly.
     */
    public static void releaseCachedItemsAfterNavigation(
            NavigationController.EventC2V.OnLocationForward navigationEvent, MvcGraph mvcGraph) {

        //Release all cached items after the fragment navigated to is ready to show.
        Collection<Provider> providers = retainedProviders.get(navigationEvent);
        for (Provider provider : providers) {
            if (provider != null) {
                provider.release();
            }
        }
        providers.clear();
        retainedProviders.remove(navigationEvent);
    }

    /**
     * Internal use. Gets all cached items this cache still manages
     * @return The collection of cached times
     */
    public static Collection<ScopeCache.CachedItem> getAllCachedInstances(MvcGraph mvcGraph) {
        return mvcGraph.singletonScopeCache.getCachedItems();
    }

}
