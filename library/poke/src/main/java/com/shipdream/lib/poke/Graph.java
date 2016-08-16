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

import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderMissingException;
import com.shipdream.lib.poke.util.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

/**
 * A graph manages how to inject dependencies to target objects.
 */
public class Graph {
    public static class IllegalRootComponentException extends PokeException {
        public IllegalRootComponentException(String message) {
            super(message);
        }
    }

    private List<Monitor> monitors;
    private String revisitedNode = null;
    private Set<String> visitedInjectNodes = new LinkedHashSet<>();
    private Map<Object, Map<String, Set<String>>> visitedFields = new HashMap<>();
    private List<Provider.DereferenceListener> dereferenceListeners;
    private List<Provider.DisposeListener> disposeListeners;

    private Component rootComponent;

    /**
     * Register {@link Monitor} which will be called the graph is about to inject or release an object
     *
     * @param monitor The monitor
     */
    public void registerMonitor(Monitor monitor) {
        if (monitors == null) {
            monitors = new CopyOnWriteArrayList<>();
        }
        monitors.add(monitor);
    }

    /**
     * Register {@link Monitor} which will be called the graph is about to inject or release an object
     *
     * @param monitor The monitor
     */
    public void unregisterMonitor(Monitor monitor) {
        if (monitors != null) {
            monitors.remove(monitor);
            if (monitors.isEmpty()) {
                monitors = null;
            }
        }
    }

    /**
     * Clear {@link Monitor} which will be called the graph is about to inject or release an object
     */
    public void clearMonitors() {
        if (monitors != null) {
            monitors.clear();
            monitors = null;
        }
    }

    /**
     * Add {@link Component} to the graph.
     *
     * @param component The root {@link Component} of this graph.
     */
    public void setRootComponent(Component component) throws IllegalRootComponentException {
        if (component != null && component.getParent() != null) {
            throw new IllegalRootComponentException("A component with parent cannot be set as a graph's root component. Make sure the component doesn't parent.");
        }
        this.rootComponent = component;
    }

    public Component getRootComponent() {
        return rootComponent;
    }

    /**
     * Register {@link Provider.DisposeListener} which will be called when either
     * <ul>
     *     <li>The provider doesn't have a scope instances and a provided instance is dereferenced</li>
     *     <li>The provider has a scope instances and the provider is dereferenced with 0 reference count</li>
     * </ul>
     * @param disposeListener The listener
     */
    public void registerDisposeListener(Provider.DisposeListener disposeListener) {
        if (disposeListeners == null) {
            disposeListeners = new CopyOnWriteArrayList<>();
        }
        disposeListeners.add(disposeListener);
    }

    /**
     * Unregister {@link Provider.DisposeListener} which will be called when either
     * <ul>
     *     <li>The provider doesn't have a scope instances and a provided instance is dereferenced</li>
     *     <li>The provider has a scope instances and the provider is dereferenced with 0 reference count</li>
     * </ul>
     *
     * @param disposeListener The listener
     */
    public void unregisterDisposeListener(Provider.DisposeListener disposeListener) {
        if (disposeListeners != null) {
            disposeListeners.remove(disposeListener);

            if (disposeListeners.isEmpty()) {
                disposeListeners = null;
            }
        }
    }

    /**
     * Clear {@link Provider.DisposeListener}s which will be called when when either
     * <ul>
     *     <li>The provider doesn't have a scope instances and a provided instance is dereferenced</li>
     *     <li>The provider has a scope instances and the provider is dereferenced with 0 reference count</li>
     * </ul>
     */
    public void clearDisposeListeners() {
        if (disposeListeners != null) {
            disposeListeners.clear();
            disposeListeners = null;
        }
    }

    /**
     * Register {@link Provider.DereferenceListener} which will be called when the provider's
     * instance is dereferenced.
     *
     * @param dereferenceListener The listener
     */
    public void registerDereferencedListener(Provider.DereferenceListener dereferenceListener) {
        if (dereferenceListeners == null) {
            dereferenceListeners = new CopyOnWriteArrayList<>();
        }
        dereferenceListeners.add(dereferenceListener);
    }

    /**
     * Unregister {@link Provider.DereferenceListener} which will be called when the provider's
     * instance is dereferenced.
     *
     * @param dereferenceListener The listener
     */
    public void unregisterDereferencedListener(Provider.DereferenceListener dereferenceListener) {
        if (dereferenceListeners != null) {
            dereferenceListeners.remove(dereferenceListener);
            if (dereferenceListeners.isEmpty()) {
                dereferenceListeners = null;
            }
        }
    }

    /**
     * Clear {@link Provider.DereferenceListener}s which will be called when the provider's
     * instance is dereferenced.
     */
    public void clearDereferencedListeners() {
        if (dereferenceListeners != null) {
            dereferenceListeners.clear();
            dereferenceListeners = null;
        }
    }

