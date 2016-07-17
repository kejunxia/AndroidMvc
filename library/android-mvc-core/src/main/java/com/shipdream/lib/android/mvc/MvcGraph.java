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

package com.shipdream.lib.android.mvc;

import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Consumer;
import com.shipdream.lib.poke.Graph;
import com.shipdream.lib.poke.Provider;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

import javax.inject.Inject;

public class MvcGraph {
    final Logger logger = LoggerFactory.getLogger(getClass());

    UiThreadRunner uiThreadRunner;
    Graph graph;

    {
        uiThreadRunner = new UiThreadRunner() {
            @Override
            public boolean isOnUiThread() {
                return true;
            }

            @Override
            public void post(Runnable runnable) {
                runnable.run();
            }

            @Override
            public void postDelayed(Runnable runnable, long delayMs) {
                runnable.run();
            }
        };

        graph = new Graph();
        try {
            graph.setRootComponent(new MvcComponent("MvcRootComponent"));
            graph.getRootComponent().register(new Object() {
                @Provides
                public UiThreadRunner uiThreadRunner() {
                    return uiThreadRunner;
                }
            });
        } catch (Graph.IllegalRootComponentException e) {
            throw new RuntimeException(e);
        } catch (ProvideException e) {
            throw new RuntimeException(e);
        } catch (ProviderConflictException e) {
            throw new RuntimeException(e);
        }
        graph.registerDisposeListener(new Provider.DisposeListener() {
            @Override
            public <T> void onDisposed(Provider<T> provider, T instance) {
                if (instance != null && instance instanceof Bean) {
                    //When the cached instance is still there free and dispose it.
                    Bean bean = (Bean) instance;
                    bean.onDestroy();

                    logger.trace("---Bean destroyed - '{}'.",
                            provider.type().getSimpleName());
                }
            }
        });
    }

    /**
     * Register {@link Graph.Monitor} which will be called the graph is about to inject or release an object
     *
     * @param monitor The monitor
     */
    public void registerMonitor(Graph.Monitor monitor) {
        if (!uiThreadRunner.isOnUiThread()) {
            throw new MvcGraphException("Cannot register mvc graph monitor from Non-UiThread");
        }
        graph.registerMonitor(monitor);
    }

    /**
     * Register {@link Graph.Monitor} which will be called the graph is about to inject or release an object
     *
     * @param monitor The monitor
     */
    public void unregisterMonitor(Graph.Monitor monitor) {
        if (!uiThreadRunner.isOnUiThread()) {
            throw new MvcGraphException("Cannot unregister mvc graph monitor from Non-UiThread");
        }
        graph.unregisterMonitor(monitor);
    }

    /**
     * Clear {@link Graph.Monitor} which will be called the graph is about to inject or release an object
     */
    public void clearMonitors() {
        if (!uiThreadRunner.isOnUiThread()) {
            throw new MvcGraphException("Cannot clear mvc graph monitors from Non-UiThread");
        }
        graph.clearMonitors();
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
        if (!uiThreadRunner.isOnUiThread()) {
            throw new MvcGraphException("Cannot reference an instance from Non-UiThread");
        }
        return graph.reference(type, qualifier, Inject.class);
    }

    /**
     * Dereference an injectable object. When it's not referenced by anything else after this
     * dereferencing, release its cached instance if possible.
     * @param instance the instance is to release
     * @param type the type of the object
     * @param qualifier the qualifier
     */
    public <T> void dereference(T instance, Class<T> type, Annotation qualifier)
            throws ProviderMissingException {
        if (!uiThreadRunner.isOnUiThread()) {
            throw new MvcGraphException("Cannot dereference an instance from Non-UiThread");
        }
        graph.dereference(instance, type, qualifier, Inject.class);
    }

