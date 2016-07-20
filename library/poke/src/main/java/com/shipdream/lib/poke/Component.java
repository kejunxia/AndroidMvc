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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Qualifier;

/**
 * <p>
 * A component is used to group {@link Provider}s. A {@link Provider} can be {@link #register(Provider)}ed
 * to a component to provide injection candidate. Component can also {@link #register(Object)}
 * multiple providers by methods provider annotated by {@link Provides} in the object.
 * </p>
 *
 * <p>
 * A component can have a cache to manage created injection candidates. If the cache is enabled,
 * the injection candidates are singleton in the scope of the component. When the cache is disabled,
 * the component will always create new instances from its managed providers.
 * </p>
 *
 * <p>
 * Components can be group in a tree structure by attaching a child component to a parent component.
 * In a component tree, injection candidates are unique by the combination of class type and qualifier.
 * When duplicate providers for the same class type and qualifier are found, a {@link ProviderConflictException}
 * will be thrown.
 * </p>
 *
 * <p>
 * To override a provider to the graph the component or its component tree is attaching to, use
 * {@link #attach(Component, boolean)} with true as the second parameter. Then the last attached
 * component will a conflicting class type and qualifier will be used until it's {@link #detach(Component)}
 * </p>
 */
public class Component {
    /**
     * Thrown when detach a component from a parent it doesn't belong to.
     */
    public static class MismatchDetachException extends PokeException {
        public MismatchDetachException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when assigning parent to an attached component already has a parent component.
     */
    public static class MultiParentException extends PokeException {
        public MultiParentException(String message) {
            super(message);
        }
    }

    private final String name;
    protected ScopeCache scopeCache;

    final Map<String, Component> componentLocator = new HashMap<>();
    Map<String, List<Component>> overriddenChain;
    protected final Map<String, Provider> providers = new HashMap<>();
    private Component parentComponent;
    private List<Component> childrenComponents;

    /**
     * Construct an unnamed component with a cache. See {@link #Component(String, boolean)}
     */
    public Component() {
        this(null, true);
    }

    /**
     * Construct a component with instance cache and the given name. See {@link #Component(String, boolean)}
     * @param name The name of the component.
     */
    public Component(String name) {
        this(name, true);
    }

    /**
     * Construct a component without a name.
     * @param enableCache indicates whether this component cache created instances. When the component
     *                    has a cache, all instances created by the providers managed by this component
     *                    will be singleton until the component is destroyed or replaced in the
     *                    component tree. Otherwise, the component always generates new instances
     *                    for injections.
     */
    public Component(boolean enableCache) {
        this(null, enableCache);
    }

    /**
     * Construct a component.
     * @param name name of the component used to identify the component.
     * @param enableCache indicates whether this component cache created instances. When the component
     *                    has a cache, all instances created by the providers managed by this component
     *                    will be singleton until the component is destroyed or replaced in the
     *                    component tree. Otherwise, the component always generates new instances
     *                    for injections.
     */
    public Component(String name, boolean enableCache) {
        if (enableCache) {
            scopeCache = new ScopeCache();
        } else {
            scopeCache = null;
        }
        this.name = name;
    }

    /**
     * @return The name of the component
     */
    public String getName() {
        return name;
    }

    public Map<String, Object> getCache() {
        if (scopeCache == null) {
            return null;
        } else {
            return scopeCache.instances;
        }
    }

    /**
     * @return The parent component
     */
    public Component getParent() {
        return parentComponent;
    }

    /**
     * @return The list of the children components
     */
    public List<Component> getChildrenComponents() {
        return childrenComponents;
    }

