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
import com.shipdream.lib.poke.exception.ProviderMissingException;
import com.shipdream.lib.poke.util.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Qualifier;
import javax.inject.Singleton;

/**
 * {@link ProviderFinder} that registers providers manually.
 */
public class ProviderFinderByRegistry implements ProviderFinder {
    private static class ProviderHolder<T> {
        private Provider<T> original;
        private Provider<T> overrider;
    }

    final Map<String, ProviderHolder> providers = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> Provider<T> findProvider(Class<T> type, Annotation qualifier) throws ProviderMissingException {
        ProviderHolder providerHolder = providers.get(PokeHelper.makeProviderKey(type, qualifier));

        if (providerHolder != null) {
            if (providerHolder.overrider == null) {
                if (providerHolder.original == null) {
                    throw new ProviderMissingException(type, qualifier);
                } else {
                    return providerHolder.original;
                }
            } else {
                return providerHolder.overrider;
            }
        } else {
            return null;
        }
    }

    /**
     * Register binding for type by full class name without scope cache. {@link Qualifier}
     * of the class will be taken into account. Registering multiple bindings against the
     * same type and qualifier will throw {@link ProviderConflictException}.
     *
     * @param type                    The type
     * @param implementationClassName The full name of the implementation class
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     * @throws ClassNotFoundException    Thrown when the class the class name pointing to is not found.
     */
    public <T> void register(Class<T> type, String implementationClassName)
            throws ProviderConflictException, ClassNotFoundException {
        register(type, implementationClassName, null, false);
    }

    /**
     * Register binding for type by full class name with given scope cache.
     * {@link Qualifier} of the class will be taken into account. Registering multiple
     * bindings against the same type and qualifier will throw {@link ProviderConflictException}.
     *
     * @param type                    The type
     * @param implementationClassName The full name of the implementation class
     * @param scopeCache              The scope cache
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     * @throws ClassNotFoundException    Thrown when the class the class name pointing to is not found.
     */
    public <T> void register(Class<T> type, String implementationClassName, ScopeCache scopeCache)
            throws ProviderConflictException, ClassNotFoundException {
        register(type, implementationClassName, scopeCache, false);
    }

    /**
     * Register binding for type by full class name  without scope cache.
     * {@link Qualifier} of the class will be taken into account. When allowOverride = false, it
     * allows to register overriding binding against the same type and {@link Qualifier} and
     * <b>last wins</b>, otherwise {@link ProviderConflictException} will be thrown.
     *
     * @param type                    The type
     * @param implementationClassName The full name of the implementation class
     * @param scopeCache              The scope cache
     * @param allowOverride           Indicates whether allowing overriding registration
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     * @throws ClassNotFoundException    Thrown when the class the class name pointing to is not found.
     */
    @SuppressWarnings("unchecked")
    public <T> void register(Class<T> type, String implementationClassName, ScopeCache scopeCache,
                             boolean allowOverride) throws ProviderConflictException, ClassNotFoundException {
        Provider<T> provider = new ProviderByClassName<>(type, implementationClassName);
        provider.setScopeCache(scopeCache);
        register(provider, allowOverride);
    }

    /**
     * Unregister binding to the given type and the qualifier annotated to the given
     * implementation class. If there is an overridden type registered already, only unregister the
     * overridden binding. The original one will be unregistered if the this method is called
     * again against to the type and qualifier associated with the provider.
     *
     * @param type                    The type of the implementation to unregister
     * @param implementationClassName The name of the class annotated with the qualifier
     * @throws ClassNotFoundException Thrown when the class of the given class name is not found
     */
    @SuppressWarnings("unchecked")
    public <T> void unregister(Class<T> type, String implementationClassName)
            throws ClassNotFoundException {
        if (implementationClassName == null) {
            throw new NullPointerException(String.format("Can't unregister against null class" +
                    " type %s", type));
        }
        Class<T> clazz = (Class<T>) Class.forName(implementationClassName);
        unregister(type, ReflectUtils.findFirstQualifier(clazz));
    }