    /**
     * Same as {@link #use(Class, Annotation, Consumer)} except using un-qualified injectable type.
     * @param type The type of the injectable instance
     * @param consumer Consume to use the instance
     */
    public <T> void use(final Class<T> type, final Consumer<T> consumer) {
        if (!uiThreadRunner.isOnUiThread()) {
            throw new MvcGraphException("Cannot use an instance from Non-UiThread");
        }
        try {
            graph.use(type, Inject.class, consumer);
        } catch (PokeException e) {
            throw new MvcGraphException(e.getMessage(), e);
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

     mvcGraph.register(new DeviceComponent());

     //OsReferenceCount = 0
     mvcGraph.use(Os.class, null, new Consumer<Os>() {
    @Override
    public void consume(Os instance) {
    //First time to create the instance.
    //OsReferenceCount = 1
    }
    });
     //Reference count decremented by use method automatically
     //OsReferenceCount = 0

     final Device device = new Device();
     mvcGraph.inject(device);  //OsReferenceCount = 1
     //New instance created and cached

     mvcGraph.use(Os.class, null, new Consumer<Os>() {
    @Override
    public void consume(Os instance) {
    //Since reference count is greater than 0, cached instance will be reused
    //OsReferenceCount = 2
    Assert.assertTrue(device.os == instance);
    }
    });
     //Reference count decremented by use method automatically
     //OsReferenceCount = 1

     mvcGraph.release(device);  //OsReferenceCount = 0
     //Last instance released, so next time a new instance will be created

     mvcGraph.use(Os.class, null, new Consumer<Os>() {
    @Override
    public void consume(Os instance) {
    //OsReferenceCount = 1
    //Since the cached instance is cleared, the new instance is a newly created one.
    Assert.assertTrue(device.os != instance);
    }
    });
     //Reference count decremented by use method automatically
     //OsReferenceCount = 0

     mvcGraph.use(Os.class, null, new Consumer<Os>() {
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

     mvcGraph.use(Os.class, null, new Consumer<Os>() {
    @Override
    public void consume(Os instance) {
    //OsReferenceCount = 1
    mvcGraph.inject(device);
    //Injection will reuse the cached instance and increment the reference count
    //OsReferenceCount = 2

    //Since the cached instance is cleared, the new instance is a newly created one.
    Assert.assertTrue(device.os == instance);
    }
    });
     //Reference count decremented by use method automatically
     //OsReferenceCount = 1

     mvcGraph.release(device);  //OsReferenceCount = 0
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
     * @throws MvcGraphException throw when there are exceptions during the consumption of the instance
     */
    public <T> void use(final Class<T> type, final Annotation qualifier, final Consumer<T> consumer) {
        if (!uiThreadRunner.isOnUiThread()) {
            throw new MvcGraphException("Cannot use an instance from Non-UiThread");
        }
        try {
            graph.use(type, qualifier, Inject.class, consumer);
        } catch (PokeException e) {
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
        if (!uiThreadRunner.isOnUiThread()) {
            throw new MvcGraphException("Cannot inject an instance from Non-UiThread");
        }
        try {
            graph.inject(target, Inject.class);
        } catch (PokeException e) {
            throw new MvcGraphException(e.getMessage(), e);
        }
    }

    /**
     * Release cached instances held by fields of target object. References of instances of the
     * instances will be decremented. Once the reference count of a contract type reaches 0, it will
     * be removed from the instances.
     *
     * @param target of which the object fields will be released.
     */
    public void release(Object target) {
        if (!uiThreadRunner.isOnUiThread()) {
            throw new MvcGraphException("Cannot release an instance from Non-UiThread");
        }
        try {
            graph.release(target, Inject.class);
        } catch (ProviderMissingException e) {
            throw new MvcGraphException(e.getMessage(), e);
        }
    }

    /**
     * Add {@link Component} to the graph.
     *
     * @param component The root {@link Component} of this graph.
     */
    public void setRootComponent(MvcComponent component) throws Graph.IllegalRootComponentException {
        if (!uiThreadRunner.isOnUiThread()) {
            throw new MvcGraphException("Cannot set root component from Non-UiThread");
        }
        graph.setRootComponent(component);
    }

    public MvcComponent getRootComponent() {
        if (!uiThreadRunner.isOnUiThread()) {
            throw new MvcGraphException("Cannot getRootComponent() from Non-UiThread");
        }
        return (MvcComponent) graph.getRootComponent();
    }

    /**
     * Register {@link Provider.DereferenceListener} which will be called when the provider
     *
     * @param onProviderFreedListener The listener
     */
    public void registerDereferencedListener(Provider.DereferenceListener onProviderFreedListener) {
        if (!uiThreadRunner.isOnUiThread()) {
            throw new MvcGraphException("Cannot register dereference listener from Non-UiThread");
        }
        graph.registerDereferencedListener(onProviderFreedListener);
    }

    /**
     * Unregister {@link Provider.DereferenceListener} which will be called when the last cached
     * instance of an injected contract is freed.
     *
     * @param onProviderFreedListener The listener
     */
    public void unregisterDereferencedListener(Provider.DereferenceListener onProviderFreedListener) {
        if (!uiThreadRunner.isOnUiThread()) {
            throw new MvcGraphException("Cannot unregister dereference listener from Non-UiThread");
        }
        graph.unregisterDereferencedListener(onProviderFreedListener);
    }

    /**
     * Clear {@link Provider.DereferenceListener}s which will be called when the last cached
     * instance of an injected contract is freed.
     */
    public void clearDereferencedListeners() {
        if (!uiThreadRunner.isOnUiThread()) {
            throw new MvcGraphException("Cannot clear dereference listeners from Non-UiThread");
        }
        graph.clearDereferencedListeners();
    }
}
