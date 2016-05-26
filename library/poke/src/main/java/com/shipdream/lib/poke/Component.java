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

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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

    //TODO: document, use the scopeCache
    final ScopeCache scopeCache;

    final Map<String, Component> componentLocator = new HashMap<>();
    final Map<String, Provider> providers = new HashMap<>();
    private Component parentComponent;

    public Component() {
        this(null);
    }

    public Component(String scope) {
        if (scope != null) {
            this.scopeCache = new ScopeCache();
        } else {
            this.scopeCache = null;
        }
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
     */
    public Component unregister(Provider provider) {
        return unregister(provider.type(), provider.getQualifier());
    }

    public <T> Component unregister(Class<T> type, Annotation qualifier) {
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
     */
    public Component unregister(Object providerHolder) {
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


    public void attach(@NotNull Component childComponent) throws AlreadyAttachedException {
        if (childComponent.parentComponent != null) {
            throw new AlreadyAttachedException("The component being added has a parent already. Remove " +
                    "it from its parent before attaching it to another component");
        }

        //Update tree nodes
        childComponent.parentComponent = this;

        //Merge the child component locator to the root component
        Component root = getRootComponent();
        for (Map.Entry<String, Component> entry : childComponent.componentLocator.entrySet()) {
            String key = entry.getKey();
            root.componentLocator.put(key, entry.getValue());
            childComponent.componentLocator.remove(key);
        }
    }

    public void detach(@NotNull Component childComponent) throws MismatchDetachException {
        if (childComponent.parentComponent != this) {
            throw new MismatchDetachException("The child component doesn't belong to the parent");
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

    <T> Provider<T> findProvider(Class<T> type, Annotation qualifier) {
        String key = PokeHelper.makeProviderKey(type, qualifier);
        Component targetComponent = getRootComponent().componentLocator.get(key);
        if (targetComponent == null) {
            return null;
        } else {
            return targetComponent.providers.get(key);
        }
    }

    private <T> void addProvider(@NotNull Provider<T> provider)
            throws ProviderConflictException {
        Class<T> type = provider.type();
        Annotation qualifier = provider.getQualifier();
        String key = PokeHelper.makeProviderKey(type, qualifier);

        addNewKeyToComponent(key, this);

        if (provider.scopeCache == null && scopeCache != null) {
            //If the component has a scope cache and the provider doesn't have. The provider will
            //inherit the component's scope cache.
            provider.scopeCache = scopeCache;
        }
        providers.put(key, provider);
    }

    private void addNewKeyToComponent(String key, Component component) throws ProviderConflictException {
        Component root = getRootComponent();

        if (componentLocator.keySet().contains(key)) {
            String msg = String.format("Type %s has already been registered " +
                    "in this component.", key);
            throw new ProviderConflictException(msg);
        }

        if (root != this && root.componentLocator.keySet().contains(key)) {
            String msg = String.format("Type %s has already been registered " +
                    "in root component.", key);
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