    /**
     * Inject all fields annotated by the given injectAnnotation
     *
     * @param target           Whose fields will be injected
     * @param injectAnnotation Annotated which a field will be recognize
     * @throws ProvideException
     */
    public void inject(Object target, Class<? extends Annotation> injectAnnotation)
            throws ProvideException, ProviderMissingException, CircularDependenciesException {
        if (monitors != null) {
            int size = monitors.size();
            for (int i = 0; i < size; i++) {
                monitors.get(i).onInject(target);
            }
        }
        doInject(target, null, null, null, injectAnnotation);
        visitedInjectNodes.clear();
        revisitedNode = null;
        visitedFields.clear();
    }

    /**
     * Same as {@link #use(Class, Annotation, Class, Consumer)} except using un-qualified injectable type.
     *
     * @param type             The type of the injectable instance
     * @param injectAnnotation injectAnnotation
     * @param consumer         Consume to use the instance
     * @throws ProvideException              ProvideException
     * @throws CircularDependenciesException CircularDependenciesException
     * @throws ProviderMissingException      ProviderMissingException
     */
    public <T> void use(Class<T> type, Class<? extends Annotation> injectAnnotation, Consumer<T> consumer)
            throws ProvideException, CircularDependenciesException, ProviderMissingException {
        use(type, null, injectAnnotation, consumer);
    }

    /**
     * Use an injectable instance in the scope of {@link Consumer#consume(Object)} without injecting
     * it as a field of an object. This method will automatically retain the instance before
     * {@link Consumer#consume(Object)} is called and released after it's returned. As a result,
     * it doesn't hold the instance like the field marked by {@link Inject} that will retain the
     * reference of the instance until {@link #release(Object, Class)} is called. However, in the
     * scope of {@link Consumer#consume(Object)} the instance will be held.
     * <p>For example,</p>
     * <pre>
     * private static class Device {
     * @MyInject
     * private Os os;
     * }
     *
     * final SimpleGraph graph = new SimpleGraph();
     * ScopeCache scopeCache = new ScopeCache();
     *
     * graph.register(Os.class, Android.class, scopeCache);
     *
     * //OsReferenceCount = 0
     * graph.use(Os.class, null, Inject.class, new Consumer<Os>() {
     * @Override
     * public void consume(Os instance) {
     * //First time to create the instance.
     * //OsReferenceCount = 1
     * }
     * });
     * //Reference count decremented by use method automatically
     * //OsReferenceCount = 0
     *
     * Device device = new Device();
     * graph.inject(device, MyInject.class);  //OsReferenceCount = 1
     * //New instance created and cached
     *
     * graph.use(Os.class, null, Inject.class, new Consumer<Os>() {
     * @Override
     * public void consume(Os instance) {
     * //Since reference count is greater than 0, cached instance will be reused
     * //OsReferenceCount = 2
     * Assert.assertTrue(device.os == instance);
     * }
     * });
     * //Reference count decremented by use method automatically
     * //OsReferenceCount = 1
     *
     * graph.release(device, MyInject.class);  //OsReferenceCount = 0
     * //Last instance released, so next time a new instance will be created
     *
     * graph.use(Os.class, null, Inject.class, new Consumer<Os>() {
     * @Override
     * public void consume(Os instance) {
     * //OsReferenceCount = 1
     * //Since the cached instance is cleared, the new instance is a newly created one.
     * Assert.assertTrue(device.os != instance);
     * }
     * });
     * //Reference count decremented by use method automatically
     * //OsReferenceCount = 0
     *
     * graph.use(Os.class, null, Inject.class, new Consumer<Os>() {
     * @Override
     * public void consume(Os instance) {
     * //OsReferenceCount = 1
     * //Since the cached instance is cleared, the new instance is a newly created one.
     * Assert.assertTrue(device.os != instance);
     * }
     * });
     * //Reference count decremented by use method automatically
     * //OsReferenceCount = 0
     * //Cached instance cleared again
     *
     * graph.use(Os.class, null, Inject.class, new Consumer<Os>() {
     * @Override
     * public void consume(Os instance) {
     * //OsReferenceCount = 1
     * graph.inject(device, MyInject.class);
     * //Injection will reuse the cached instance and increment the reference count
     * //OsReferenceCount = 2
     *
     * //Since the cached instance is cleared, the new instance is a newly created one.
     * Assert.assertTrue(device.os == instance);
     * }
     * });
     * //Reference count decremented by use method automatically
     * //OsReferenceCount = 1
     *
     * graph.release(device, MyInject.class);  //OsReferenceCount = 0
     * </pre>
     *
     * @param type             The type of the injectable instance
     * @param qualifier        Qualifier for the injectable instance
     * @param injectAnnotation injectAnnotation
     * @param consumer         Consume to use the instance
     * @throws ProvideException              ProvideException
     * @throws CircularDependenciesException CircularDependenciesException
     * @throws ProviderMissingException      ProviderMissingException
     */
    public <T> void use(Class<T> type, Annotation qualifier,
                        Class<? extends Annotation> injectAnnotation, Consumer<T> consumer)
            throws ProvideException, CircularDependenciesException, ProviderMissingException {
        T instance = reference(type, qualifier, injectAnnotation);
        consumer.consume(instance);
        dereference(instance, type, qualifier, injectAnnotation);
    }

