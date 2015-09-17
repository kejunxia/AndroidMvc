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
    private Map<Object, Set<String>> visitedFields = new HashMap<>();

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
        doInject(target, null, null, injectAnnotation, true);
        visitedInjectNodes.clear();
        revisitedNode = null;
        visitedFields.clear();
    }

    /**
     * Get an instance matching the type and qualifier. If there is an instance cached, the cached
     * instance will be returned otherwise a new instance will be created.
     *
     * <p>Note that, not like {@link #inject(Object, Class)} this method will <b>NOT</b> increment
     * reference count for the injectable object with the same type and qualifier.</p>
     * @param type the type of the object
     * @param qualifier the qualifier of the injected object. Null is allowed if no qualifier is specified
     * @return The cached object or a new instance matching the type and qualifier
     * @throws ProviderMissingException throw if the provider matching the requiredType and qualifier is not found
     * @throws ProvideException throw when failed to create a new instance
     * @throws CircularDependenciesException throw when circular dependency found during injecting the newly created instance
     */
    public <T> T get(Class<T> type, Annotation qualifier, Class<? extends Annotation> injectAnnotation)
            throws ProviderMissingException, ProvideException, CircularDependenciesException {
        Provider<T> provider = getProvider(type, qualifier);
        T cachedInstance = provider.findCachedInstance();
        if (cachedInstance != null) {
            return cachedInstance;
        } else {
            T newInstance = provider.createInstance();

            doInject(newInstance, null, null, injectAnnotation, false);

            return newInstance;
        }
    }

    @SuppressWarnings("unchecked")
    private void doInject(Object target, Class targetType, Annotation targetQualifier,
                          Class<? extends Annotation> injectAnnotation, boolean retainReference)
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

                        Object impl;
                        if (retainReference) {
                            impl = provider.get();
                            provider.retain(target, field);
                        } else {
                            impl = provider.createInstance();
                        }

                        ReflectUtils.setField(target, field, impl);

                        boolean firstTimeInject = provider.totalReference() == 1;
                        if (!isFieldVisited(impl, field)) {
                            doInject(impl, fieldType, fieldQualifier, injectAnnotation, retainReference);
                        }

                        if (firstTimeInject) {
                            provider.notifyInjected(impl);
                        }

                        recordVisitField(impl, field);
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
        doRelease(target, null, null, injectAnnotation);
        visitedInjectNodes.clear();
        revisitedNode = null;
        visitedFields.clear();
    }

    private void doRelease(Object target, Class targetType, Annotation targetQualifier,
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
                            if (!isFieldVisited(target, field) && stillReferenced) {
                                recordVisitField(target, field);
                                doRelease(fieldValue, fieldType, fieldQualifier, injectAnnotation);

                                provider.release(target, field);

                                if (provider.totalReference() == 0) {
                                    if (onProviderFreedListeners != null) {
                                        int listenerSize = onProviderFreedListeners.size();
                                        for (int k = 0; k < listenerSize; k++) {
                                            onProviderFreedListeners.get(k).onFreed(provider);
                                        }
                                    }

                                    provider.freeCache();
                                }
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

    private void recordVisitField(Object object, Field field) {
        Set<String> fields = visitedFields.get(object);
        if(fields == null) {
            fields = new HashSet<>();
            visitedFields.put(object, fields);
        }
        fields.add(field.getName());
    }

    private boolean isFieldVisited(Object object, Field field) {
        Set<String> fields = visitedFields.get(object);
        return fields != null && fields.contains(field.getName());
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