    /**
     * //TODO: document how component scope instances will override provider's
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
     * own scope instances.
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
            targetComponent.scopeCache.removeInstance(PokeHelper.makeProviderKey(type, qualifier));
        }

        Component root = getRootComponent();
        //Remove it from root component's locator
        root.componentLocator.remove(key);

        return this;
    }

    /**
     * <p>
     * Register component where methods annotated by {@link Provides} will be registered as
     * injection providers. When allowOverride = false, it allows to register overriding
     * binding against the same type and {@link Qualifier} and <b>last wins</b>, otherwise
     * {@link ProviderConflictException} will be thrown.
     * </p>
     *
     * <pre>
     *     component.register(new Object(){
                @ Provides
                @ EventBusV
                public EventBus createEventBusV() {
                    return eventBusV;
                }
            });
     * </pre>
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

    /**
     * Attach a component to this component. The root of the component tree this component belongs
     * to will be able to find all providers registered to the child component.
     * @param childComponent The component to be added as a child component
     * @throws MultiParentException Thrown when the child component has had a parent already.
     * @throws ProviderConflictException Thrown when the child component has provider has been
     * registered to component tree this component belongs to.
     */
    public void attach(@NotNull Component childComponent) throws MultiParentException, ProviderConflictException {
        attach(childComponent, false);
    }

    /**
     * Attach a component to this component. The root of the component tree this component belongs
     * to will be able to find all providers registered to the child component.
     * @param childComponent The component to be added as a child component
     * @throws MultiParentException Thrown when the child component has had a parent already.
     * @throws ProviderConflictException Thrown when the child component has provider has been
     * registered to component tree this component belongs to.
     */
    public void attach(@NotNull Component childComponent, boolean allowOverride) throws MultiParentException, ProviderConflictException {
        if (childComponent.parentComponent != null) {
            String msg = String.format("The attaching component(%s) has a parent already. Remove its parent before attaching it",
                    childComponent.name == null ? "unnamed" : childComponent.name);
            throw new MultiParentException(msg);
        }

        //Merge the child component locator to the root component
        Component root = getRootComponent();

        if (childComponent.parentComponent == null) {
            //Child component was a root component
        }

        Set<String> addedKeys = new HashSet<>();
        Iterator<Map.Entry<String, Component>> iterator = childComponent.componentLocator.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, Component> entry = iterator.next();
            String key = entry.getKey();

            //check conflict if override is not allowed
            if (root.componentLocator.containsKey(key)) {
                if (!allowOverride) {
                    for (String k : addedKeys) {
                        root.componentLocator.remove(k);
                    }

                    throw new ProviderConflictException(
                            String.format("Type(%s) in the adding child component(%s) has been added " +
                                            "to rootComponent(%s) or its attached child components.",
                                    key, childComponent.getComponentId(), root.getComponentId()));
                } else {
                    if (root.overriddenChain == null) {
                        root.overriddenChain = new HashMap<>();
                    }
                    List<Component> chain = root.overriddenChain.get(key);
                    if (chain == null) {
                        chain = new ArrayList<>();
                        root.overriddenChain.put(key, chain);
                    }
                    chain.add(childComponent);
                }
            }

            root.componentLocator.put(key, entry.getValue());
            iterator.remove();
            addedKeys.add(key);
        }

        //Update tree nodes
        childComponent.parentComponent = this;

        if (childrenComponents == null) {
            childrenComponents = new ArrayList<>();
        }
        childrenComponents.add(childComponent);
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

