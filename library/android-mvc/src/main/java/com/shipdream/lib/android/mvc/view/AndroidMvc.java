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

package com.shipdream.lib.android.mvc.view;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.shipdream.lib.android.mvc.MvcGraph;
import com.shipdream.lib.android.mvc.StateKeeper;
import com.shipdream.lib.android.mvc.StateManaged;
import com.shipdream.lib.android.mvc.controller.BaseController;
import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.android.mvc.controller.internal.AndroidPosterImpl;
import com.shipdream.lib.android.mvc.event.BaseEventV2V;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC2V;
import com.shipdream.lib.android.mvc.event.bus.internal.EventBusImpl;
import com.shipdream.lib.poke.exception.PokeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.inject.Inject;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * {@link AndroidMvc} will generate a default {@link MvcGraph} for injection. To replace
 * {@link MvcGraph.BaseDependencies} use {@link #configGraph(MvcGraph.BaseDependencies)}.
 * By default, the graph uses naming convention to locate the implementations of dependencies. See
 * {@link MvcGraph} how it works.
 */
public class AndroidMvc {
    private static class EventBusC2VHolder {
        @Inject
        @EventBusC2V
        EventBus eventBusC2V;
    }

    static final String MVC_SATE_PREFIX = "__--AndroidMvc:State:";
    static final String FRAGMENT_TAG_PREFIX = "__--AndroidMvc:Fragment:";
    private static MvcGraph mvcGraph;
    private static EventBusC2VHolder eventBusC2VHolder;
    private static EventBus eventBusV2V;
    private static DefaultStateKeeper sStateManager;

    static {
        configGraph(new DefaultControllerDependencies());
        sStateManager = new DefaultStateKeeper();
        eventBusV2V = new EventBusImpl();

        AndroidPosterImpl.init();
    }

    private AndroidMvc() {
    }

    /**
     * The graph to inject dependencies for mvc components.
     * @return The {@link MvcGraph}
     */
    public static MvcGraph graph() {
        return mvcGraph;
    }

    /**
     * Config the {@link MvcGraph} by custom dependencies.
     * <p>Note that, the graph will be regenerated after config. In addition, it's cached instances
     * will be regenerated such as cached singletons.</p>
     *
     * @param baseDependencies The dependencies of all controllers
     */
    public static void configGraph(MvcGraph.BaseDependencies baseDependencies) {
        try {
            mvcGraph = new MvcGraph(baseDependencies);
            eventBusC2VHolder = new EventBusC2VHolder();
            mvcGraph.inject(eventBusC2VHolder);
        } catch (PokeException e) {
            throw new RuntimeException(e);
        }
    }

    static void saveStateOfAllControllers(Bundle outState) {
        sStateManager.bundle = outState;
        mvcGraph.saveAllStates(sStateManager);
        sStateManager.bundle = null;
    }

    static void restoreStateOfAllControllers(Bundle savedState) {
        sStateManager.bundle = savedState;
        mvcGraph.restoreAllStates(sStateManager);
        sStateManager.bundle = null;
    }

    static void saveControllerStateOfTheirOwn(Bundle outState, Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        sStateManager.bundle = outState;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (BaseController.class.isAssignableFrom(field.getType())) {
                StateManaged stateManaged = null;
                try {
                    field.setAccessible(true);
                    stateManaged = (StateManaged) field.get(object);
                } catch (IllegalAccessException e) {
                    //ignore
                }
                sStateManager.saveState(stateManaged.getState(), stateManaged.getStateType());
            }
        }
    }

    static void restoreControllerStateByTheirOwn(Bundle savedState, Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        sStateManager.bundle = savedState;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (BaseController.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    StateManaged stateManaged = (StateManaged) field.get(object);
                    Object value = sStateManager.getState(stateManaged.getStateType());
                    stateManaged.restoreState(value);
                } catch (IllegalAccessException e) {
                    //ignore
                }
            }
        }
    }

    /**
     * Gets Controllers to Views event bus. Internal use by AndroidMvc library only.
     * @return The event bus
     */
    static EventBus getEventBusC2V() {
        return eventBusC2VHolder.eventBusC2V;
    }

    /**
     * Gets the views to views event bus. By default all {@link MvcFragment}s,
     * {@link MvcDialogFragment} and {@link MvcService} have registered to this event bus. They
     * also have short cut methods to post {@link BaseEventV2V}. Custom views which need to use the
     * event bus must register to this event respectively..
     * @return The event bus
     */
    public static EventBus getEventBusV2V() {
        return eventBusV2V;
    }

    /**
     * Set the custom state keeper for the objects that are wanted to be saved and restored by it.
     * Other objects will still be saved and restored by json serialization.
     * @param customStateKeeper The State keeper use {@link Parcelable} to save and restore state.
     *                          If any state doesn't need to be managed by this state keeper return
     *                          null in its {@link AndroidStateKeeper#saveState(Object, Class)} and
     *                          {@link AndroidStateKeeper#getState(Parcelable, Class)}
     */
    public static void setCustomStateKeeper(AndroidStateKeeper customStateKeeper) {
        sStateManager.customStateKeeper = customStateKeeper;
    }

    private static class DefaultControllerDependencies extends MvcGraph.BaseDependencies {
        private static ExecutorService sNetworkExecutorService;
        private final static String BACKGROUND_THREAD_NAME = "AndroidMvcDefaultBackgroundThread";

        @Override
        public ExecutorService createExecutorService() {
            if (sNetworkExecutorService == null) {
                sNetworkExecutorService = Executors.newFixedThreadPool(10, new ThreadFactory() {
                    @Override
                    public Thread newThread(final @NonNull Runnable r) {
                        return new Thread(new Runnable() {
                            @Override
                            public void run() {
                                android.os.Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
                                r.run();
                            }
                        }, BACKGROUND_THREAD_NAME);
                    }
                });
            }
            return sNetworkExecutorService;
        }
    }

    private static class DefaultStateKeeper implements StateKeeper {
        private static Gson gson;
        private Bundle bundle;
        private Logger logger = LoggerFactory.getLogger(getClass());
        private AndroidStateKeeper navigationModelKeeper = new NavigationModelKeeper();
        private AndroidStateKeeper customStateKeeper;

        private DefaultStateKeeper() {
            gson = new GsonBuilder().create();
        }

        private static String getStateKey(String stateTypeName) {
            return MVC_SATE_PREFIX + stateTypeName;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> void saveState(T state, Class<T> type) {
            if (type != null) {
                Parcelable parcelable = null;

                if (NavigationController.Model.class == type) {
                    //Use navigation model keeper to save state
                    parcelable = navigationModelKeeper.saveState(state, type);
                } else {
                    if (customStateKeeper != null) {
                        //Use customs state manager to restore state
                        parcelable = customStateKeeper.saveState(state, type);
                    }
                }

                long ts = System.currentTimeMillis();
                if (parcelable != null) {
                    String stateKey = getStateKey(type.getName());
                    bundle.putParcelable(stateKey, parcelable);
                    logger.trace("Save state by parcel state keeper - {}, {}ms used.",
                            type.getName(), System.currentTimeMillis() - ts);
                } else {
                    //Use Gson to restore state
                    String stateKey = getStateKey(type.getName());
                    String json = gson.toJson(state);
                    bundle.putString(stateKey, json);

                    logger.trace("Save state by JSON - {}, {}ms used. Content: {}",
                            type.getName(), System.currentTimeMillis() - ts, json);
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getState(Class<T> type) {
            T state = null;
            if (type != null) {
                long ts = System.currentTimeMillis();
                String stateKey = getStateKey(type.getName());
                Object value = bundle.get(stateKey);
                Parcelable parcelable = null;
                if (value instanceof Parcelable) {
                    parcelable = (Parcelable) value;
                }
                if (NavigationController.Model.class == type) {
                    //Use navigation model keeper to restore state
                    state = (T) navigationModelKeeper.getState(parcelable, type);
                    logger.trace("Restore state by parcel state keeper - {}, {}ms used.",
                            type.getName(), System.currentTimeMillis() - ts);
                } else {
                    //Use custom state keeper or gson state keeper to restore state.
                    if (customStateKeeper != null) {
                        if (parcelable != null) {
                            //Use custom state manager to restore state
                            state = (T) customStateKeeper.getState(parcelable, type);
                            logger.trace("Restore state by parcel state keeper - {}, {}ms used.",
                                    type.getName(), System.currentTimeMillis() - ts);
                        }
                    }
                    if (state == null) {
                        //State is not restored successfully by custom state keeper nor navigation
                        //model keeper. So try to use Gson to restore state
                        state = deserialize(bundle, type);
                    }
                }

                if (state == null) {
                    throw new IllegalStateException("Can't find restore state for " + type.getName());
                }
            }
            return state;
        }

        private <T> T deserialize(Bundle outState, Class<T> type) {
            T state;
            long ts = System.currentTimeMillis();

            String stateKey = getStateKey(type.getName());
            String json = outState.getString(stateKey);
            try {
                //recover the model
                state = gson.fromJson(json, type);
                //rebind the model to the controller
            } catch (JsonSyntaxException exception) {
                String errorMessage = String.format(
                        "Failed to restore state(%s) by json deserialization", type.getName());
                throw new RuntimeException(errorMessage, exception);
            }

            logger.trace("Restore state by JSON - {}, {}ms used.",
                    type.getName(), System.currentTimeMillis() - ts);

            return state;
        }

    }
}