    /**
     * Register binding for type by class type without scope cache. {@link Qualifier} of the
     * class will be taken into account. Registering multiple bindings against the same type
     * and qualifier will throw {@link ProviderConflictException}.
     *
     * @param type                The type
     * @param implementationClass The class type of the implementation
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    public <T, S extends T> void register(Class<T> type, Class<S> implementationClass)
            throws ProviderConflictException {
        register(type, implementationClass, null, false);
    }

    /**
     * Register binding for type by class type with given scope cache. {@link Qualifier} of
     * the class will be taken into account. Registering multiple bindings against the same
     * type and qualifier will throw {@link ProviderConflictException}.
     *
     * @param type                The type
     * @param implementationClass The class type of the implementation
     * @param scopeCache          The scope cache
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    public <T, S extends T> void register(Class<T> type, Class<S> implementationClass, ScopeCache scopeCache)
            throws ProviderConflictException {
        register(type, implementationClass, scopeCache, false);
    }

    /**
     * Register binding for type by class type. {@link Qualifier} of the class will be
     * taken into account. When allowOverride = false, it allows to register overriding
     * binding against the same type and {@link Qualifier} and <b>last wins</b>, otherwise
     * {@link ProviderConflictException} will be thrown.
     *
     * @param type                The type
     * @param implementationClass The class type of the implementation
     * @param scopeCache          The scope cache
     * @param allowOverride       Indicates whether allowing overriding registration
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    @SuppressWarnings("unchecked")
    public <T, S extends T> void register(Class<T> type, Class<S> implementationClass,
                                          ScopeCache scopeCache, boolean allowOverride)
            throws ProviderConflictException {
        Provider<T> provider = new ProviderByClassType<>(type, implementationClass);
        provider.setScopeCache(scopeCache);
        register(provider, allowOverride);
    }

    /**
     * Unregister binding to the given type and the qualifier annotated to the given
     * implementation class. If there is an overridden type registered already, only unregister the
     * overridden binding. The original one will be unregistered if the this method is called
     * again against to the type and qualifier associated with the provider.
     *
     * @param type                The type of the implementation to unregister
     * @param implementationClass The class annotated with the qualifier
     */
    public <T, S extends T> void unregister(Class<T> type, Class<S> implementationClass) {
        if (implementationClass == null) {
            throw new NullPointerException(String.format("Can't unregister against null class" +
                    " type %s", type));
        }
        unregister(type, ReflectUtils.findFirstQualifier(implementationClass));
    }

    /**
     * Register a {@link Provider}. Registering multiple bindings against the same type and
     * qualifier will throw {@link ProviderConflictException}.
     *
     * @param provider The provider
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    public void register(Provider provider) throws ProviderConflictException {
        register(provider, false);
    }

    /**
     * Register a {@link Provider}. When allowOverride = false, it allows to register overriding
     * binding against the same type and {@link Qualifier} and <b>last wins</b>, otherwise
     * {@link ProviderConflictException} will be thrown.
     *
     * @param provider      The provider
     * @param allowOverride Indicates whether allowing overriding registration
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    public void register(Provider provider, boolean allowOverride) throws ProviderConflictException {
        Class type = provider.type();
        String key = PokeHelper.makeProviderKey(provider.type(), provider.getQualifier());
        ProviderHolder providerHolder = providers.get(key);

        if (providerHolder == null) {
            //First time add
            providerHolder = new ProviderHolder();
            providerHolder.original = provider;
            providers.put(key, providerHolder);
        } else {
            //existing provider, check conflicts
            if (!allowOverride) {
                //Not overriding, throw conflict exception
                String msg;
                if (provider.getQualifier() == null) {
                    msg = String.format("Type %s has already been registered " +
                            "in this graph.", type);
                } else {
                    msg = String.format("Type %s with Qualifier %s has already " +
                                    "been registered in this graph.",
                            type, provider.getQualifier().annotationType().getName());
                }
                throw new ProviderConflictException(msg);
            } else {
                //Otherwise, overrides the provider
                providerHolder.overrider = provider;
            }
        }
    }

    /**
     * Unregister provider. If there is an overridden type registered already, only unregister the
     * overridden binding. The original one will be unregistered if the this method is called
     * again against to the type and qualifier associated with the provider.
     *
     * @param provider The provider that has the type and qualifier to unregister against
     */
    public void unregister(Provider provider) {
        unregister(provider.type(), provider.getQualifier());
    }

