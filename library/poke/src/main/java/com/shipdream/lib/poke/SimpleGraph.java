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
import com.shipdream.lib.poke.exception.ProviderConflictException;

import javax.inject.Qualifier;

/**
 * Simple graph can register and unregister injection bindings
 */
public class SimpleGraph extends Graph {
    private final ProviderFinderByRegistry providerFinder;

    /**
     * Construct a default graph with its own {@link ProviderFinderByRegistry}
     */
    public SimpleGraph() {
        this(new ProviderFinderByRegistry());
    }

    /**
     * Construct a default graph with the given {@link ProviderFinderByRegistry}
     * @param providerFinder The specific {@link ProviderFinderByRegistry}
     */
    public SimpleGraph(ProviderFinderByRegistry providerFinder) {
        this.providerFinder = providerFinder;
        addProviderFinders(providerFinder);
    }

    /**
     * Unregister binding to the given type and the qualifier annotated to the given
     * implementation class. If there is an overridden type registered already, only unregister the
     * overridden binding. The original one will be unregistered if the this method is called
     * again against to the type and qualifier associated with the provider.
     *
     * @param type The type of the implementation to unregister
     * @param implementationClassName The name of the class annotated with the qualifier
     *
     * @throws ClassNotFoundException Thrown when the class of the given class name is not found
     */
    public <T> void unregister(Class<T> type, String implementationClassName)
            throws ClassNotFoundException {
        providerFinder.unregister(type, implementationClassName);
    }

    /**
     * Register a {@link Provider}. When allowOverride = false, it allows to register overriding
     * binding against the same type and {@link Qualifier} and <b>last wins</b>, otherwise
     * {@link ProviderConflictException} will be thrown.
     *
     * @param provider The provider
     * @param allowOverride Indicates whether allowing overriding registration
     *
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     * type and qualifier.
     */
    public void register(Provider provider, boolean allowOverride) throws ProviderConflictException {
        providerFinder.register(provider, allowOverride);
    }

    /**
     * Register binding for type by full class name with given scope cache.
     * {@link Qualifier} of the class will be taken into account. Registering multiple
     * bindings against the same type and qualifier will throw {@link ProviderConflictException}.
     *
     * @param type The type
     * @param implementationClassName The full name of the implementation class
     * @param scopeCache The scope cache
     *
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     * type and qualifier.
     * @throws ClassNotFoundException Thrown when the class the class name pointing to is not found.
     */
    public <T> void register(Class<T> type, String implementationClassName, ScopeCache scopeCache)
            throws ProviderConflictException, ClassNotFoundException {
        providerFinder.register(type, implementationClassName, scopeCache);
    }

    /**
     * Unregister binding to the given type and the qualifier annotated to the given
     * implementation class. If there is an overridden type registered already, only unregister the
     * overridden binding. The original one will be unregistered if the this method is called
     * again against to the type and qualifier associated with the provider.
     *  @param type The type of the implementation to unregister
     * @param implementationClass The class annotated with the qualifier
     */
    public <T, S extends T> void unregister(Class<T> type, Class<S> implementationClass) {
        providerFinder.unregister(type, implementationClass);
    }

    /**
     * Register a {@link Provider}. Registering multiple bindings against the same type and
     * qualifier will throw {@link ProviderConflictException}.
     *
     * @param provider The provider
     *
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     * type and qualifier.
     */
    public void register(Provider provider) throws ProviderConflictException {
        providerFinder.register(provider);
    }

    /**
     * Register binding for type by full class name without scope cache. {@link Qualifier}
     * of the class will be taken into account. Registering multiple bindings against the
     * same type and qualifier will throw {@link ProviderConflictException}.
     *
     * @param type The type
     * @param implementationClassName The full name of the implementation class
     *
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     * type and qualifier.
     * @throws ClassNotFoundException Thrown when the class the class name pointing to is not found.
     */
    public <T> void register(Class<T> type, String implementationClassName)
            throws ProviderConflictException, ClassNotFoundException {
        providerFinder.register(type, implementationClassName);
    }

    /**
     * Unregister component where methods annotated by {@link Provides} will be registered as
     * injection providers.
     *
     * @param component The component contains methods annotated by {@link Provides}
     */
    public void unregister(Component component) {
        providerFinder.unregister(component);
    }

