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

import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * {@link AndroidMvp} will generate a default {@link MvpGraph} for injection. To replace
 * {@link MvpGraph.BaseDependencies} use {@link Injector#configGraph(MvpGraph.BaseDependencies)}.
 * By default, the graph uses naming convention to locate the implementations of dependencies. See
 * {@link MvpGraph} how it works.
 */
public class AndroidMvp {
    private static class DefaultPresenterDependencies extends MvpGraph.BaseDependencies {
        private static ExecutorService sNetworkExecutorService;
        private final static String BACKGROUND_THREAD_NAME = "AndroidMvpDefaultBackgroundThread";

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

    static final String MVP_SATE_PREFIX = "__android.mvp.state:";

    static {
        Injector.configGraph(new DefaultPresenterDependencies());
    }

    private AndroidMvp() {
    }

    /**
     * The graph to inject dependencies for mvp components.
     * @return The {@link MvpGraph}
     */
    public static MvpGraph graph() {
        return Injector.getGraph();
    }

    /**
     * Set the custom state keeper for the objects that are wanted to be saved and restored by it.
     * Other objects will still be saved and restored by json serialization.
     * @param customStateKeeper The State keeper use {@link Parcelable} to save and restore state.
     *                          If any state doesn't need to be managed by this state keeper return
     *                          null in its {@link AndroidModelKeeper#saveModel(Object, Class)} and
     *                          {@link AndroidModelKeeper#getModel(Parcelable, Class)}
     */
    public static void setCustomStateKeeper(AndroidModelKeeper customStateKeeper) {
        ModelKeeperHolder.stateKeeper.customModelKeeper = customStateKeeper;
    }

}
