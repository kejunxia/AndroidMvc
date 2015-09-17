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

import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provider controls the injection type mapping as well as the scope by associated
 * {@link ScopeCache}.
 */
public abstract class Provider<T> {
    /**
     * Listener monitoring when the provider is referenced for the first time
     */
    public interface OnInjectedListener<T> {
        /**
         * Called provider is first time used to inject its content. The call is guaranteed to be
         * invoked after all injectable fields of its content is fully injected.
         * @param object Who is fully injected
         */
        void onInjected(T object);
    }

    /**
     * Listener will be called when the given provider is not referenced by any objects. The
     * listener will be called before its cached instance is freed if there is a cache associated.
     */
    public interface OnFreedListener {
        /**
         * listener to be invoked when when the given provider is not referenced by any objects.
         *
         * @param provider  The provider whose content is not referenced by any objects.
         */
        void onFreed(Provider provider);
    }

    private final Class<T> type;
    ScopeCache scopeCache;
    private Annotation qualifier;

    private Map<Object, Map<String, Integer>> owners = new HashMap<>();
    private int totalRefCount = 0;

    int getReferenceCount(Object owner, Field field) {
        Map<String, Integer> fields = owners.get(owner);
        if (fields != null) {
            Integer count = fields.get(field.getName());
            if(count != null) {
                return count;
            }

        }
        return 0;
    }

    void retain(Object owner, Field field) {
        totalRefCount++;
        Map<String, Integer> fields = owners.get(owner);
        if (fields == null) {
            fields = new HashMap<>();
            owners.put(owner, fields);
        }

        Integer count = fields.get(field.getName());
        if (count == null) {
            fields.put(field.getName(), 1);
        } else {
            count++;
            fields.put(field.getName(), count);
        }
    }

    void release(Object owner, Field field) {
        Map<String, Integer> fields = owners.get(owner);
        if(fields!= null) {
            totalRefCount--;

            Integer count = fields.get(field.getName());
            if(--count > 0) {
                fields.put(field.getName(), count);
            } else {
                fields.remove(field.getName());
            }
        }

        if(fields != null && fields.isEmpty()) {
            owners.remove(owner);
        }
    }

    void freeCache() {
        if (scopeCache != null) {
            ScopeCache.CachedItem cachedItem = scopeCache.findCacheItem(type, qualifier);
            if (cachedItem != null) {
                scopeCache.removeCache(cachedItem.type, cachedItem.qualifier);
            }
        }
    }

    int totalReference() {
        return totalRefCount;
    }

    /**
     * The listeners when the instance is injected.
     */
    private List<OnInjectedListener<T>> onInjectedListeners;
    /**
     * Hold the removing listeners as removal logic may be called in an iteration of
     * {@link #onInjectedListeners} which would cause a {@link java.util.ConcurrentModificationException}.
     */
    private List<OnInjectedListener<T>> removingOnInjectedListeners;

    /**
     * Construct non-overriding provider without qualifier
     * @param type The type of the instance the provider is providing
     */
    public Provider(Class<T> type) {
        this(type, null);
    }

    /**
     * Construct provider with given qualifier and indicates if it's a overriding provider.
     * Overriding provider always overrides the existing one and last one wins.
     * @param type The type of the instance the provider is providing
     * @param qualifier Qualifier
     */
    public Provider(Class<T> type, Annotation qualifier) {
        this.type = type;
        this.qualifier = qualifier;
    }

    /**
     * Get qualifier of the provider
     * @return The qualifier
     */
    public Annotation getQualifier() {
        return this.qualifier;
    }

    /**
     * Sets {@link ScopeCache}.
     * @param scopeCache Null when provider is not scoped otherwise the {@link ScopeCache}.
     */
    public void setScopeCache(ScopeCache scopeCache) {
        this.scopeCache = scopeCache;
    }

    /**
     * Get the cached instance of this provider if there is a cache associated with this provider
     * and the instance is cached already. Note that, the method will NOT increase reference count of
     * this provider
     * @return The cached instance of this provider if there is a cache associated with this provider
     * and the instance is cached already, otherwise null will be returned
     */
    public T findCachedInstance() {
        if (scopeCache != null) {
            ScopeCache.CachedItem<T> cachedItem = scopeCache.findCacheItem(type, qualifier);
            if(cachedItem != null) {
                return cachedItem.instance;
            }
        }
        return null;
    }

    /**
     * Register listener which will be called back when the instance is injected. It will called
     * until all injectable fields of the object are fully and recursively if needed injected.
     */
    public void registerOnInjectedListener(OnInjectedListener<T> listener) {
        if(onInjectedListeners == null) {
            onInjectedListeners = new ArrayList<>();
        }
        onInjectedListeners.add(listener);
    }

    /**
     * Unregister listener which will be called back when the instance is injected.
     */
    public void unregisterOnInjectedListener(OnInjectedListener<T> listener) {
        if(removingOnInjectedListeners == null) {
            removingOnInjectedListeners = new ArrayList<>();
        }
        removingOnInjectedListeners.add(listener);
    }

    /**
     * Get an instance of the type the provider is providing. When there is a {@link ScopeCache}
     * associated to it the provider will try to use the cached instance when applicable otherwise
     * always generates a new instance.
     * @return The instance being created or cached
     * @throws ProvideException Exception thrown during constructing the object
     * @throws CircularDependenciesException Exception thrown if nested injection has circular dependencies
     * @throws ProviderMissingException Exception thrown if nested injection misses dependencies
     */
    final T get() throws ProvideException {
        if(scopeCache == null) {
            T impl = createInstance();
            if(impl == null) {
                String qualifierName = (qualifier == null) ? "null" : qualifier.getClass().getName();
                throw new ProvideException(String.format("Provider (type: %s, qualifier: " +
                        "%s) should not provide NULL as instance", type.getName(), qualifierName));
            }

            return impl;
        } else {
            return scopeCache.get(this);
        }
    }

    /**
     * Notify the instance with the given type is <b>FULLY</b> injected which means all of its
     * nested injectable fields are injected and ready <b>RECURSIVELY</b>.
     * <p>This method should get called every time the instance is injected, no matter if it's a
     * newly created instance or it's a reused cached instance.</p>
     *
     * @param object Who just get fully injected
     */
    void notifyInjected(T object) {
        if (onInjectedListeners != null) {
            int len = onInjectedListeners.size();
            for(int i = 0; i < len; i++) {
                onInjectedListeners.get(i).onInjected(object);
            }
        }

        //Check the held listeners need to be removed. If exist remove them.
        if(removingOnInjectedListeners != null && onInjectedListeners != null) {
            int len = removingOnInjectedListeners.size();
            for(int i = 0; i < len; i++) {
                onInjectedListeners.remove(removingOnInjectedListeners.get(i));
            }

            if(removingOnInjectedListeners.isEmpty()) {
                removingOnInjectedListeners = null;
            }

            if(onInjectedListeners.isEmpty()) {
                onInjectedListeners = null;
            }
        }
    }

    /**
     * Type of the contract/interface
     * @return The type of the provider
     */
    public final Class<T> type() {
        return type;
    }

    /**
     * Override to implement how the provider instantiates a new instance
     * @return The newly created instance
     * @throws ProvideException
     */
    protected abstract T createInstance() throws ProvideException;
}