    /**
     * Register binding for type by class type without scope cache. {@link Qualifier} of the
     * class will be taken into account. Registering multiple bindings against the same type
     * and qualifier will throw {@link ProviderConflictException}.
     *
     * @param type The type
     * @param implementationClass The class type of the implementation
     *
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     * type and qualifier.
     */
    public <T, S extends T> void register(Class<T> type, Class<S> implementationClass)
            throws ProviderConflictException {
        providerFinder.register(type, implementationClass);
    }

    /**
     * Register component where methods annotated by {@link Provides} will be registered as
     * injection providers. Registering multiple bindings against the same type and qualifier
     * will throw {@link ProviderConflictException}.
     *
     * @param component The components contains methods annotated by {@link Provides}
     *
     * @throws ProvideException Thrown when exception occurs during providers creating instances
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     * type and qualifier.
     */
    public void register(Component component) throws ProvideException, ProviderConflictException {
        providerFinder.register(component);
    }

    /**
     * Register binding for type by class type. {@link Qualifier} of the class will be
     * taken into account. When allowOverride = false, it allows to register overriding
     * binding against the same type and {@link Qualifier} and <b>last wins</b>, otherwise
     * {@link ProviderConflictException} will be thrown.
     *
     * @param type The type
     * @param implementationClass The class type of the implementation
     * @param scopeCache The scope cache
     * @param allowOverride Indicates whether allowing overriding registration
     *
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     * type and qualifier.
     */
    public <T, S extends T> void register(Class<T> type, Class<S> implementationClass,
                                          ScopeCache scopeCache, boolean allowOverride)
            throws ProviderConflictException {
        providerFinder.register(type, implementationClass, scopeCache, allowOverride);
    }

    /**
     * Register component where methods annotated by {@link Provides} will be registered as
     * injection providers. When allowOverride = false, it allows to register overriding
     * binding against the same type and {@link Qualifier} and <b>last wins</b>, otherwise
     * {@link ProviderConflictException} will be thrown.
     * @param component The components contains methods annotated by {@link Provides}
     * @param allowOverride Indicates whether allowing overriding registration
     *
     * @throws ProvideException Thrown when exception occurs during providers creating instances
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     * type and qualifier.
     */
    public void register(Component component, boolean allowOverride) throws ProvideException,
            ProviderConflictException {
        providerFinder.register(component, allowOverride);
    }

    /**
     * Register binding for type by class type with given scope cache. {@link Qualifier} of
     * the class will be taken into account. Registering multiple bindings against the same
     * type and qualifier will throw {@link ProviderConflictException}.
     *
     * @param type The type
     * @param implementationClass The class type of the implementation
     * @param scopeCache The scope cache
     *
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     * type and qualifier.
     */
    public <T, S extends T> void register(Class<T> type, Class<S> implementationClass,
                                          ScopeCache scopeCache) throws ProviderConflictException {
        providerFinder.register(type, implementationClass, scopeCache);
    }

    /**
     * Unregister provider. If there is an overridden type registered already, only unregister the
     * overridden binding. The original one will be unregistered if the this method is called
     * again against to the type and qualifier associated with the provider.
     * @param provider The provider that has the type and qualifier to unregister against
     */
    public void unregister(Provider provider) {
        providerFinder.unregister(provider);
    }

    /**
     * Register binding for type by full class name  without scope cache.
     * {@link Qualifier} of the class will be taken into account. When allowOverride = false, it
     * allows to register overriding binding against the same type and {@link Qualifier} and
     * <b>last wins</b>, otherwise {@link ProviderConflictException} will be thrown.
     *
     * @param type The type
     * @param implementationClassName The full name of the implementation class
     * @param scopeCache The scope cache
     * @param allowOverride Indicates whether allowing overriding registration
     *
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     * type and qualifier.
     * @throws ClassNotFoundException Thrown when the class the class name pointing to is not found.
     */
    public <T> void register(Class<T> type, String implementationClassName, ScopeCache scopeCache,
                             boolean allowOverride) throws ProviderConflictException, ClassNotFoundException {
        providerFinder.register(type, implementationClassName, scopeCache, allowOverride);
    }
}
