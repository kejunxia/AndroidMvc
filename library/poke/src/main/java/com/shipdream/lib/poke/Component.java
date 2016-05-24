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
import com.shipdream.lib.poke.exception.ProviderConflictException;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Qualifier;

/**
 * //TODO: document, component has a default scope cache
 * {@link Component} that registers providers manually.
 */
public class Component {
    //TODO: document
    public static class MismatchDetachException extends Exception {
        public MismatchDetachException(String message) {
            super(message);
        }
    }

    //TODO: document, use the scopeCache
    final ScopeCache scopeCache;

    final Map<String, Component> componentLocator = new HashMap<>();
    final Map<String, Provider> providers = new HashMap<>();
    private Component parentComponent;
    private List<Component> childComponents;

    public Component() {
        this(null);
    }

    public Component(String scope) {
        if (scope != null) {
            this.scopeCache = new ScopeCache(scope);
        } else {
            this.scopeCache = null;
        }
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
        removeProvider(provider.type(), provider.getQualifier());
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

                    removeProvider(returnType, qualifier);
                }
            }
        }
        return this;
    }


    public void attach(@NotNull Component childComponent) {
        if (childComponents == null) {
            childComponents = new ArrayList<>();
        }

        //Update tree nodes
        childComponent.parentComponent = this;
        childComponents.add(childComponent);


        mergeComponentLocator(childComponent);
    }

    /**
     * Extract and merge all descents' componentLocators to this component
     * @param child The child component
     */
    private void mergeComponentLocator(@NotNull Component child) {
        for (Map.Entry<String, Component> entry : child.componentLocator.entrySet()) {
            componentLocator.put(entry.getKey(), entry.getValue());
        }
        if (child.childComponents != null) {
            for (Component grandChild : child.childComponents) {
                mergeComponentLocator(grandChild);
            }
        }
    }

    public void detach(@NotNull Component childComponent) throws MismatchDetachException {
        if (childComponent.parentComponent != this
                || childComponents == null) {
            throw new MismatchDetachException("The child component doesn't belong to the parent");
        }

        childComponents.remove(childComponent);
        //Update tree nodes
        childComponent.parentComponent = null;

        disbandComponentLocator(childComponent);
    }

    private void disbandComponentLocator(@NotNull Component child) {
        for (String key : child.componentLocator.keySet()) {
            componentLocator.remove(key);
        }
        if (child.childComponents != null) {
            for (Component grandChild : child.childComponents) {
                disbandComponentLocator(grandChild);
            }
        }
    }

    <T> Provider<T> findProvider(Class<T> type, Annotation qualifier) {
        String key = PokeHelper.makeProviderKey(type, qualifier);
        Component targetComponent = findTargetComponent(key);
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
        if (componentLocator.keySet().contains(key)) {
            String msg = String.format("Type %s has already been registered " +
                    "in this component or its ancestor component.", key);
            throw new ProviderConflictException(msg);
        }

        componentLocator.put(key, component);

        if (component.parentComponent != null) {
            addNewKeyToComponent(key, component.parentComponent);
        }
    }

    private <T> void removeProvider(Class<T> type, Annotation qualifier) {
        String key = PokeHelper.makeProviderKey(type, qualifier);
        Component targetComponent = findTargetComponent(key);

        targetComponent.providers.remove(key);
        if (targetComponent.scopeCache != null) {
            targetComponent.scopeCache.removeCache(type, qualifier);
        }

        removeNewKeyToComponent(key, targetComponent);
    }

    private void removeNewKeyToComponent(String key, Component component) {
        component.componentLocator.remove(key);

        if (component.parentComponent != null) {
            removeNewKeyToComponent(key, component.parentComponent);
        }
    }

    private Component findTargetComponent(String key) {
        Component component = componentLocator.get(key);
        if (component == null) {
            if (componentLocator == null || componentLocator.isEmpty()) {
                return null;
            } else {
                for (Component child : componentLocator.values()) {
                    Component found = child.findTargetComponent(key);
                    if (found != null) {
                        return found;
                    }
                }
                return null;
            }
        } else {
            return component;
        }
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
