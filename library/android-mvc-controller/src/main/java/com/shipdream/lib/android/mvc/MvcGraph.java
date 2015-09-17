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

package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.controller.BaseController;
import com.shipdream.lib.android.mvc.controller.internal.AsyncTask;
import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC2C;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC2V;
import com.shipdream.lib.android.mvc.event.bus.internal.EventBusImpl;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Graph;
import com.shipdream.lib.poke.ImplClassLocator;
import com.shipdream.lib.poke.ImplClassLocatorByPattern;
import com.shipdream.lib.poke.ImplClassNotFoundException;
import com.shipdream.lib.poke.Provider;
import com.shipdream.lib.poke.ProviderByClassType;
import com.shipdream.lib.poke.ProviderFinderByRegistry;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.ScopeCache;
import com.shipdream.lib.poke.SimpleGraph;
import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;
import com.shipdream.lib.poke.Provider.OnFreedListener;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * {@link MvcGraph} injects instances and all its nested dependencies to target object
 * recursively. By default, all injected instances and their dependencies that are located by
 * naming convention will be <b>SINGLETON</b>. It can also register custom injections by
 * {@link #register(Component)}.
 * <p/>
 * Priority of finding implementation of contract is by<br>
 * <ol>
 * <li>Registered implementation by {@link #register(Component)}</li>
 * <li>General class name mapping strategy. Implementation for interface a.b.c.SomeContract
 * should be named as a.b.c.internal.SomeContractImpl. Interface: a.b.c.SomeContract -->
 * a.b.c.<b>internal</b>.SomeContract<b>Impl</b></li>
 * </ol>
 * <p/>
 * As described above, explicit implementation can be registered by {@link #register(Component)}.
 * Once an implementation is registered, it will override the default auto implementation locating
 * described above. This would be handy in unit testing where if partial of real implementations
 * are wanted to be used and the other are mocks.<br>
 * <p/>
 * <p>Note that, <b>qualifier will be ignore for dependencies injected by naming convention
 * strategy</b>, though qualifier of provide methods of registered {@link Component} will still
 * be taken into account.
 * <p/>
 */
public class MvcGraph {
    ScopeCache singletonScopeCache;
    DefaultProviderFinder defaultProviderFinder;
    List<StateManaged> stateManagedObjects = new ArrayList<>();

    //Composite graph to hide methods
    Graph graph;

    public MvcGraph(BaseDependencies baseDependencies)
            throws ProvideException, ProviderConflictException {
        singletonScopeCache = new ScopeCache();
        defaultProviderFinder = new DefaultProviderFinder(MvcGraph.this);
        defaultProviderFinder.register(new __Component(singletonScopeCache, baseDependencies));

        graph = new SimpleGraph(defaultProviderFinder);

        graph.registerProviderFreedListener(new OnFreedListener() {
            @Override
            public void onFreed(Provider provider) {
                Object obj = provider.findCachedInstance();

                if (obj != null) {
                    //When the cached instance is still there free and dispose it.
                    if (obj instanceof StateManaged) {
                        stateManagedObjects.remove(obj);
                    }

                    if (obj instanceof Disposable) {
                        ((Disposable) obj).onDisposed();
                    }
                }
            }

        });
    }

    /**
     * For testing to hijack the cache
     * @param singletonScopeCache the cache to hijack
     */
    void hijack(ScopeCache singletonScopeCache) {
        this.singletonScopeCache = singletonScopeCache;
    }

    /**
     * Register {@link Graph.Monitor} which will be called the graph is about to inject or release an object
     *
     * @param monitor The monitor
     */
    public void registerMonitor(Graph.Monitor monitor) {
        graph.registerMonitor(monitor);
    }

    /**
     * Register {@link Graph.Monitor} which will be called the graph is about to inject or release an object
     *
     * @param monitor The monitor
     */
    public void unregisterMonitor(Graph.Monitor monitor) {
        graph.unregisterMonitor(monitor);
    }

    /**
     * Clear {@link Graph.Monitor} which will be called the graph is about to inject or release an object
     */
    public void clearMonitors() {
        graph.clearMonitors();
    }

    /**
     * Register {@link OnFreedListener} which will be called when the last cached
     * instance of an injected contract is freed.
     *
     * @param onProviderFreedListener The listener
     */
    public void registerProviderFreedListener(OnFreedListener onProviderFreedListener) {
        graph.registerProviderFreedListener(onProviderFreedListener);
    }

    /**
     * Unregister {@link OnFreedListener} which will be called when the last cached
     * instance of an injected contract is freed.
     *
     * @param onProviderFreedListener The listener
     */
    public void unregisterProviderFreedListener(OnFreedListener onProviderFreedListener) {
        graph.unregisterProviderFreedListener(onProviderFreedListener);
    }

    /**
     * Clear {@link OnFreedListener}s which will be called when the last cached
     * instance of an injected contract is freed.
     */
    public void clearOnProviderFreedListeners() {
        graph.clearOnProviderFreedListeners();
    }

    /**
     * Get an instance matching the type and qualifier. If there is an instance cached, the cached
     * instance will be returned otherwise a new instance will be created.
     *
     * <p>Note that, not like {@link #inject(Object)} (Object)} this method will <b>NOT</b> increment
     * reference count for the injectable object with the same type and qualifier.</p>
     * @param type the type of the object
     * @param qualifier the qualifier of the injected object. Null is allowed if no qualifier is specified
     * @return The cached object or a new instance matching the type and qualifier
     * @throws MvcGraphException throw if exception occurs during getting the instance
     */
    public <T> T get(Class<T> type, Annotation qualifier) {
        try {
            return graph.get(type, qualifier, Inject.class);
        } catch (ProviderMissingException e) {
            throw new MvcGraphException(e.getMessage(), e);
        } catch (ProvideException e) {
            throw new MvcGraphException(e.getMessage(), e);
        } catch (CircularDependenciesException e) {
            throw new MvcGraphException(e.getMessage(), e);
        }
    }

    /**
     * Inject all fields annotated by {@link Inject}. References of controllers will be
     * incremented.
     *
     * @param target The target object whose fields annotated by {@link Inject} will be injected.
     */
    public void inject(Object target) {
        try {
            graph.inject(target, Inject.class);
        } catch (ProvideException e) {
            throw new MvcGraphException(e.getMessage(), e);
        } catch (ProviderMissingException e) {
            throw new MvcGraphException(e.getMessage(), e);
        } catch (CircularDependenciesException e) {
            throw new MvcGraphException(e.getMessage(), e);
        }
    }

    /**
     * Release cached instances held by fields of target object. References of cache of the
     * instances will be decremented. Once the reference count of a contract type reaches 0, it will
     * be removed from the cache.
     *
     * @param target of which the object fields will be released.
     */
    public void release(Object target) {
        try {
            graph.release(target, Inject.class);
        } catch (ProviderMissingException e) {
            throw new MvcGraphException(e.getMessage(), e);
        }
    }

    /**
     * Register all providers listed by the {@link Component}
     *
     * @param component The component
     */
    public void register(Component component) {
        try {
            defaultProviderFinder.register(component);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Unregister all providers listed by the {@link Component}
     *
     * @param component The component
     */
    public void unregister(Component component) {
        defaultProviderFinder.unregister(component);
    }

    /**
     * Save state of all injected objects
     * @param stateKeeper The state keeper to manage the state
     */
    public void saveAllStates(StateKeeper stateKeeper) {
        int size = stateManagedObjects.size();
        for (int i = 0; i < size; i++) {
            StateManaged obj = stateManagedObjects.get(i);
            stateKeeper.saveState(obj.getState(), obj.getStateType());
        }
    }

    /**
     * Restore state of all injected objects
     * @param stateKeeper The state keeper to manage the state
     */
    @SuppressWarnings("unchecked")
    public void restoreAllStates(StateKeeper stateKeeper) {
        int size = stateManagedObjects.size();
        for (int i = 0; i < size; i++) {
            StateManaged obj = stateManagedObjects.get(i);
            Object state = stateKeeper.getState(obj.getStateType());
            if(state != null) {
                stateManagedObjects.get(i).restoreState(state);
            }
        }
    }

    /**
     * Dependencies for all controllers
     */
    public abstract static class BaseDependencies {
        /**
         * Create a new instance of EventBus for events among controllers. This event bus will be
         * injected into fields annotated by {@link EventBusC2C}.
         *
         * @return The event bus
         */
        protected EventBus createEventBusC2C() {
            return new EventBusImpl();
        }

        /**
         * Create a new instance of EventBus for events from controllers to views. This event bus
         * will be injected into fields annotated by {@link EventBusC2V}.
         *
         * @return The event bus
         */
        protected EventBus createEventBusC2V() {
            return new EventBusImpl();
        }

        /**
         * Create a new instance of ExecutorService to support
         * {@link BaseControllerImpl#runAsyncTask(Object, AsyncTask)}. To run tasks really
         * asynchronously by calling {@link BaseControllerImpl#runAsyncTask(Object, AsyncTask)}, an
         * {@link ExecutorService} runs tasks on threads different from the caller of
         * {@link BaseControllerImpl#runAsyncTask(Object, AsyncTask)} is needed. However, to provide
         * a {@link ExecutorService} runs tasks on the same thread would be handy for testing. For
         * example, network responses can be mocked to return immediately.
         *
         * @return The {@link ExecutorService} controls on which threads tasks sent to
         * {@link BaseControllerImpl#runAsyncTask(Object, AsyncTask)} will be running on.
         */
        protected abstract ExecutorService createExecutorService();
    }

    /**
     * Internal use. Do use this in your code.
     */
    public static class __Component extends Component {
        private final BaseDependencies baseDependencies;

        public __Component(ScopeCache scopeCache, BaseDependencies baseDependencies) {
            super(scopeCache);
            this.baseDependencies = baseDependencies;
        }

        @Provides
        @EventBusC2C
        @Singleton
        public EventBus providesIEventBusC2C() {
            return baseDependencies.createEventBusC2C();
        }

        @Provides
        @EventBusC2V
        @Singleton
        public EventBus providesIEventBusC2V() {
            return baseDependencies.createEventBusC2V();
        }

        @Provides
        @Singleton
        public ExecutorService providesExecutorService() {
            return baseDependencies.createExecutorService();
        }
    }

    static class DefaultProviderFinder extends ProviderFinderByRegistry {
        private final MvcGraph mvcGraph;
        private final ImplClassLocator defaultImplClassLocator;
        private Map<Class, Provider> providers = new HashMap<>();

        private DefaultProviderFinder(MvcGraph mvcGraph) {
            this.mvcGraph = mvcGraph;
            defaultImplClassLocator = new ImplClassLocatorByPattern(mvcGraph.singletonScopeCache);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Provider<T> findProvider(Class<T> type, Annotation qualifier) throws ProviderMissingException {
            Provider<T> provider = super.findProvider(type, qualifier);
            if (provider == null) {
                provider = providers.get(type);
                if (provider == null) {
                    try {
                        Class<? extends T> impClass = defaultImplClassLocator.locateImpl(type);
                        provider = new MvcProvider<>(mvcGraph.stateManagedObjects, type, impClass);
                        provider.setScopeCache(defaultImplClassLocator.getScopeCache());
                        providers.put(type, provider);
                    } catch (ImplClassNotFoundException e) {
                        throw new ProviderMissingException(type, qualifier, e);
                    }
                }
            }
            return provider;
        }
    }

    private static class MvcProvider<T> extends ProviderByClassType<T> {
        private List<StateManaged> stateManagedObjects;

        public MvcProvider(List<StateManaged> stateManagedObjects, Class<T> type, Class<? extends T> implementationClass) {
            super(type, implementationClass);
            this.stateManagedObjects = stateManagedObjects;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T createInstance() throws ProvideException {
            final T newInstance = (T) super.createInstance();

            if (newInstance instanceof BaseController) {
                registerOnInjectedListener(new OnInjectedListener() {
                    @Override
                    public void onInjected(Object object) {
                        if (object instanceof BaseController) {
                            BaseController controller = (BaseController) object;
                            controller.init();
                        }
                        unregisterOnInjectedListener(this);
                    }
                });
            }

            if (newInstance instanceof StateManaged) {
                stateManagedObjects.add((StateManaged) newInstance);
            }

            return newInstance;
        }
    }

}
