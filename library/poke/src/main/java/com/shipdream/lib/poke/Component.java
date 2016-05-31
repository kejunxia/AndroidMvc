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

import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Qualifier;

/**
 * //TODO: document, component has a default scope cache
 * {@link Component} that registers providers manually.
 */
public class Component {
    //TODO: document
    public static class MismatchDetachException extends PokeException {
        public MismatchDetachException(String message) {
            super(message);
        }
    }

    //TODO document
    public static class AlreadyAttachedException extends PokeException {
        public AlreadyAttachedException(String message) {
            super(message);
        }
    }

    private final String name;
    final ScopeCache scopeCache;

    final Map<String, Component> componentLocator = new HashMap<>();
    final Map<String, Provider> providers = new HashMap<>();
    private Component parentComponent;

    public Component() {
        this(null, true);
    }

    public Component(String name) {
        this(name, true);
    }

    public Component(boolean enableCache) {
        this(null, enableCache);
    }

    public Component(String name, boolean enableCache) {
        if (enableCache) {
            scopeCache = new ScopeCache();
        } else {
            scopeCache = null;
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean hasCache() {
        return scopeCache != null;
    }

    public Component getParent() {
        return parentComponent;
    }

    /**
     * //TODO: document how component scope cache will override provider's
     * Register a {@link Provider}. When allowOverride = false, it allows to register overriding
     * binding against the same type and {@link Qualifier} and <b>last wins</b>, otherwise
     * {@link ProviderConflictException} will be thrown.
     *
     * @param provider The provider
     * @return this instance
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    public Component register(@NotNull Provider provider) throws ProviderConflictException {
        addProvider(provider);
        return this;
    }

    /**
     * Unregister provider. If there is an overridden type registered already, only unregister the
     * overridden binding. The original one will be unregistered if the this method is called
     * again against to the type and qualifier associated with the provider.
     *
     * @param provider The provider that has the type and qualifier to unregister against
     * @return this instance
     *
     * @throws ProviderMissingException Thrown when the provider with the given type and qualifier
     *                                  cannot be found under this component
     */
    public Component unregister(Provider provider) throws ProviderMissingException {
        return unregister(provider.type(), provider.getQualifier());
    }

    /**
     * Find the provider in this {@link Component} and its descents. If the provider is found,
     * detach it from its associated {@link Component}. After this point, the provider will use its
     * own scope cache.
     * @param type The type of the provider
     * @param qualifier The qualifier of the provider
     * @return this instance
     *
     * @throws ProviderMissingException Thrown when the provider with the given type and qualifier
     *                                  cannot be found under this component
     */
    public <T> Component unregister(Class<T> type, Annotation qualifier) throws ProviderMissingException {
        //Detach corresponding provider from  it's component
        Provider<T> provider = findProvider(type, qualifier);
        provider.setComponent(null);

        String key = PokeHelper.makeProviderKey(type, qualifier);
        Component targetComponent = getRootComponent().componentLocator.get(key);

        targetComponent.providers.remove(key);
        if (targetComponent.scopeCache != null) {
            targetComponent.scopeCache.removeCache(type, qualifier);
        }

        Component root = getRootComponent();
        //Only remove it to root component's locator
        root.componentLocator.put(key, this);

        return this;
    }

    /**
     * Register component where methods annotated by {@link Provides} will be registered as
     * injection providers. When allowOverride = false, it allows to register overriding
     * binding against the same type and {@link Qualifier} and <b>last wins</b>, otherwise
     * {@link ProviderConflictException} will be thrown.
     *
     * @param providerHolder The object with methods marked by {@link Provides} to provide injectable
     *                       instances
     * @return this instance
     * @throws ProvideException          Thrown when exception occurs during providers creating instances
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    public Component register(Object providerHolder) throws ProvideException,
            ProviderConflictException {
        Method[] methods = providerHolder.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Provides.class)) {
                registerProvides(providerHolder, method);
            }
        }
        return this;
    }

    /**
     * Unregister component where methods annotated by {@link Provides} will be registered as
     * injection providers.
     *
     * @param providerHolder The object with methods marked by {@link Provides} to provide injectable
     *                       instances
     * @return this instance
     *
     * @throws ProviderMissingException Thrown when the any provider in the provider holder with
     *  the given type and qualifier cannot be found under this component
     */
    public Component unregister(Object providerHolder) throws ProviderMissingException {
        Method[] methods = providerHolder.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Provides.class)) {
                Class<?> returnType = method.getReturnType();
                if (returnType != void.class) {
                    Annotation qualifier = null;
                    Annotation[] annotations = method.getAnnotations();
                    for (Annotation a : annotations) {
                        if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
                            qualifier = a;
                            break;
                        }
                    }

                    unregister(returnType, qualifier);
                }
            }
        }
        return this;
    }

    public void attach(@NotNull Component childComponent) throws AlreadyAttachedException, ProviderConflictException {
        if (childComponent.parentComponent != null) {
            String msg = String.format("The attaching component(%s) has a parent already. Remove its parent before attaching it",
                    childComponent.name == null ? "unnamed" : childComponent.name);
            throw new AlreadyAttachedException(msg);
        }

        //Merge the child component locator to the root component
        Component root = getRootComponent();

        Set<String> addedKeys = new HashSet<>();
        for (Map.Entry<String, Component> entry : childComponent.componentLocator.entrySet()) {
            String key = entry.getKey();

            if (root.componentLocator.containsKey(key)) {
                for (String k : addedKeys) {
                    root.componentLocator.remove(k);
                }

                throw new ProviderConflictException(
                        String.format("Type(%s) in the adding child component(%s) has been added " +
                                "to rootComponent(%s) or its attached child components.",
                                key, childComponent.getComponentId(), root.getComponentId()));
            }

            root.componentLocator.put(key, entry.getValue());
            addedKeys.add(key);
        }

        //Update tree nodes
        childComponent.parentComponent = this;
    }

    private String getComponentId() {
        if (name != null) {
            return name;
        } else {
            if (providers.size() == 0) {
                return "Unnamed empty Component";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Unnamed Component containing type(s):");
            int max = 5;
            int count = 0;
            for (Provider provider : providers.values()) {
                if (count > 0) {
                    sb.append(", ");
                }
                sb.append(provider.type().getSimpleName());
                count ++;

                if (count > max) {
                    break;
                }
            }

            return sb.toString();
        }
    }

    public void detach(@NotNull Component childComponent) throws MismatchDetachException {
        if (childComponent.parentComponent != this) {
            String msg = String.format("The child component(%s) doesn't belong to component(%s)",
                    childComponent.name == null ? "unnamed" : childComponent.name,
                    name == null ? "unnamed" : name);
            throw new MismatchDetachException(msg);
        }

        //Update tree nodes
        childComponent.parentComponent = null;

        //Disband the child component locator from the root component and return the keys to child
        //component itself
        Component root = getRootComponent();
        for (String key : childComponent.providers.keySet()) {
            root.componentLocator.remove(key);
            childComponent.componentLocator.put(key, childComponent);
        }
    }

    private Component getRootComponent() {
        Component root = this;
        while (root.parentComponent != null) {
            root = root.parentComponent;
        }
        return root;
    }

    protected <T> Provider<T> findProvider(Class<T> type, Annotation qualifier) throws ProviderMissingException {
        String key = PokeHelper.makeProviderKey(type, qualifier);
        Component targetComponent = getRootComponent().componentLocator.get(key);

        Provider<T> provider = null;
        if (targetComponent != null) {
            provider = targetComponent.providers.get(key);
        }
        if (provider == null) {
            throw new ProviderMissingException(type, qualifier);
        } else {
            return provider;
        }
    }

    private <T> void addProvider(@NotNull Provider<T> provider)
            throws ProviderConflictException {
        Class<T> type = provider.type();
        Annotation qualifier = provider.getQualifier();
        String key = PokeHelper.makeProviderKey(type, qualifier);

        addNewKeyToComponent(key, this);

        provider.setComponent(this);
        providers.put(key, provider);
    }

    private void addNewKeyToComponent(String key, Component component) throws ProviderConflictException {
        Component root = getRootComponent();

        if (componentLocator.keySet().contains(key)) {
            String msg = String.format("Type %s has already been registered " +
                    "in this component(%s).", key, name == null ? "unnamed" : name);
            throw new ProviderConflictException(msg);
        }

        if (root != this && root.componentLocator.keySet().contains(key)) {
            String msg = String.format("Type %s has already been registered " +
                    "in root component(%s).", key, name == null ? "unnamed" : name);
            throw new ProviderConflictException(msg);
        }

        //Only put it to root component's locator
        root.componentLocator.put(key, component);
    }

    private void registerProvides(final Object providerHolder, final Method method)
            throws ProvideException, ProviderConflictException {
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class) {
            throw new ProvideException(String.format("Provides method %s must not return void.",
                    method.getName()));
        } else {
            Annotation[] annotations = method.getAnnotations();
            Annotation qualifier = null;
            for (Annotation a : annotations) {
                Class<? extends Annotation> annotationType = a.annotationType();
                if (annotationType.isAnnotationPresent(Qualifier.class)) {
                    if (qualifier != null) {
                        throw new ProvideException("Only one Qualifier is supported for Provide method. " +
                                String.format("Found multiple qualifier %s and %s for method %s",
                                        qualifier.getClass().getName(), a.getClass().getName(),
                                        method.getName()));
                    }
                    qualifier = a;
                }
            }

            Provider provider = new MethodProvider(returnType, qualifier, scopeCache, providerHolder, method);
            register(provider);
        }
    }

    static class MethodProvider extends Provider {
        private final Object providerHolder;
        private final Method method;

        @SuppressWarnings("unchecked")
        MethodProvider(Class type, Annotation qualifier, ScopeCache scopeCache,
                       Object providerHolder, Method method) {
            super(type, qualifier, scopeCache);
            this.providerHolder = providerHolder;
            this.method = method;
        }

        @Override
        protected Object createInstance() throws ProvideException {
            try {
                boolean accessible = method.isAccessible();
                if (!accessible) {
                    method.setAccessible(true);
                }

                Object obj = method.invoke(providerHolder);

                method.setAccessible(accessible);

                return obj;
            } catch (IllegalAccessException e) {
                throw new ProvideException(String.format("Provides method %s must " +
                        "be accessible.", method.getName()), e);  // $COVERAGE-IGNORE$
            } catch (InvocationTargetException e) {
                throw new ProvideException(String.format("Provides method %s is not able " +
                        "to be invoked against %s.", method.getName(), providerHolder.getClass().getName()), e);  // $COVERAGE-IGNORE$
            }
        }
    }
}
