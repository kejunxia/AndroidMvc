package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.event.BaseEventC;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.manager.BaseManagerImpl;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestManagerImpl {
    @After
    public void tearDown() throws Exception {
        Injector.mvcGraph = null;
    }

    @Test
    public void should_not_crash_when_manager_post_event_even_without_registering_controller_event_bus() {
        class MyManager extends BaseManagerImpl {
            void doPost(){
                postControllerEvent(mock(BaseEventC.class));
            }

            @Override
            public Class modelType() {
                return null;
            }
        };

        MyManager myManager = new MyManager();
        //Haven't registered controller event yet, but should not throw exception but just log
        myManager.doPost();
    }

    @Test
    public void should_be_able_to_post_events_to_controller_event_bus_from_manager() {
        class MyEvent extends BaseEventC{
            public MyEvent(Object sender) {
                super(sender);
            }
        }

        final MyEvent myEvent = mock(MyEvent.class);

        class MyManager extends BaseManagerImpl {
            void doPost(){
                postControllerEvent(myEvent);
            }

            @Override
            public Class modelType() {
                return null;
            }
        }

        final EventBus bus = mock(EventBus.class);

        Injector.configGraph(new MvcGraph.BaseDependencies() {
            @Override
            protected EventBus createEventBusC() {
                return bus;
            }

            @Override
            protected ExecutorService createExecutorService() {
                return null;
            }
        });

        MyManager myManager = new MyManager();
        Injector.getGraph().inject(myManager);

        verify(bus, times(0)).post(anyObject());

        myManager.doPost();

        verify(bus, times(1)).post(myEvent);
    }
}
