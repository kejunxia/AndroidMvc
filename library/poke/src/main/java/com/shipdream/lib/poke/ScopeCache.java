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

package com.shipdream.lib.poke;

import com.shipdream.lib.poke.exception.ProvideException;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The instances controls how the provider associated should generate new instances.
 */
public class ScopeCache {
    protected Map<String, Object> instances = new HashMap<>();

    @SuppressWarnings("unchecked")
    <T> T get(Provider<T> provider) throws ProvideException {
        String key = PokeHelper.makeProviderKey(provider.type(), provider.getQualifier());
        T instance = (T) instances.get(key);
        if (instance == null) {
            instance = provider.createInstance();
            if(instance == null) {
                String qualifierName = (provider.getQualifier() == null) ? "null" : provider.getQualifier().getClass().getName();
                throw new ProvideException(String.format("Provider (type: %s, qualifier: " +
                                "%s) should not provide NULL as instance",
                        provider.type().getName(), qualifierName));
            }
            instances.put(key, instance);

            provider.newlyCreatedInstance = instance;
        }

        return instance;
    }

    @SuppressWarnings("unchecked")
    /**
     * Get the cached instance
     * @param cacheKey result of {@link PokeHelper#makeProviderKey(Class, Annotation)}
     */
    Object findInstance(String cacheKey) {
        return instances.get(cacheKey);
    }

    @SuppressWarnings("unchecked")
    <T> T findInstance(Class<T> type, Annotation qualifier) {
        return (T) this.findInstance(PokeHelper.makeProviderKey(type, qualifier));
    }

    @SuppressWarnings("unchecked")
    /**
     * Remove the cached instance from the scope instances.
     * @param cacheKey result of {@link PokeHelper#makeProviderKey(Class, Annotation)}
     */
    <T> void removeInstance(String key) {
        instances.remove(key);
    }

    /**
     * Gets all cached instances this instances still manages
     * @return The collection of cached times
     */
    public Collection<Object> getCachedInstances() {
        return instances.values();
    }
}
