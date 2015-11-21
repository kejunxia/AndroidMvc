package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC2C;
import com.shipdream.lib.android.mvc.event.bus.internal.EventBusImpl;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Provides;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import javax.inject.Singleton;

import static org.mockito.Mockito.mock;

public class TestInjector {
    @After
    public void tearDown() throws Exception {
        Injector.mvcGraph = null;
    }

    @Test(expected = RuntimeException.class)
    public void should_raise_exception_when_getting_mvc_graph_before_configuring_it() {
        Injector.getGraph();
    }

    static class Comp extends Component {
        @Provides
        @EventBusC2C
        @Singleton
        public EventBus providesIEventBusC2C() {
            return new EventBusImpl();
        }
    }

    @Test(expected = RuntimeException.class)
    public void should_raise_runtime_exception_when_exception_occurring_when_configuring_mvc_graph_by_injector() {
        MvcGraph.BaseDependencies baseDependencies = new MvcGraph.BaseDependencies() {
            @Override
            protected ExecutorService createExecutorService() {
                return mock(ExecutorService.class);
            }
        };

        Injector.configGraph(baseDependencies);

        //Register component providing duplicate instances
        Injector.getGraph().register(new Comp());

        //Exception should be raised here
        Injector.configGraph(baseDependencies);
    }
}
