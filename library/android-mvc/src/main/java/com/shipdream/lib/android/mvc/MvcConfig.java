package com.shipdream.lib.android.mvc;

public class MvcConfig {
    /**
     * Config the dependencies of MvcGraph.
     * @param dependencies the dependencies.
     */
    public static void configGraph(MvcGraph.BaseDependencies dependencies) {
        Injector.configGraph(dependencies);
    }
}
