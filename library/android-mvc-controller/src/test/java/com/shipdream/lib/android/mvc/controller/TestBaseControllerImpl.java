package com.shipdream.lib.android.mvc.controller;

import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.controller.internal.NavigationControllerImpl;
import com.shipdream.lib.android.mvc.event.BaseEventC2C;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestBaseControllerImpl extends BaseControllerTest{

    @Test
    public void should_return_getState_and_getStateType_correctly() throws Exception {
        NavigationControllerImpl navigationController = new NavigationControllerImpl();

        Assert.assertTrue(navigationController.getModel() == navigationController.getState());
        Assert.assertTrue(navigationController.getStateType() == navigationController.getModelClassType());
    }

    @Test
    public void should_rebind_model_on_restoration() throws Exception {
        NavigationControllerImpl navigationController = new NavigationControllerImpl();

        NavigationController.Model restoreState = mock(NavigationController.Model.class);
        Assert.assertNotEquals(restoreState, navigationController.getModel());

        navigationController.restoreState(restoreState);
        Assert.assertEquals(restoreState, navigationController.getModel());
    }

    @Test (expected = IllegalArgumentException.class)
    public void should_throw_exception_on_binding_null_model() throws Exception {
        NavigationControllerImpl navigationController = new NavigationControllerImpl();

        navigationController.bindModel(this, null);
    }

    class Event extends BaseEventC2C {
        public Event(Object sender) {
            super(sender);
        }
    }

    static class Controller1 extends BaseControllerImpl {
        @Override
        protected Class getModelClassType() {
            return null;
        }

        void postMyEvent(BaseEventC2C e) {
            postC2CEvent(e);
        }
    }

    static class Controller2 extends BaseControllerImpl {
        interface EventProxy {
            void handleEvent(Event event);
        }

        EventProxy proxy;

        @Override
        protected Class getModelClassType() {
            return null;
        }

        private void onEvent(Event event) {
            proxy.handleEvent(event);
        }
    }

    @Test
    public void should_be_able_to_send_and_receive_c2c_events() throws Exception {
        //Arrange
        Controller2.EventProxy proxy = mock(Controller2.EventProxy.class);

        Controller1 c1 = new Controller1();
        graph.inject(c1);
        c1.init();
        Controller2 c2 = new Controller2();
        graph.inject(c2);
        c2.init();
        c2.proxy = proxy;

        Event myEvent = new Event(this);

        //Act
        c1.postMyEvent(myEvent);

        //Assert
        verify(proxy).handleEvent(eq(myEvent));
    }
}
