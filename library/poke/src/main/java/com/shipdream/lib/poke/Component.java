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
import com.shipdream.lib.poke.exception.ProviderMissingException;
import com.shipdream.lib.poke.util.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Qualifier;

/**
 * //TODO: document, component has a default scope cache
 * {@link ProviderFinder} that registers providers manually.
 */
public class Component implements ProviderFinder {
    //TODO: document, use the scopeCache
    final ScopeCache scopeCache;

    private static class ProviderHolder<T> {
        private Provider<T> original;
        private Provider<T> overrider;
    }

    final Map<String, ProviderHolder> providers = new HashMap<>();

    public Component() {
        this(null);
    }

    public Component(ScopeCache scopeCache) {
        this.scopeCache = scopeCache;
    }

    //TODO: should hide this method from publicly accessible
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
     * @return this instance
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     * @throws ClassNotFoundException    Thrown when the class the class name pointing to is not found.
     */
    public <T> Component register(Class<T> type, String implementationClassName)
            throws ProviderConflictException, ClassNotFoundException {
        return register(type, implementationClassName, false);
    }

    /**
     * Register binding for type by full class name  without scope cache.
     * {@link Qualifier} of the class will be taken into account. When allowOverride = false, it
     * allows to register overriding binding against the same type and {@link Qualifier} and
     * <b>last wins</b>, otherwise {@link ProviderConflictException} will be thrown.
     *
     * @param type                    The type
     * @param implementationClassName The full name of the implementation class
     * @param allowOverride           Indicates whether allowing overriding registration
     * @return this instance
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     * @throws ClassNotFoundException    Thrown when the class the class name pointing to is not found.
     */
    @SuppressWarnings("unchecked")
    public <T> Component register(Class<T> type, String implementationClassName, boolean allowOverride)
            throws ProviderConflictException, ClassNotFoundException {
        Provider<T> provider = new ProviderByClassName<>(type, implementationClassName, scopeCache);
        return register(provider, allowOverride);
    }

    /**
     * Unregister binding to the given type and the qualifier annotated to the given
     * implementation class. If there is an overridden type registered already, only unregister the
     * overridden binding. The original one will be unregistered if the this method is called
     * again against to the type and qualifier associated with the provider.
     *
     * @param type                    The type of the implementation to unregister
     * @param implementationClassName The name of the class annotated with the qualifier
     * @return this instance
     * @throws ClassNotFoundException Thrown when the class of the given class name is not found
     */
    @SuppressWarnings("unchecked")
    public <T> Component unregister(Class<T> type, String implementationClassName)
            throws ClassNotFoundException {
        if (implementationClassName == null) {
            throw new NullPointerException(String.format("Can't unregister against null class" +
                    " type %s", type));
        }
        Class<T> clazz = (Class<T>) Class.forName(implementationClassName);
        return unregister(type, ReflectUtils.findFirstQualifierInAnnotations(clazz));
    }

    /**
     * Register binding for type by class type with given scope cache. {@link Qualifier} of
     * the class will be taken into account. Registering multiple bindings against the same
     * type and qualifier will throw {@link ProviderConflictException}.
     *
     * @param type                The type
     * @param implementationClass The class type of the implementation
     * @return this instance
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    public <T, S extends T> Component register(Class<T> type, Class<S> implementationClass)
            throws ProviderConflictException {
        return register(type, implementationClass, false);
    }

    /**
     * Register binding for type by class type. {@link Qualifier} of the class will be
     * taken into account. When allowOverride = false, it allows to register overriding
     * binding against the same type and {@link Qualifier} and <b>last wins</b>, otherwise
     * {@link ProviderConflictException} will be thrown.
     *
     * @param type                The type
     * @param implementationClass The class type of the implementation
     * @param allowOverride       Indicates whether allowing overriding registration
     * @return this instance
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    @SuppressWarnings("unchecked")
    public <T, S extends T> Component register(Class<T> type, Class<S> implementationClass, boolean allowOverride)
            throws ProviderConflictException {
        Provider<T> provider = new ProviderByClassType<>(type, implementationClass, scopeCache);
        return register(provider, allowOverride);
    }

    /**
     * Unregister binding to the given type and the qualifier annotated to the given
     * implementation class. If there is an overridden type registered already, only unregister the
     * overridden binding. The original one will be unregistered if the this method is called
     * again against to the type and qualifier associated with the provider.
     *
     * @param type                The type of the implementation to unregister
     * @param implementationClass The class annotated with the qualifier
     * @return this instance
     */
    public <T, S extends T> Component unregister(Class<T> type, Class<S> implementationClass) {
        if (implementationClass == null) {
            throw new NullPointerException(String.format("Can't unregister against null class" +
                    " type %s", type));
        }
        return unregister(type, ReflectUtils.findFirstQualifierInAnnotations(implementationClass));
    }

