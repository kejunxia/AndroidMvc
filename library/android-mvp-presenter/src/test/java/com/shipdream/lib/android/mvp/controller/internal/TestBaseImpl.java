/*
 * Copyright 2016 Kejun Xia
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

package com.shipdream.lib.android.mvp.controller.internal;

import com.shipdream.lib.android.mvp.controller.BaseTest;
import com.shipdream.lib.android.mvp.manager.NavigationManager;
import com.shipdream.lib.android.mvp.event.BaseEventC;
import com.shipdream.lib.android.mvp.event.BaseEventV;
import com.shipdream.lib.android.mvp.manager.internal.NavigationManagerImpl;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestBaseImpl extends BaseTest {

    @Test
    public void should_return_getState_and_getStateType_correctly() throws Exception {
        NavigationManagerImpl navigationManager = new NavigationManagerImpl();

        Assert.assertTrue(navigationManager.getModel() == navigationManager.getModel());
        Assert.assertTrue(navigationManager.modelType() == navigationManager.modelType());
    }

    @Test
    public void should_rebind_model_on_restoration_when_state_class_type_is_NOT_null() throws Exception {
        NavigationManagerImpl navigationManager = new NavigationManagerImpl();

        NavigationManager.Model restoreState = mock(NavigationManager.Model.class);
        Assert.assertNotEquals(restoreState, navigationManager.getModel());

        navigationManager.restoreModel(restoreState);
        Assert.assertEquals(restoreState, navigationManager.getModel());
    }

    class StatelessController extends BaseControllerImpl {
        @Override
        public Class modelType() {
            return null;
        }
    }

    @Test
    public void should_rebind_model_on_restoration_when_state_class_type_is_null() throws Exception {
        StatelessController controller = new StatelessController();
        //Pre-verify
        Assert.assertNull(controller.modelType());
        Assert.assertNull(controller.getModel());

        controller.restoreModel("Non-Null State");
        Assert.assertNull(controller.getModel());
    }

    @Test (expected = IllegalArgumentException.class)
    public void should_throw_exception_on_binding_null_model() throws Exception {
        NavigationManagerImpl navigationManager = new NavigationManagerImpl();

        navigationManager.bindModel(this, null);
    }

    class Event extends BaseEventC {
        public Event(Object sender) {
            super(sender);
        }
    }

    static class Controller1 extends BaseControllerImpl {
        @Override
        public Class modelType() {
            return null;
        }

        void postMyEvent(BaseEventC e) {
            postEvent2C(e);
        }
    }

    static class Controller2 extends BaseControllerImpl {
        interface EventProxy {
            void handleEvent(Event event);
        }

        EventProxy proxy;

        @Override
        public Class modelType() {
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
        c1.onConstruct();
        Controller2 c2 = new Controller2();
        graph.inject(c2);
        c2.onConstruct();
        c2.proxy = proxy;

        Event myEvent = new Event(this);

        //Act
        c1.postMyEvent(myEvent);

        //Assert
        verify(proxy).handleEvent(eq(myEvent));
    }

    class TestController extends BaseControllerImpl {
        @Override
        public Class modelType() {
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
        controller.onConstruct();
        controller.setLogger(loggerMock);
        controller.eventBus2C = null;

        BaseEventC event = new BaseEventC(this){};

        //Act
        controller.postEvent2C(event);

        //Assert
        verify(loggerMock).warn(anyString(), anyVararg());
    }

    @Test
    public void should_log_exception_when_posting_c2v_events_to_null_eventBus() throws Exception {
        //Arrange
        final Logger loggerMock = mock(Logger.class);

        TestController controller = new TestController();
        graph.inject(controller);
        controller.onConstruct();
        controller.setLogger(loggerMock);
        controller.eventBus2V = null;

        BaseEventV event = new BaseEventV(this){};

        //Act
        controller.postEvent2V(event);

        //Assert
        verify(loggerMock).warn(anyString(), anyVararg());
    }

    private static class PrivateModel {
        private PrivateModel() {}
    }

    class BadController extends BaseControllerImpl<PrivateModel> {
        @Override
        public Class<PrivateModel> modelType() {
            return PrivateModel.class;
        }
    }

    @Test (expected = RuntimeException.class)
    public void should_throw_out_runtime_exception_when_unable_to_create_model_instance() throws Exception {
        BadController controller = new BadController();
        graph.inject(controller);
        controller.onConstruct();
    }
}
