package com.shipdream.lib.android.mvc;

public interface View {
    /**
     * When a view is requested to update itself, it should read it's controller's model and bind
     * the data to the view.
     */
    void update();
}
