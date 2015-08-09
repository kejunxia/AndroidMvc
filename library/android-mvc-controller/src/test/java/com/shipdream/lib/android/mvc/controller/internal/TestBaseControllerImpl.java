/*
 * Copyright 2015 Kejun Xia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shipdream.lib.android.mvc.controller.internal;

import com.shipdream.lib.android.mvc.controller.BaseControllerTest;
import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.android.mvc.event.BaseEventC2C;
import com.shipdream.lib.android.mvc.event.BaseEventC2V;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestBaseControllerImpl extends BaseControllerTest {

    @Test
    public void should_return_getState_and_getStateType_correctly() throws Exception {
        NavigationControllerImpl navigationController = new NavigationControllerImpl();

        Assert.assertTrue(navigationController.getModel() == navigationController.getState());
        Assert.assertTrue(navigationController.getStateType() == navigationController.getModelClassType());
    }

    @Test
    public void should_rebind_model_on_restoration_when_state_class_type_is_NOT_null() throws Exception {
        NavigationControllerImpl navigationController = new NavigationControllerImpl();

        NavigationController.Model restoreState = mock(NavigationController.Model.class);
        Assert.assertNotEquals(restoreState, navigationController.getModel());

        navigationController.restoreState(restoreState);
        Assert.assertEquals(restoreState, navigationController.getModel());
    }

    class StatelessController extends BaseControllerImpl {
        @Override
        protected Class getModelClassType() {
            return null;
        }
    }

    @Test
    public void should_rebind_model_on_restoration_when_state_class_type_is_null() throws Exception {
        StatelessController controller = new StatelessController();
        //Pre-verify
        Assert.assertNull(controller.getModelClassType());
        Assert.assertNull(controller.getModel());

        controller.restoreState("Non-Null State");
        Assert.assertNull(controller.getModel());
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

    class TestController extends BaseControllerImpl {
        @Override
        protected Class getModelClassType() {
            return null;
        }

        void setLogger(Logger logger) {
            this.logger = logger;
        }
    }

    @Test
    public void should_log_exception_when_posting_c2c_events_to_null_eventBus() throws Exception {
        //Arrange
        final Logger loggerMock = mock(Logger.class);

        TestController controller = new TestController();
        graph.inject(controller);
        controller.init();
        controller.setLogger(loggerMock);
        controller.eventBusC2C = null;

        BaseEventC2C event = new BaseEventC2C(this);

        //Act
        controller.postC2CEvent(event);

        //Assert
        verify(loggerMock).warn(anyString(), anyVararg());
    }

    @Test
    public void should_log_exception_when_posting_c2v_events_to_null_eventBus() throws Exception {
        //Arrange
        final Logger loggerMock = mock(Logger.class);

        TestController controller = new TestController();
        graph.inject(controller);
        controller.init();
        controller.setLogger(loggerMock);
        controller.mEventBusC2V = null;

        BaseEventC2V event = new BaseEventC2V(this);

        //Act
        controller.postC2VEvent(event);

        //Assert
        verify(loggerMock).warn(anyString(), anyVararg());
    }

    private static class PrivateModel {
        private PrivateModel() {}
    }

    class BadController extends BaseControllerImpl<PrivateModel> {
        @Override
        protected Class<PrivateModel> getModelClassType() {
            return PrivateModel.class;
        }
    }

    @Test (expected = RuntimeException.class)
    public void should_throw_out_runtime_exception_when_unable_to_create_model_instance() throws Exception {
        BadController controller = new BadController();
        graph.inject(controller);
        controller.init();
    }
}
