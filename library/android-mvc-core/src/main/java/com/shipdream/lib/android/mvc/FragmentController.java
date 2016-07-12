package com.shipdream.lib.android.mvc;

public abstract class FragmentController<MODEL, VIEW extends UiView> extends Controller<MODEL, VIEW>{
    Orientation orientation;

    protected Orientation currentOrientation() {
        return orientation;
    }

    /**
     * Called when the view of the corresponding fragment is created. It calls {@link UiView#update()}
     * automatically.
     */
    public void onViewReady(Reason reason) {
        view.update();
    }

    /**
     * Called when corresponding fragment's onResume is called
     */
    public void onResume() {
    }

    /**
     * Called when corresponding fragment is about to be pushed to background
     */
    public void onPushToBackStack() {
    }

    /**
     * Called when corresponding fragment was the top most fragment and is about to be removed by
     * fragment popping out from back stack
     */
    public void onPopAway() {
    }

    /**
     * Called when corresponding fragment returns foreground from background <b>ONLY</b> when the
     * model doesn't need to be rebound to the controller. For example, if the fragment is rotated
     * or recreated then this method won't be called. But if home button pressed and then then the
     * app is brought back to front without being killed by the OS, this method will be called.
     */
    public void onReturnForeground() {
    }

    /**
     * Called when corresponding fragment popped out from back history
     */
    public void onPoppedOutToFront() {
    }

    /**
     * Called when corresponding fragment's orientation changed
     */
    public void onOrientationChanged(Orientation last, Orientation current) {
        orientation = current;
    }

    /**
     * Called when corresponding fragment's onPause is called
     */
    public void onPause() {
    }

    /**
     * Called when corresponding fragment's onBackButtonPressed is called. This happens when the
     * device back button is pressed.
     * @return True to consume the back button pressed event, otherwise returns false which will
     * forward the back button pressed event to OS
     */
    public boolean onBackButtonPressed() {
        return false;
    }
}
