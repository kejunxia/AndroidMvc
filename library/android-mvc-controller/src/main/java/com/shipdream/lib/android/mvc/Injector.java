package com.shipdream.lib.android.mvc;

import com.shipdream.lib.poke.exception.PokeException;

public class Injector {
    private static MvcGraph mvcGraph;

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
        return mvcGraph;
    }
}
