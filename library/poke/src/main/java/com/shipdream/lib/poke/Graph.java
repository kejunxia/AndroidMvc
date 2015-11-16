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

import com.shipdream.lib.poke.Provider.OnFreedListener;
import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderMissingException;
import com.shipdream.lib.poke.util.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

/**
 * Abstract graph manages how to inject dependencies to target objects.
 */
public abstract class Graph {
    private List<ProviderFinder> providerFinders;
    private Map<Class, ProviderFinder> finderCache = new HashMap<>();
    private List<OnFreedListener> onProviderFreedListeners;
    private List<Monitor> monitors;
    private String revisitedNode = null;
    private Set<String> visitedInjectNodes = new LinkedHashSet<>();
    private Map<Object, Map<String, Set<String>>> visitedFields = new HashMap<>();

    /**
     * Register {@link OnFreedListener} which will be called when the provider
     *
     * @param onProviderFreedListener The listener
     */
    public void registerProviderFreedListener(OnFreedListener onProviderFreedListener) {
        if (onProviderFreedListeners == null) {
            onProviderFreedListeners = new CopyOnWriteArrayList<>();
        }
        onProviderFreedListeners.add(onProviderFreedListener);
    }

    /**
     * Unregister {@link OnFreedListener} which will be called when the last cached
     * instance of an injected contract is freed.
     *
     * @param onProviderFreedListener The listener
     */
    public void unregisterProviderFreedListener(OnFreedListener onProviderFreedListener) {
        if (onProviderFreedListeners != null) {
            onProviderFreedListeners.remove(onProviderFreedListener);
            if (onProviderFreedListeners.isEmpty()) {
                onProviderFreedListeners = null;
            }
        }
    }

