package com.shipdream.lib.android.mvc;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestFragmentController extends BaseTest {
    @Test
    public void should_run_all_life_cycle_calls_without_exception() {
        FragmentController controller = new FragmentController() {
            @Override
            public Class modelType() {
                return null;
            }
        };

        Assert.assertFalse(controller.onBackButtonPressed());
        controller.currentOrientation();
        controller.onOrientationChanged(Orientation.LANDSCAPE, Orientation.PORTRAIT);
        controller.onResume();
        controller.onPause();
        controller.onPopAway();
        controller.onPoppedOutToFront();
        controller.onPushToBackStack();
        controller.onReturnForeground();

        UiView view = mock(UiView.class);
        controller.view = view;
        controller.onViewReady(new Reason());

        verify(view).update();
    }
}