    /**
     * Unregister provider registered with given type and qualifier
     *
     * @param type      The type of the provider is providing
     * @param qualifier The annotation of the qualifier. When null is given, this method will
     *                  specifically look for provider without qualifier
     */
    private void unregister(Class<?> type, Annotation qualifier) {
        String key = PokeHelper.makeProviderKey(type, qualifier);
        ProviderHolder targetProvider = providers.get(key);

        if (targetProvider != null) {
            Provider providerToRemove;
            if (targetProvider.overrider != null) {
                providerToRemove = targetProvider.overrider;
                targetProvider.overrider = null;
            } else {
                providerToRemove = targetProvider.original;
                providers.remove(key);
            }
            if (providerToRemove.scopeCache != null) {
                providerToRemove.scopeCache.removeCache(type, qualifier);
            }
        }
    }

    /**
     * Register component where methods annotated by {@link Provides} will be registered as
     * injection providers. Registering multiple bindings against the same type and qualifier
     * will throw {@link ProviderConflictException}.
     *
     * @param component The components contains methods annotated by {@link Provides}
     * @throws ProvideException          Thrown when exception occurs during providers creating instances
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    public void register(Component component) throws ProvideException,
            ProviderConflictException {
        register(component, false);
    }

    /**
     * Register component where methods annotated by {@link Provides} will be registered as
     * injection providers. When allowOverride = false, it allows to register overriding
     * binding against the same type and {@link Qualifier} and <b>last wins</b>, otherwise
     * {@link ProviderConflictException} will be thrown.
     *
     * @param component     The components contains methods annotated by {@link Provides}
     * @param allowOverride Indicates whether allowing overriding registration
     * @throws ProvideException          Thrown when exception occurs during providers creating instances
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    public void register(Component component, boolean allowOverride) throws ProvideException,
            ProviderConflictException {
        Method[] methods = component.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Provides.class)) {
                registerProvides(component, method, allowOverride);
            }
        }
    }

    /**
     * Unregister component where methods annotated by {@link Provides} will be registered as
     * injection providers.
     *
     * @param component The component contains methods annotated by {@link Provides}
     */
    public void unregister(Component component) {
        Method[] methods = component.getClass().getDeclaredMethods();
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
    }

    private void registerProvides(final Component component, final Method method, boolean allowOverride)
            throws ProvideException, ProviderConflictException {
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class) {
            throw new ProvideException(String.format("Provides method %s must not return void.",
                    method.getName()));
        } else {
            Annotation[] annotations = method.getAnnotations();
            boolean singleton = false;
            Annotation qualifier = null;
            for (Annotation a : annotations) {
                if (a.annotationType() == Singleton.class) {
                    singleton = true;
                }

                if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
                    if (qualifier != null) {
                        throw new ProvideException("Only one Qualifier is supported for Provide method. " +
                                String.format("Found multiple qualifier %s and %s for method %s",
                                        qualifier.getClass().getName(), a.getClass().getName(),
                                        method.getName()));
                    }
                    qualifier = a;
                }
            }

            Provider provider = new MethodProvider(returnType, qualifier, component, method);
            if (singleton) {
                provider.setScopeCache(component.getScopeCache());
            }

            register(provider, allowOverride);
        }
    }

    static class MethodProvider extends Provider {
        private final Component component;
        private final Method method;

        @SuppressWarnings("unchecked")
        MethodProvider(Class type, Annotation qualifier, Component component, Method method) {
            super(type, qualifier);
            this.component = component;
            this.method = method;
        }

        @Override
        protected Object createInstance() throws ProvideException {
            try {
                return method.invoke(component);
            } catch (IllegalAccessException e) {
                throw new ProvideException(String.format("Provides method %s must " +
                        "be accessible.", method.getName()), e);  // $COVERAGE-IGNORE$
            } catch (InvocationTargetException e) {
                throw new ProvideException(String.format("Provides method %s is not able " +
                        "to be invoked against %s.", method.getName(), component.getClass().getName()));  // $COVERAGE-IGNORE$
            }
        }
    }
}