    /**
     * Clear {@link OnFreedListener}s which will be called when the last cached
     * instance of an injected contract is freed.
     */
    public void clearOnProviderFreedListeners() {
        if (onProviderFreedListeners != null) {
            onProviderFreedListeners.clear();
            onProviderFreedListeners = null;
        }
    }

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
     * Add {@link ProviderFinder} to the graph directly. Eg. if manual provider registration
     * is needed, a {@link com.shipdream.lib.poke.ProviderFinderByRegistry} can be added.
     * <p/>
     * <p>Note that, when there are multiple {@link ProviderFinder}s able to inject an instance the
     * later merged graph wins.</p>
     *
     * @param providerFinders The {@link ProviderFinder}s to add
     */
    protected void addProviderFinders(ProviderFinder... providerFinders) {
        if (this.providerFinders == null) {
            this.providerFinders = new ArrayList<>();
        }
        this.providerFinders.addAll(Arrays.asList(providerFinders));
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
     * @param type The type of the injectable instance
     * @param injectAnnotation injectAnnotation
     * @param consumer Consume to use the instance
     * @throws ProvideException ProvideException
     * @throws CircularDependenciesException CircularDependenciesException
     * @throws ProviderMissingException ProviderMissingException
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
        private static class Device {
            @MyInject
            private Os os;
        }

        final SimpleGraph graph = new SimpleGraph();
        ScopeCache scopeCache = new ScopeCache();

        graph.register(Os.class, Android.class, scopeCache);

        //OsReferenceCount = 0
        graph.use(Os.class, null, Inject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
              //First time to create the instance.
              //OsReferenceCount = 1
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 0

        Device device = new Device();
        graph.inject(device, MyInject.class);  //OsReferenceCount = 1
        //New instance created and cached

        graph.use(Os.class, null, Inject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
              //Since reference count is greater than 0, cached instance will be reused
              //OsReferenceCount = 2
              Assert.assertTrue(device.os == instance);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 1

        graph.release(device, MyInject.class);  //OsReferenceCount = 0
        //Last instance released, so next time a new instance will be created

        graph.use(Os.class, null, Inject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
              //OsReferenceCount = 1
              //Since the cached instance is cleared, the new instance is a newly created one.
              Assert.assertTrue(device.os != instance);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 0

        graph.use(Os.class, null, Inject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //OsReferenceCount = 1
                //Since the cached instance is cleared, the new instance is a newly created one.
                Assert.assertTrue(device.os != instance);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 0
        //Cached instance cleared again

        graph.use(Os.class, null, Inject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //OsReferenceCount = 1
                graph.inject(device, MyInject.class);  
                //Injection will reuse the cached instance and increment the reference count
                //OsReferenceCount = 2

                //Since the cached instance is cleared, the new instance is a newly created one.
                Assert.assertTrue(device.os == instance);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 1

        graph.release(device, MyInject.class);  //OsReferenceCount = 0      
     * </pre>
     * @param type The type of the injectable instance
     * @param qualifier Qualifier for the injectable instance
     * @param injectAnnotation injectAnnotation
     * @param consumer Consume to use the instance
     * @throws ProvideException ProvideException
     * @throws CircularDependenciesException CircularDependenciesException
     * @throws ProviderMissingException ProviderMissingException
     */
    public <T> void use(Class<T> type, Annotation qualifier,
                        Class<? extends Annotation> injectAnnotation, Consumer<T> consumer)
            throws ProvideException, CircularDependenciesException, ProviderMissingException {
        T instance;

        Provider<T> provider = getProvider(type, qualifier);
        T cachedInstance = provider.findCachedInstance();
        if (cachedInstance != null) {
            instance = cachedInstance;
        } else {
            T newInstance = provider.get();

            doInject(newInstance, null, type, qualifier, injectAnnotation);

            instance = newInstance;
        }

        provider.retain();
        if (provider.getReferenceCount() == 1) {
            provider.notifyInjected(instance);
        }
        consumer.consume(instance);

        //Clear visiting records
        visitedFields.clear();
        visitedInjectNodes.clear();

        doRelease(instance, null, type, qualifier, injectAnnotation);

        provider.release();
        checkToFreeProvider(provider);
    }

    @SuppressWarnings("unchecked")
    private void doInject(Object target, Field targetField, Class targetType, Annotation targetQualifier,
                          Class<? extends Annotation> injectAnnotation)
            throws ProvideException, ProviderMissingException, CircularDependenciesException {
        boolean circularDetected = false;
        Provider targetProvider;
        ScopeCache.CachedItem cachedTargetItem = null;
        if (targetType != null) {
            //Nested injection
            circularDetected = recordVisit(targetType, targetQualifier);
            targetProvider = getProvider(targetType, targetQualifier);
            if (targetProvider.scopeCache != null) {
                cachedTargetItem = targetProvider.scopeCache.findCacheItem(targetType, targetQualifier);
            }
            boolean infiniteCircularInjection = true;
            if (circularDetected) {
                if (cachedTargetItem != null) {
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
                        Annotation fieldQualifier = ReflectUtils.findFirstQualifier(field);
                        Provider provider = getProvider(fieldType, fieldQualifier);

                        Object impl = provider.get();
                        provider.retain(target, field);

                        ReflectUtils.setField(target, field, impl);

                        boolean firstTimeInject = provider.getReferenceCount() == 1;
                        boolean visited = isFieldVisited(target, targetField, field);
                        if (!visited) {
                            doInject(impl, field, fieldType, fieldQualifier, injectAnnotation);
                        }

                        if (firstTimeInject) {
                            provider.notifyInjected(impl);
                        }

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
     * Release cached instances held by fields of target object. References of cache of the
     * instances will be decremented. Once the reference count of a controller reaches 0, it will
     * be removed from the cache and raise {@link OnFreedListener}.
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
                        if(fieldValue != null) {
                            final Class<?> fieldType = field.getType();
                            Annotation fieldQualifier = ReflectUtils.findFirstQualifier(field);
                            Provider provider = getProvider(fieldType, fieldQualifier);

                            boolean stillReferenced = provider.getReferenceCount(target, field) > 0;
                            boolean fieldVisited = isFieldVisited(target, targetField, field);
                            if (!fieldVisited && stillReferenced) {
                                recordVisitField(target, targetField, field);
                                doRelease(fieldValue, field, fieldType, fieldQualifier, injectAnnotation);

                                provider.release(target, field);

                                checkToFreeProvider(provider);
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

    private void checkToFreeProvider(Provider provider) {
        if (provider.getReferenceCount() == 0) {
            if (onProviderFreedListeners != null) {
                int listenerSize = onProviderFreedListeners.size();
                for (int k = 0; k < listenerSize; k++) {
                    onProviderFreedListeners.get(k).onFreed(provider);
                }
            }

            provider.freeCache();
        }
    }

    /**
     * Records the field of a target object is visited
     * @param object The field holder
     * @param objectField The field which holds the object in its parent
     * @param field The field of the holder
     */
    private void recordVisitField(Object object, Field objectField, Field field) {
        Map<String, Set<String>> bag = visitedFields.get(object);
        if (bag == null) {
            bag = new HashMap<>();
            visitedFields.put(object, bag);
        }
        Set<String> fields = bag.get(objectField);

        String objectFiledKey = objectField == null ? "" : objectField.toGenericString();

        if(fields == null) {
            fields = new HashSet<>();
            bag.put(objectFiledKey, fields);
        }
        fields.add(field.toGenericString());
    }

    /**
     * Indicates whether the field of a target object is visited
     * @param object The field holder
     * @param objectField The field which holds the object in its parent
     * @param field The field of the holder
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
        String key = makeCircularRecordKey(classType, qualifier);
        boolean circularVisitDetected = visitedInjectNodes.contains(key);
        if (!circularVisitDetected) {
            visitedInjectNodes.add(key);
        } else {
            revisitedNode = key;
        }
        return circularVisitDetected;
    }

    private void unrecordVisit(Class classType, Annotation qualifier) {
        String key = makeCircularRecordKey(classType, qualifier);
        visitedInjectNodes.remove(key);
    }

    private String makeCircularRecordKey(Class classType, Annotation qualifier) {
        return classType.getName() + "@" + ((qualifier == null) ? "NoQualifier" : qualifier.toString());
    }

    Provider getProvider(Class type, Annotation qualifier) throws ProviderMissingException {
        //Try finder cache first. If not found try to cache it.
        Provider provider = null;
        ProviderFinder providerFinder = finderCache.get(type);
        if (providerFinder == null) {
            int count = providerFinders.size();
            for (int i = 0; i < count; i++) {
                providerFinder = providerFinders.get(i);
                provider = providerFinder.findProvider(type, qualifier);
                if (provider != null) {
                    finderCache.put(type, providerFinder);
                    return provider;
                }
            }
        } else {
            provider = providerFinder.findProvider(type, qualifier);
        }

        if (provider == null) {
            throw new ProviderMissingException(type, qualifier);
        }

        return provider;
    }

    /**
     * Print readable circular graph
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
         * @param target The object to inject into
         */
        void onInject(Object target);

        /**
         * Called when the graph is about to release dependencies from the given object
         * @param target The object to release
         */
        void onRelease(Object target);
    }

}