    private <T> Provider<T> findProvider(Class<T> type, Annotation qualifier) throws ProviderMissingException {
        Provider<T> provider = rootComponent.findProvider(type, qualifier);
        return provider;
    }

    /**
     * Reference an injectable object and retain it. Use
     * {@link #dereference(Object, Class, Annotation, Class)} to dereference it when it's not used
     * any more.
     *
     * @param type             the type of the object
     * @param qualifier        the qualifier
     * @param injectAnnotation the inject annotation
     * @return
     */
    public <T> T reference(Class<T> type, Annotation qualifier, Class<? extends Annotation> injectAnnotation)
            throws ProviderMissingException, ProvideException, CircularDependenciesException {
        Provider<T> provider = findProvider(type, qualifier);
        T instance = provider.get();
        doInject(instance, null, type, qualifier, injectAnnotation);
        provider.retain();
        provider.notifyReferenced(provider, instance);

        //Clear visiting records
        visitedFields.clear();
        visitedInjectNodes.clear();

        return instance;
    }

    /**
     * Dereference an injectable object. When it's not referenced by anything else after this
     * dereferencing, release its cached instance if possible.
     *
     * @param instance         the instance is to dereference
     * @param type             the type of the object
     * @param qualifier        the qualifier
     * @param injectAnnotation the inject annotation
     */
    public <T> void dereference(T instance, Class<T> type, Annotation qualifier,
                                Class<? extends Annotation> injectAnnotation) throws ProviderMissingException {
        doRelease(instance, null, type, qualifier, injectAnnotation);

        Provider<T> provider = findProvider(type, qualifier);
        provider.release();
        dereferenceProvider(provider, instance);
    }

    @SuppressWarnings("unchecked")
    private void doInject(Object target, Field targetField, Class targetType, Annotation targetQualifier,
                          Class<? extends Annotation> injectAnnotation)
            throws ProvideException, ProviderMissingException, CircularDependenciesException {
        boolean circularDetected = false;
        Provider targetProvider;
        if (targetType != null) {
            //Nested injection
            circularDetected = recordVisit(targetType, targetQualifier);
            targetProvider = findProvider(targetType, targetQualifier);
            Object cachedInstance = targetProvider.getCachedInstance();
            boolean infiniteCircularInjection = true;
            if (circularDetected) {
                if (cachedInstance != null) {
                    infiniteCircularInjection = false;
                }

                if (infiniteCircularInjection) {
                    throwCircularDependenciesException();
                }
            }
        }

        if (!circularDetected && target != null) {
            Class<?> clazz = target.getClass();
            while (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(injectAnnotation)) {
                        Class fieldType = field.getType();
                        Annotation fieldQualifier = ReflectUtils.findFirstQualifierInAnnotations(field);
                        Provider provider = findProvider(fieldType, fieldQualifier);

                        Object impl = provider.get();
                        ReflectUtils.setField(target, field, impl);

                        boolean visited = isFieldVisited(target, targetField, field);
                        if (!visited) {
                            doInject(impl, field, fieldType, fieldQualifier, injectAnnotation);
                        }

                        provider.retain(target, field);
                        provider.notifyReferenced(provider, impl);

                        recordVisitField(target, targetField, field);
                    }
                }
                clazz = clazz.getSuperclass();
            }

