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
    private final Annotation qualifier;
    //The component the provider is attached to
    private Component component;
    private ScopeCache scopeCache;

    Map<Object, Map<String, Integer>> owners = new HashMap<>();
    private int totalRefCount = 0;

    /**
     * Construct an unscoped and unqualified provider
     * @param type The type of the instance the provider is providing
     */
    public Provider(Class<T> type) {
        this(type, null, null);
    }

    /**
     * Construct an unscoped and qualified provider
     * @param type The type of the instance the provider is providing
     * @param qualifier Qualifier
     */
    public Provider(Class<T> type, Annotation qualifier) {
        this.type = type;
        this.qualifier = qualifier;
        this.scopeCache = null;
    }

    /**
     * Construct a scoped and unqualified provider
     * @param type The type of the instance the provider is providing
     * @param scopeCache the cache for the scope
     */
    public Provider(Class<T> type, ScopeCache scopeCache) {
        this(type, null, scopeCache);
    }

    /**
     * Construct a scoped and qualified provider
     * @param type The type of the instance the provider is providing
     * @param qualifier Qualifier
     * @param scopeCache ScopeCache
     */
    public Provider(Class<T> type, Annotation qualifier, ScopeCache scopeCache) {
        this.type = type;
        this.qualifier = qualifier;
        this.scopeCache = scopeCache;
    }

    Component getComponent() {
        return component;
    }

    void setComponent(Component component) {
        this.component = component;
    }

    /**
     * Get the scope cache. If this provider is not attached to any component. It returns this
     * provider's own scope cache otherwise the component's scope cache.
     * @return
     */
    ScopeCache getScopeCache() {
        if (component == null) {
            return scopeCache;
        } else {
            return component.scopeCache;
        }
    }

    int getReferenceCount(Object owner, Field field) {
        Map<String, Integer> fields = owners.get(owner);
        if (fields != null) {
            Integer count = fields.get(field.toGenericString());
            if(count != null) {
                return count;
            }

        }
        return 0;
    }

    /**
     * Increase reference count.
     */
    public void retain() {
        totalRefCount++;
    }

    /**
     * Retain an instance injected as a field of an object
     * @param owner The owner of the field
     * @param field The field
     */
    void retain(Object owner, Field field) {
        retain();
        Map<String, Integer> fields = owners.get(owner);
        if (fields == null) {
            fields = new HashMap<>();
            owners.put(owner, fields);
        }

        Integer count = fields.get(field.toGenericString());
        if (count == null) {
            fields.put(field.toGenericString(), 1);
        } else {
            count++;
            fields.put(field.toGenericString(), count);
        }
    }

    /**
     * Decrease reference count.
     */
    public void release() {
        totalRefCount--;
    }

    /**
     * Release an instance injected as a field of an object
     * @param owner The owner of the field
     * @param field The field
     */
    void release(Object owner, Field field) {
        Map<String, Integer> fields = owners.get(owner);
        if(fields != null) {
            release();

            Integer count = fields.get(field.toGenericString());
            if(--count > 0) {
                fields.put(field.toGenericString(), count);
            } else {
                fields.remove(field.toGenericString());
            }
        }

        if(fields != null && fields.isEmpty()) {
            owners.remove(owner);
        }
    }

    void freeCache() {
        ScopeCache cache = getScopeCache();
        if (cache != null) {
            ScopeCache.CachedItem cachedItem = cache.findCacheItem(type, qualifier);
            if (cachedItem != null) {
                cache.removeCache(cachedItem.provider.type, cachedItem.provider.qualifier);
            }
        }
    }

    public int getReferenceCount() {
        return totalRefCount;
    }

    /**
     * The listeners when the instance is injected.
     */
    private List<OnInjectedListener<T>> onInjectedListeners;

    /**
     * Get qualifier of the provider
     * @return The qualifier
     */
    public Annotation getQualifier() {
        return this.qualifier;
    }

    /**
     * Get the cached instance of this provider if there is a cache associated with this provider
     * and the instance is cached already. Note that, the method will NOT increase reference count of
     * this provider
     * @return The cached instance of this provider if there is a cache associated with this provider
     * and the instance is cached already, otherwise null will be returned
     */
    public T findCachedInstance() {
        ScopeCache cache = getScopeCache();
        if (cache != null) {
            ScopeCache.CachedItem<T> cachedItem = cache.findCacheItem(type, qualifier);
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
        onInjectedListeners.remove(listener);
    }

    /**
     * Get an instance of the type the provider is providing. When there is a {@link ScopeCache}
     * associated to it the provider will try to use the cached instance when applicable otherwise
     * always generates a new instance.
     * @return The instance being created or cached
     * @throws ProvideException Exception thrown during constructing the object
     */
    final T get() throws ProvideException {
        ScopeCache cache = getScopeCache();
        if(cache == null) {
            T impl = createInstance();
            if (impl == null) {
                String qualifierName = (qualifier == null) ? "null" : qualifier.getClass().getName();
                throw new ProvideException(String.format("Provider (type: %s, qualifier: " +
                        "%s) should not provide NULL as instance", type.getName(), qualifierName));
            }

            return impl;
        } else {
            return cache.get(this);
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
