package com.shipdream.lib.android.mvc;

import com.shipdream.lib.poke.exception.PokeException;

public class Injector {
    private static MvcGraph mvcGraph;

    public static void configGraph(MvcGraph.BaseDependencies baseDependencies) {
        try {
            mvcGraph = new MvcGraph(baseDependencies);
        } catch (PokeException e) {
            throw new RuntimeException(e);
        }
    }

    public static MvcGraph getGraph() {
        return mvcGraph;
    }
}