            if (targetType != null) {
                unrecordVisit(targetType, targetQualifier);
            }
        }
    }

    /**
     * Release cached instances held by fields of target object. References of instances of the
     * instances will be decremented. Once the reference count of a controller reaches 0, it will
     * be removed from the instances and raise {@link Provider.DereferenceListener}.
     *
     * @param target           Whose fields will be injected
     * @param injectAnnotation Annotated which a field will be recognize
     */
    public void release(Object target, Class<? extends Annotation> injectAnnotation) throws ProviderMissingException {
        if (monitors != null) {
            int size = monitors.size();
            for (int i = 0; i < size; i++) {
                monitors.get(i).onRelease(target);
            }
        }
        doRelease(target, null, null, null, injectAnnotation);
        visitedInjectNodes.clear();
        revisitedNode = null;
        visitedFields.clear();
    }

    private void doRelease(Object target, Field targetField, Class targetType, Annotation targetQualifier,
                           final Class<? extends Annotation> injectAnnotation) throws ProviderMissingException {
        Class<?> clazz = target.getClass();

        boolean circularDetected = false;

        if (targetType != null) {
            circularDetected = recordVisit(targetType, targetQualifier);
        }

        if (!circularDetected) {
            while (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(injectAnnotation)) {
                        Object fieldValue = ReflectUtils.getFieldValue(target, field);
                        if (fieldValue != null) {
                            final Class<?> fieldType = field.getType();
                            Annotation fieldQualifier = ReflectUtils.findFirstQualifierInAnnotations(field);
                            Provider provider = findProvider(fieldType, fieldQualifier);

                            boolean stillReferenced = provider.getReferenceCount(target, field) > 0;
                            boolean fieldVisited = isFieldVisited(target, targetField, field);
                            if (!fieldVisited && stillReferenced) {
                                recordVisitField(target, targetField, field);
                                doRelease(fieldValue, field, fieldType, fieldQualifier, injectAnnotation);

                                provider.release(target, field);

                                dereferenceProvider(provider, fieldValue);
                            }
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }

            if (targetType != null) {
                unrecordVisit(targetType, targetQualifier);
            }
        }
    }

    private <T> void dereferenceProvider(Provider<T> provider, T instance) {
        if (dereferenceListeners != null) {
            int listenerSize = dereferenceListeners.size();
            for (int i = 0; i < listenerSize; i++) {
                dereferenceListeners.get(i).onDereferenced(provider, instance);
            }
        }
        if (disposeListeners != null) {
            boolean disposing = false;
            if (provider.getScopeCache() == null) {
                disposing = true;
            } else if (provider.getReferenceCount() == 0) {
                disposing = true;
            }
            if (disposing) {
                int listenerSize = disposeListeners.size();
                for (int i = 0; i < listenerSize; i++) {
                    disposeListeners.get(i).onDisposed(provider, instance);
                }
            }
        }
    }

    /**
     * Records the field of a target object is visited
     *
     * @param object      The field holder
     * @param objectField The field which holds the object in its parent
     * @param field       The field of the holder
     */
    private void recordVisitField(Object object, Field objectField, Field field) {
        Map<String, Set<String>> bag = visitedFields.get(object);
        if (bag == null) {
            bag = new HashMap<>();
            visitedFields.put(object, bag);
        }
        Set<String> fields = bag.get(objectField);

        String objectFiledKey = objectField == null ? "" : objectField.toGenericString();

        if (fields == null) {
            fields = new HashSet<>();
            bag.put(objectFiledKey, fields);
        }
        fields.add(field.toGenericString());
    }

    /**
     * Indicates whether the field of a target object is visited
     *
     * @param object      The field holder
     * @param objectField The field which holds the object in its parent
     * @param field       The field of the holder
     */
    private boolean isFieldVisited(Object object, Field objectField, Field field) {
        Map<String, Set<String>> bag = visitedFields.get(object);
        if (bag == null) {
            return false;
        }

        String objectFiledKey = objectField == null ? "" : objectField.toGenericString();
        Set<String> fields = bag.get(objectFiledKey);
        return fields != null && fields.contains(field);
    }

    private boolean recordVisit(Class classType, Annotation qualifier) {
        String key = PokeHelper.makeProviderKey(classType, qualifier);
        boolean circularVisitDetected = visitedInjectNodes.contains(key);
        if (!circularVisitDetected) {
            visitedInjectNodes.add(key);
        } else {
            revisitedNode = key;
        }
        return circularVisitDetected;
    }

    private void unrecordVisit(Class classType, Annotation qualifier) {
        String key = PokeHelper.makeProviderKey(classType, qualifier);
        visitedInjectNodes.remove(key);
    }

    /**
     * Print readable circular graph
     *
     * @throws CircularDependenciesException
     */
    private void throwCircularDependenciesException()
            throws CircularDependenciesException {
        String msg = "Circular dependencies found. Check the circular graph below:\n";
        boolean firstNode = true;
        String tab = "  ";
        for (String visit : visitedInjectNodes) {
            if (!firstNode) {
                msg += tab + "->";
                tab += tab;
            }
            msg += visit + "\n";
            firstNode = false;
        }
        msg += tab.substring(2) + "->" + revisitedNode + "\n";
        throw new CircularDependenciesException(msg);
    }

    /**
     * Monitor to watch when the graph is about to inject or release an object
     */
    public interface Monitor {
        /**
         * Called when the graph is about to inject dependencies into the given object
         *
         * @param target The object whose injectable fields have been injected
         */
        void onInject(Object target);

        /**
         * Called when the graph is about to release dependencies from the given object
         *
         * @param target The object whose injectable fields have been released
         */
        void onRelease(Object target);
    }

}
