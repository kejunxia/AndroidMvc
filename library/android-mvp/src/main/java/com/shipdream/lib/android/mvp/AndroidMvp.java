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

import com.shipdream.lib.android.mvp.event.bus.EventBus;
import com.shipdream.lib.android.mvp.event.bus.annotation.EventBusC;
import com.shipdream.lib.android.mvp.event.bus.annotation.EventBusV;
import com.shipdream.lib.android.mvp.event.bus.internal.EventBusImpl;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.PokeException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * {@link AndroidMvp} will generate a default {@link Mvp} for injection. To replace
 * {@link Mvp.BaseDependencies} use {@link Mvp#configGraph(Mvp.BaseDependencies)}.
 * By default, the graph uses naming convention to locate the implementations of dependencies. See
 * {@link Mvp} how it works.
 */
public class AndroidMvp {
    static final String MVP_SATE_PREFIX = "__android.graph.state:";

    static {
        try {
            Mvp.graph().getRootComponent().register(new Object() {
                private final static String BACKGROUND_THREAD_NAME = "MvpBackgroundThread";

                @Provides
                @EventBusC
                public EventBus providesEventBusC() {
                    return new EventBusImpl();
                }

                @Provides
                @EventBusV
                public EventBus providesEventBusV() {
                    return new EventBusImpl();
                }

                @Provides
                public ExecutorService providesExecutorService() {
                    return Executors.newFixedThreadPool(10, new ThreadFactory() {
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
            });
        } catch (PokeException e) {
            throw new RuntimeException(e);
        }
    }

    private AndroidMvp() {
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
