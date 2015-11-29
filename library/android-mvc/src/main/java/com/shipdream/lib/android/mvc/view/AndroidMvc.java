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

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.shipdream.lib.android.mvc.Injector;
import com.shipdream.lib.android.mvc.MvcGraph;
import com.shipdream.lib.android.mvc.UiThreadRunnerImpl;
import com.shipdream.lib.android.mvc.controller.internal.AndroidPosterImpl;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.internal.EventBusImpl;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Provides;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.inject.Singleton;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * {@link AndroidMvc} will generate a default {@link MvcGraph} for injection. To replace
 * {@link MvcGraph.BaseDependencies} use {@link Injector#configGraph(MvcGraph.BaseDependencies)}.
 * By default, the graph uses naming convention to locate the implementations of dependencies. See
 * {@link MvcGraph} how it works.
 */
public class AndroidMvc {
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

    static class ViewComponent extends Component {
        @Provides
        @EventBusV2V
        @Singleton
        public EventBus providesIEventBusC2V() {
            return new EventBusImpl();
        }
    }

    static {
        Injector.configGraph(new DefaultControllerDependencies());
        Injector.getGraph().register(new ViewComponent());

        AndroidPosterImpl.init();
        UiThreadRunnerImpl.init();
    }

    private AndroidMvc() {
    }

    /**
     * The graph to inject dependencies for mvc components.
     * @return The {@link MvcGraph}
     */
    public static MvcGraph graph() {
        return Injector.getGraph();
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
        DefaultStateKeeperHolder.stateKeeper.customStateKeeper = customStateKeeper;
    }

}
