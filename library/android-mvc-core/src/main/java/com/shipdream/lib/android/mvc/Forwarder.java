package com.shipdream.lib.android.mvc;

import org.jetbrains.annotations.NotNull;

/**
 * Configuration of forwarding navigation by setting
 * <ul>
 *     <li>{@link #setInterim(boolean)}</li>
 *     <li>{@link #clearTo(String)}</li>
 *     <li>{@link #clearAll()}</li>
 * </ul>
 */
public class Forwarder {
    boolean interim = false;
    boolean clearHistory = false;
    String clearToLocationId;

    /**
     * Set whether this location navigating to is an interim location that won't be pushed to
     * history back stack.
     * @return
     */
    public Forwarder setInterim(boolean interim){
        this.interim = interim;
        return this;
    }

    /**
     * Indicates this location navigating to is an interim location that won't be pushed to
     * history back stack.
     * @return
     */
    public boolean isInterim() {
        return interim;
    }

    /**
     * Clear history to the first matched locationId. For example, current history is
     * A->B->A->C->B, clearToLocationId("A") will pop B and C and leave the back stack as A->B->A.
     *
     * <p>Note that, if {@link #clearAll()} is called, this method has no effect</p>
     * @param clearTo The presenter below the next location after clearing history
     * @return This instance
     */
    public Forwarder clearTo(@NotNull Class<? extends Controller> clearTo) {
        clearHistory = true;
        clearToLocationId = clearTo.getName();
        return this;
    }

    /**
     * Clear all history.
     *
     * <p>Note that, if this method is called, {@link #clearTo(Class)} will have no effect</p>
     * @return This instance
     */
    public Forwarder clearAll() {
        clearHistory = true;
        clearToLocationId = null;
        return this;
    }
}
