package com.shipdream.lib.android.mvc.controller.internal;

/**
 * Preparer in which the injected instance will be configured before a navigation.
 */
public interface Preparer<T> {
    /**
     * Prepare the state for a navigation
     * @param instance The instance(usually a controller) that will be configured
     */
    void prepare(T instance);
}