    /**
     * Detach the child component from this component. Once a component is detached the component
     * tree won't use this component to locate suitable injection candidates and all its cached
     * instances will be removed
     * @param childComponent The child component to detach
     * @throws MismatchDetachException thrown when the child component was not attached to this component
     */
    public void detach(@NotNull Component childComponent) throws MismatchDetachException {
        if (childComponent.parentComponent != this) {
            String msg = String.format("The child component(%s) doesn't belong to component(%s)",
                    childComponent.name == null ? "unnamed" : childComponent.getComponentId(),
                    getComponentId());
            throw new MismatchDetachException(msg);
        }

        //Update tree nodes
        childComponent.parentComponent = null;
        if (childrenComponents != null) {
            childrenComponents.remove(childComponent);
        }

        //Disband the child component locator from the root component and return the keys to child
        //component itself
        Component root = getRootComponent();
        for (String key : childComponent.providers.keySet()) {
            //Find all keys in itself
            childComponent.componentLocator.put(key, childComponent);

            //TODO: need to restructure child components overriding priority, say child
            //has c1, c2 overriding a key before detaching only root is aware of them. After detaching
            //childComponent should perform as a root component so it needs to know how to manage them

            List<Component> chain = null;
            if (root.overriddenChain != null) {
                chain = root.overriddenChain.get(key);
            }

            if (chain != null) {
                if (!chain.isEmpty()) {
                    Component removingItem = null;
                    int size = chain.size();
                    for (int i = size - 1; i >= 0 ; i--) {
                        Component c = chain.get(i);
                        if (c == childComponent) {
                            //Delete the first found
                            removingItem = c;
                            break;
                        }
                    }

                    chain.remove(removingItem);
                }

                //notify root component that child component is surrendering managing the key
                if (chain.isEmpty()) {
                    //No overridden any more
                    if (root.providers.containsKey(key)) {
                        //Root is managing the key itself
                        root.componentLocator.put(key, root);
                    } else {
                        //Nobody is managing the key
                        root.componentLocator.remove(key);
                    }
                } else {
                    //Second last overriding component takes over the management of the key
                    root.componentLocator.put(key, chain.get(chain.size() - 1));
                }
            } else {
                root.componentLocator.remove(key);
            }
        }
    }

    private Component getRootComponent() {
        Component root = this;
        while (root.parentComponent != null) {
            root = root.parentComponent;
        }
        return root;
    }

    /**
     * Find the provider specified by the type and qualifier. It will look through the providers
     * registered to this component and all its children components'.
     * @param type The type the provider is associated with
     * @param qualifier The qualifier the provider is associated with
     * @return The provider
     * @throws ProviderMissingException Thrown when the provider can't be found
     */
    protected <T> Provider<T> findProvider(Class<T> type, Annotation qualifier) throws ProviderMissingException {
        String key = PokeHelper.makeProviderKey(type, qualifier);
        Component targetComponent = getRootComponent().componentLocator.get(key);

        Provider provider = null;
        if (targetComponent != null) {
            provider = targetComponent.providers.get(key);
        }
        if (provider == null) {
            String msg = String.format("Provider(%s) cannot be found", key);
            throw new ProviderMissingException(msg);
        } else {
            return provider;
        }
    }

    /**
     * Add provider to this component and notify the root component this component is
     * managing the provider.
     * @param provider The adding provider
     * @throws ProviderConflictException Thrown when a provider bound to same type and qualifier is
     * added in the component tree that this component is in.
     */
    private <T> void addProvider(@NotNull Provider<T> provider)
            throws ProviderConflictException {
        Class<T> type = provider.type();
        Annotation qualifier = provider.getQualifier();
        String key = PokeHelper.makeProviderKey(type, qualifier);

        addNewKeyToComponent(key, this);

        provider.setComponent(this);
        providers.put(key, provider);
    }

    /**
     * Add key to the component locator of the component. This component and the the component tree
     * root's component locator will both be updated.
     * @param key The key to add
     * @param component The component whose providers directly contain the key
     * @throws ProviderConflictException The key has been added to the component or the component tree
     */
    private void addNewKeyToComponent(String key, Component component) throws ProviderConflictException {
        Component root = getRootComponent();

        if (componentLocator.keySet().contains(key)) {
            String msg = String.format("Type %s has already been registered " +
                    "in this component(%s).", key, getComponentId());
            throw new ProviderConflictException(msg);
        }

        if (root != this && root.componentLocator.keySet().contains(key)) {
            String msg = String.format("\nClass type %s cannot be registered to component(%s)\nsince it's  " +
                    "already been registered in its root component(%s).\n\nYou can prepare a child " +
                    "component and register providers to it first. Then attach the child component\nto the " +
                    "component tree with allowOverridden flag set true", key, getComponentId(),
                    getRootComponent().getComponentId());
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

    /**
     * Method provider to extract providers from object's methods annotated by inject annotation
     */
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
