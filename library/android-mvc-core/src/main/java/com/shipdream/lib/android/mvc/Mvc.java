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

import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusV;
import com.shipdream.lib.android.mvc.event.bus.internal.EventBusImpl;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Mvc {
    static MvcGraph graph;

    /**
     * Get the {@link MvcGraph} managing injectable objects. It comes with a default
     * {@link MvcComponent}.
     * @return
     */
    public static MvcGraph graph() {
        if (graph == null) {
            graph = new MvcGraph();

            try {
                graph.getRootComponent().register(new Object() {
                    @Provides
                    @EventBusC
                    public EventBus eventBusC() {
                        return new EventBusImpl();
                    }

                    @Provides
                    @EventBusV
                    public EventBus eventBusV() {
                        return new EventBusImpl() {
                            @Override
                            public void post(final Object event) {
                                if (graph.uiThreadRunner.isOnUiThread()) {
                                    postEvent(event);
                                } else {
                                    graph.uiThreadRunner.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            postEvent(event);
                                        }
                                    });
                                }
                            }

                            private void postEvent(Object event) {
                                super.post(event);
                            }
                        };
                    }

                    @Provides
                    public ExecutorService executorService() {
                        return Executors.newFixedThreadPool(10, new ThreadFactory() {
                            @Override
                            public Thread newThread(final Runnable r) {
                                return new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
                                        r.run();
                                    }
                                }, "MvcBackgroundThread");
                            }
                        });
                    }
                });
            } catch (ProvideException e) {
                throw new MvcGraphException("Failed to register base Mvc dependencies", e);
            } catch (ProviderConflictException e) {
                throw new MvcGraphException("Failed to register base Mvc dependencies", e);
            }
        }
        return graph;
    }

}
