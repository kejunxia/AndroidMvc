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

    private AndroidMvp() {
    }

}
