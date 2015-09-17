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

package com.shipdream.lib.poke;

import com.shipdream.lib.poke.exception.ProvideException;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * The cache controls how the provider associated should generate new instances.
 */
public class ScopeCache {

    public static class CachedItem<T> {
        Class<T> type;
        T instance;
        Annotation qualifier;
    }

    protected Map<String, CachedItem> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    <T> T get(Provider<T> provider) throws ProvideException {
        String key = PokeHelper.makeProviderKey(provider.type(), provider.getQualifier());
        CachedItem<T> item = cache.get(key);
        if (item == null) {
            item = new CachedItem<>();
            item.type = provider.type();
            item.instance = provider.createInstance();
            if(item.instance == null) {
                String qualifierName = (provider.getQualifier() == null) ? "null" : provider.getQualifier().getClass().getName();
                throw new ProvideException(String.format("Provider (type: %s, qualifier: " +
                                "%s) should not provide NULL as instance",
                        provider.type().getName(), qualifierName));
            }
            item.qualifier = provider.getQualifier();
            cache.put(key, item);
        }

        return item.instance;
    }

    @SuppressWarnings("unchecked")
    <T> CachedItem<T> findCacheItem(Class<T> type, Annotation qualifier) {
        return cache.get(PokeHelper.makeProviderKey(type, qualifier));
    }

    @SuppressWarnings("unchecked")
    /**
     * Remove the cached instance from the scope cache.
     * @param type The type of the provider is providing
     * @param qualifier The annotation of the qualifier. When null is given, this method will
     *                  specifically look for provider without qualifier
     */
    public <T> void removeCache(Class<T> type, Annotation qualifier) {
        cache.remove(PokeHelper.makeProviderKey(type, qualifier));
    }

}
