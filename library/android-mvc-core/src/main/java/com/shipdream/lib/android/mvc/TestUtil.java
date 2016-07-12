package com.shipdream.lib.android.mvc;

/**
 * Util to help test controllers.
 */
public class TestUtil {
    /**
     * Allow test cases to assign the view of controller.
     * @param controller The controller
     * @param view The view associated with the controller
     * @param <VIEW> The type of view
     */
    public static <VIEW extends UiView> void assignControllerView(Controller controller, VIEW view) {
        controller.view = view;
    }
}
