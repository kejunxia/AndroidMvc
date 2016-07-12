package com.shipdream.lib.android.mvc;

/**
 * UiView represents android views. Call it Ui-View not to conflict with android.view.View and
 * simplify imports
 */
public interface UiView {
    /**
     * When a view is requested to update itself, it should read it's controller's model by
     * {@link Controller#getModel()} to bind the data to the view.
     *
     * <p>Do NOT change values of model from view but only from controllers.</p>
     */
    void update();
}