    /**
     * Register a {@link Provider}. Registering multiple bindings against the same type and
     * qualifier will throw {@link ProviderConflictException}.
     *
     * @param provider The provider
     * @return this instance
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    public Component register(Provider provider) throws ProviderConflictException {
        return register(provider, false);
    }

    /**
     * //TODO: document how component scope cache will override provider's
     * Register a {@link Provider}. When allowOverride = false, it allows to register overriding
     * binding against the same type and {@link Qualifier} and <b>last wins</b>, otherwise
     * {@link ProviderConflictException} will be thrown.
     *
     * @param provider      The provider
     * @param allowOverride Indicates whether allowing overriding registration
     * @return this instance
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    public Component register(Provider provider, boolean allowOverride) throws ProviderConflictException {
        Class type = provider.type();
        String key = PokeHelper.makeProviderKey(provider.type(), provider.getQualifier());
        ProviderHolder providerHolder = providers.get(key);

        if (scopeCache != null && provider.scopeCache == null) {
            //If the component has a scope cache and the provider doesn't have. The provider will
            //inherit the component's scope cache.
            provider.scopeCache = scopeCache;
        }

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
                providerHolder.original.scopeCache.removeCache(provider.type(), provider.getQualifier());
            }
        }
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

    /**
     * Unregister provider registered with given type and qualifier
     *
     * @param type      The type of the provider is providing
     * @param qualifier The annotation of the qualifier. When null is given, this method will
     *                  specifically look for provider without qualifier
     * @return this instance
     */
    private Component unregister(Class<?> type, Annotation qualifier) {
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
        return this;
    }

    /**
     * Register component where methods annotated by {@link Provides} will be registered as
     * injection providers. Registering multiple bindings against the same type and qualifier
     * will throw {@link ProviderConflictException}.
     *
     * @param providerHolder The object with methods marked by {@link Provides} to provide injectable
     *                      instances
     * @return this instance
     * @throws ProvideException          Thrown when exception occurs during providers creating instances
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    public Component register(Object providerHolder) throws ProvideException,
            ProviderConflictException {
        return register(providerHolder, false);
    }

    /**
     * Register component where methods annotated by {@link Provides} will be registered as
     * injection providers. When allowOverride = false, it allows to register overriding
     * binding against the same type and {@link Qualifier} and <b>last wins</b>, otherwise
     * {@link ProviderConflictException} will be thrown.
     *
     * @param providerHolder The object with methods marked by {@link Provides} to provide injectable
     *                      instances
     * @param allowOverride Indicates whether allowing overriding registration
     * @return this instance
     * @throws ProvideException          Thrown when exception occurs during providers creating instances
     * @throws ProviderConflictException Thrown when duplicate registries detected against the same
     *                                   type and qualifier.
     */
    public Component register(Object providerHolder, boolean allowOverride) throws ProvideException,
            ProviderConflictException {
        Method[] methods = providerHolder.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Provides.class)) {
                registerProvides(providerHolder, method, allowOverride);
            }
        }
        return this;
    }

    /**
     * Unregister component where methods annotated by {@link Provides} will be registered as
     * injection providers.
     *
     * @param providerHolder The object with methods marked by {@link Provides} to provide injectable
     *                      instances
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

    private void registerProvides(final Object providerHolder, final Method method, boolean allowOverride)
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
            register(provider, allowOverride);
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
