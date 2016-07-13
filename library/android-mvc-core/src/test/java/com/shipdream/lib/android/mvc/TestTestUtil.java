package com.shipdream.lib.android.mvc;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class TestTestUtil {
    @Test
    public void can_construct_testUtil() {
        new TestUtil();
    }

    @Test
    public void should_assign_controller_view_correctly() {
        Controller controller = new Controller() {
            @Override
            public Class modelType() {
                return null;
            }
        };
        UiView view = mock(UiView.class);

        TestUtil.assignControllerView(controller, view);

        Assert.assertTrue(controller.view == view);
    }
}
