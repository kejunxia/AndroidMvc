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

package com.shipdream.lib.android.mvp;

import com.shipdream.lib.android.mvp.event.bus.EventBus;
import com.shipdream.lib.android.mvp.event.bus.annotation.EventBusC;
import com.shipdream.lib.android.mvp.event.bus.annotation.EventBusV;
import com.shipdream.lib.android.mvp.event.bus.internal.EventBusImpl;
import com.shipdream.lib.poke.Consumer;
import com.shipdream.lib.poke.Graph;
import com.shipdream.lib.poke.Provider;
import com.shipdream.lib.poke.Provider.OnFreedListener;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.ScopeCache;
import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * {@link Mvp} injects instances and all its nested dependencies to target object
 * recursively. By default, all injected instances and their dependencies that are located by
 * naming convention will be <b>SINGLETON</b>. It can also register custom injections by
 * {@link #register(Module)}.
 * <p/>
 * Priority of finding implementation of contract is by<br>
 * <ol>
 * <li>Registered implementation by {@link #register(Module)}</li>
 * <li>When the injecting type is an interface: Implementation for interface a.b.c.SomeContract
 * should be named as a.b.c.internal.SomeContractImpl. Interface: a.b.c.SomeContract -->
 * a.b.c.<b>internal</b>.SomeContract<b>Impl</b></li>
 * <li>When the injecting type is a concrete class type with empty constructor: implementation is
 * itself. Therefore a new instance of itself will be created for the injection.</li>
 * <li>Otherwise, errors will occur</li>
 * </ol>
 * <p/>
 * As described above, explicit implementation can be registered by {@link #register(Module)}.
 * Once an implementation is registered, it will override the default auto implementation locating
 * described above. This would be handy in unit testing where if partial of real implementations
 * are wanted to be used and the other are mocks.<br>
 * <p/>
 * <p>Note that, <b>qualifier will be ignore for dependencies injected by naming convention
 * strategy</b>, though qualifier of provide methods of registered {@link Module} will still
 * be taken into account.
 * <p/>
 */
public class Mvp {
    public static class Exception extends RuntimeException {
        public Exception(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private Logger logger = LoggerFactory.getLogger(getClass());
    MvpComponent appProviderFinder;

    //Composite graph to hide methods
    Graph graph;

    public Mvp(BaseDependencies baseDependencies)
            throws ProvideException, ProviderConflictException {

        appProviderFinder = new MvpComponent(new ScopeCache());
        appProviderFinder.register(new __Module(baseDependencies));

        graph = new SimpleGraph(appProviderFinder);

        graph.registerProviderFreedListener(new OnFreedListener() {
            @Override
            public void onFreed(Provider provider) {
                Object obj = provider.findCachedInstance();

                if (obj != null) {
                    //When the cached instance is still there free and dispose it.
                    if (obj instanceof Bean) {
                        Bean bean = (Bean) obj;
                        bean.onDisposed();
                        appProviderFinder.beans.remove(obj);

                        logger.trace("--MvpBean freed - '{}'.",
                                obj.getClass().getSimpleName());
                    }
                }
            }

        });
    }

    //TODO: improve this
    /**
     * For testing to hijack the cache
     * @param singletonScopeCache the cache to hijack
     */
    void hijack(ScopeCache singletonScopeCache) {
        appProviderFinder.scopeCache = singletonScopeCache;
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
     * Reference an injectable object and retain it. Use
     * {@link #dereference(Object, Class, Annotation)} to dereference it when it's not used
     * any more.
     * @param type the type of the object
     * @param qualifier the qualifier
     * @return
     */
    public <T> T reference(Class<T> type, Annotation qualifier)
            throws ProviderMissingException, ProvideException, CircularDependenciesException {
        return graph.reference(type, qualifier, Inject.class);
    }

    /**
     * Dereference an injectable object. When it's not referenced by anything else after this
     * dereferencing, release its cached instance if possible.
     * @param type the type of the object
     * @param qualifier the qualifier
     */
    public <T> void dereference(T instance, Class<T> type, Annotation qualifier)
            throws ProviderMissingException {
        graph.dereference(instance, type, qualifier, Inject.class);
    }

    /**
     * Same as {@link #use(Class, Annotation, Consumer)} except using un-qualified injectable type.
     * @param type The type of the injectable instance
     * @param consumer Consume to use the instance
     */
    public <T> void use(final Class<T> type, final Consumer<T> consumer) {
        try {
            graph.use(type, Inject.class, consumer);
        } catch (PokeException e) {
            throw new Exception(e.getMessage(), e);
        }
    }

    /**
     * Use an injectable instance in the scope of {@link Consumer#consume(Object)} without injecting
     * it as a field of an object. This method will automatically retain the instance before
     * {@link Consumer#consume(Object)} is called and released after it's returned. As a result,
     * it doesn't hold the instance like the field marked by {@link Inject} that will retain the
     * reference of the instance until {@link #release(Object)} is called. However, in the
     * scope of {@link Consumer#consume(Object)} the instance will be held.
     *
     * <p>For example,</p>
     * <pre>
        interface Os {
        }

        static class DeviceComponent extends Component {
            @Provides
            @Singleton
            public Os provide() {
                return new Os(){
                };
            }
        }
    
        class Device {
            @Inject
            private Os os;
        }

        mvpGraph.register(new DeviceComponent());

        //OsReferenceCount = 0
        mvpGraph.use(Os.class, null, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //First time to create the instance.
                //OsReferenceCount = 1
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 0

        final Device device = new Device();
        mvpGraph.inject(device);  //OsReferenceCount = 1
        //New instance created and cached

        mvpGraph.use(Os.class, null, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //Since reference count is greater than 0, cached instance will be reused
                //OsReferenceCount = 2
                Assert.assertTrue(device.os == instance);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 1

        mvpGraph.release(device);  //OsReferenceCount = 0
        //Last instance released, so next time a new instance will be created

        mvpGraph.use(Os.class, null, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //OsReferenceCount = 1
                //Since the cached instance is cleared, the new instance is a newly created one.
                Assert.assertTrue(device.os != instance);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 0

        mvpGraph.use(Os.class, null, new Consumer<Os>() {
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

        mvpGraph.use(Os.class, null, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //OsReferenceCount = 1
                mvpGraph.inject(device);
                //Injection will reuse the cached instance and increment the reference count
                //OsReferenceCount = 2

                //Since the cached instance is cleared, the new instance is a newly created one.
                Assert.assertTrue(device.os == instance);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 1

        mvpGraph.release(device);  //OsReferenceCount = 0
     * </pre>
     *
     * <p><b>Note that, if navigation is involved in {@link Consumer#consume(Object)}, though the
     * instance injected is still held until consume method returns, the injected instance may
     * loose its model when the next fragment is loaded. This is because Android doesn't load
     * fragment immediately by fragment manager, instead navigation will be done in the future main
     * loop. Therefore, if the model of an injected instance needs to be carried to the next fragment
     * navigated to, use {@link NavigationManager#navigate(Object)}.{@link Navigator#with(Class, Annotation, Preparer)}</b></p>
     *
     * @param type The type of the injectable instance
     * @param qualifier Qualifier for the injectable instance
     * @param consumer Consume to use the instance
     * @throws Exception throw when there are exceptions during the consumption of the instance
     */
    public <T> void use(final Class<T> type, final Annotation qualifier, final Consumer<T> consumer) {
        try {
            graph.use(type, qualifier, Inject.class, consumer);
        } catch (PokeException e) {
            throw new Exception(e.getMessage(), e);
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
        } catch (PokeException e) {
            throw new Exception(e.getMessage(), e);
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
            throw new Exception(e.getMessage(), e);
        }
    }

    /**
     * Register all providers listed by the {@link Module}
     *
     * @param module The component
     */
    public void register(Module module) {
        try {
            appProviderFinder.register(module);
        } catch (java.lang.Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Unregister all providers listed by the {@link Module}
     *
     * @param module The component
     */
    public void unregister(Module module) {
        appProviderFinder.unregister(module);
    }

    /**
     * Dependencies for all controllers
     */
    public abstract static class BaseDependencies {
        /**
         * Create a new instance of EventBus for events among controllers. This event bus will be
         * injected into fields annotated by {@link EventBusC}.
         *
         * @return The event bus
         */
        protected EventBus createEventBusC() {
            return new EventBusImpl();
        }

        /**
         * Create a new instance of EventBus for events posted to views. This event bus
         * will be injected into fields annotated by {@link EventBusV}.
         *
         * @return The event bus
         */
        protected EventBus createEventBusV() {
            return new EventBusImpl();
        }

        /**
         * Create a new instance of ExecutorService to support
         * {@link AbstractPresenter#runTask(Object, Task)}. To run tasks really
         * asynchronously by calling {@link AbstractPresenter#runTask(Object, Task)}, an
         * {@link ExecutorService} runs tasks on threads different from the caller of
         * {@link AbstractPresenter#runTask(Object, Task)} is needed. However, to provide
         * a {@link ExecutorService} runs tasks on the same thread would be handy for testing. For
         * example, network responses can be mocked to return immediately.
         *
         * @return The {@link ExecutorService} controls on which threads tasks sent to
         * {@link AbstractPresenter#runTask(Object, Task)} will be running on.
         */
        protected abstract ExecutorService createExecutorService();
    }

    /**
     * Internal use. Do use this in your code.
     */
    public class __Module {
        private final BaseDependencies baseDependencies;

        public __Module(BaseDependencies baseDependencies) {
            super(appProviderFinder.scopeCache);
            this.baseDependencies = baseDependencies;
        }

        @Provides
        @EventBusC
        @Singleton
        public EventBus providesEventBusC() {
            return baseDependencies.createEventBusC();
        }

        @Provides
        @EventBusV
        @Singleton
        public EventBus providesEventBusV() {
            return baseDependencies.createEventBusV();
        }

        @Provides
        @Singleton
        public ExecutorService providesExecutorService() {
            return baseDependencies.createExecutorService();
        }
    }

}
