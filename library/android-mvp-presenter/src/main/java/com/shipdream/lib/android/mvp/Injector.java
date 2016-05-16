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

import com.shipdream.lib.poke.exception.PokeException;

public class Injector {
    static MvcGraph mvcGraph;

    /**
     * Config the dependencies of MvcGraph. Be careful to use this method because it will dump the
     * existing graph and all injectable instances managed by it
     * @param dependencies the dependencies.
     */
    public static void configGraph(MvcGraph.BaseDependencies dependencies) {
        try {
            mvcGraph = new MvcGraph(dependencies);
        } catch (PokeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the graph managing injectable objects.
     * @return
     */
    public static MvcGraph getGraph() {
        if (mvcGraph == null) {
            throw new RuntimeException("In unit testing, the graph needs to be mocked before running tests. See how the graph is prepared by TestControllerBase#prepareGraph() in https://github.com/kejunxia/AndroidMvc/blob/master/samples/note/core/src/test/java/com/shipdream/lib/android/mvc/samples/note/controller/internal/TestControllerBase.java");
        }
        return mvcGraph;
    }
}
