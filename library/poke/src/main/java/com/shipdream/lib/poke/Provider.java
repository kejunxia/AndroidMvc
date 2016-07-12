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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Provider controls the injection type mapping as well as the scope by associated
 * {@link ScopeCache}.
 */
public abstract class Provider<T> {
    /**
     * Listener monitoring when the provider is instantiating a new instance.
     */
    public interface CreationListener<T> {
        /**
         * Called when a new instance is constructed by the provider
         *
         * @param provider The provider used to provide the injecting instance
         * @param instance The instance. Its own injectable members should have been injected recursively as well.
         */
        void onCreated(Provider<T> provider, T instance);
    }

    /**
     * Listener monitoring when the instance provided by the provider is freed. It happens either
     * <ul>
     *     <li>The provider doesn't have a scope instances and a provided instance is dereferenced</li>
     *     <li>The provider has a scope instances and the provider is dereferenced with 0 reference count</li>
     * </ul>
     */
    public interface DisposeListener {
        /**
         * Called when either
         * <ul>
         *     <li>The provider doesn't have a scope instances and a provided instance is dereferenced</li>
         *     <li>The provider has a scope instances and the provider is dereferenced with 0 reference count</li>
         * </ul>
         *
         * @param provider The provider used to provide the injecting instance
         * @param instance The instance. Its own injectable members should have been injected recursively as well.
         */
        <T> void onDisposed(Provider<T> provider, T instance);
    }

    /**
     * Listener monitoring when the provider is referenced.
     */
    public interface ReferencedListener<T> {
        /**
         * Called provider is first time used to inject its content. The call is guaranteed to be
         * invoked after all injectable fields of its content is fully injected.
         *
         * @param provider The provider used to provide the injecting instance
         * @param instance The instance. Its own injectable members should have been injected recursively as well.
         */
        void onReferenced(Provider<T> provider, T instance);
    }

    /**
     * Listener will be called when the given provider is dereferenced.
     */
    public interface DereferenceListener {
        /**
         * listener to be invoked when when the given provider is not referenced by any objects.
         *
         * @param provider The provider whose content is not referenced by any objects.
         * @param instance The instance that is being dereferrenced
         */
        <T> void onDereferenced(Provider<T> provider, T instance);
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
     * @param scopeCache the instances for the scope
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
     * Get the scope instances. If this provider is not attached to any component. It returns this
     * provider's own scope instances otherwise the component's scope instances.
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

        if (totalRefCount == 0) {
            freeCache();
        }
    }

    private void freeCache() {
        ScopeCache cache = getScopeCache();
        if (cache != null) {
            String key = PokeHelper.makeProviderKey(type, qualifier);
            Object instance = cache.findInstance(key);
            if (instance != null) {
                cache.removeInstance(key);
            }
        }
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

    public int getReferenceCount() {
        return totalRefCount;
    }

    /**
     * The listeners when the instance is injected.
     */
    private List<CreationListener<T>> creationListeners;

    /**
     * @return Instantiation listeners if there are registered listeners. Null may be returned if
     * nothing is registered ever.
     */
    public List<CreationListener<T>> getCreationListeners() {
        return creationListeners;
    }

    /**
     * The listeners when the instance is referenced.
     */
    private List<ReferencedListener<T>> referencedListeners;

    /**
     * @return Referenced listeners if there are registered listeners. Null may be returned if
     * nothing is registered ever.
     */
    public List<ReferencedListener<T>> getReferencedListeners() {
        return referencedListeners;
    }

    /**
     * Get qualifier of the provider
     * @return The qualifier
     */
    public Annotation getQualifier() {
        return this.qualifier;
    }

    /**
     * Get the cached instance of this provider when there is a instances associated with this provider
     * and the instance is cached already. Note that, the method will NOT increase reference count of
     * this provider
     * @return The cached instance of this provider if there is a instances associated with this provider
     * and the instance is cached already, otherwise null will be returned
     */
    public T getCachedInstance() {
        ScopeCache cache = getScopeCache();

        if (cache != null) {
            String key = PokeHelper.makeProviderKey(type, qualifier);
            Object instance = cache.findInstance(key);
            if(instance != null) {
                return (T) instance;
            }
        }
        return null;
    }

    public void registerCreationListener(CreationListener<T> listener) {
        if(creationListeners == null) {
            creationListeners = new CopyOnWriteArrayList<>();
        }
        creationListeners.add(listener);
    }

    public void unregisterCreationListener(CreationListener<T> listener) {
        if (creationListeners != null) {
            creationListeners.remove(listener);
            if (creationListeners.isEmpty()) {
                creationListeners = null;
            }
        }
    }

    public void clearCreationListeners() {
        if (creationListeners != null) {
            creationListeners.clear();
            creationListeners = null;
        }
    }

    public void registerOnReferencedListener(ReferencedListener<T> listener) {
        if(referencedListeners == null) {
            referencedListeners = new CopyOnWriteArrayList<>();
        }
        referencedListeners.add(listener);
    }

    public void unregisterOnReferencedListener(ReferencedListener<T> listener) {
        if (referencedListeners != null) {
            referencedListeners.remove(listener);
            if (referencedListeners.isEmpty()) {
                referencedListeners = null;
            }
        }
    }

    public void clearOnReferencedListener() {
        if (referencedListeners != null) {
            referencedListeners.clear();
            referencedListeners = null;
        }
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

            newlyCreatedInstance = impl;

            return impl;
        } else {
            return cache.get(this);
        }
    }

    /**
     * Delay notifying instantiation listeners since they need to be full
     * injected if the instance has injectable fields
     */
    T newlyCreatedInstance = null;
    private void notifyInstanceCreationWhenNeeded() {
        if (newlyCreatedInstance != null && creationListeners != null) {
            for (CreationListener l : creationListeners) {
                l.onCreated(this, newlyCreatedInstance);
            }
        }
        newlyCreatedInstance = null;
    }

    /**
     * Notify the instance with the given type is <b>FULLY</b> injected which means all of its
     * nested injectable fields are injected and ready <b>RECURSIVELY</b>.
     * <p>This method should get called every time the instance is injected, no matter if it's a
     * newly created instance or it's a reused cached instance.</p>
     *
     * @param instance The instance. Its own injectable members should have been injected recursively as well.
     */
    void notifyReferenced(Provider provider, T instance) {
        notifyInstanceCreationWhenNeeded();

        if (referencedListeners != null) {
            int len = referencedListeners.size();
            for(int i = 0; i < len; i++) {
                referencedListeners.get(i).onReferenced(provider, instance);
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
