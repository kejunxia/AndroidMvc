package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusV;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

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

    @Test
    public void should() {
        Injector.configGraph(new MvcGraph.BaseDependencies() {
            @Override
            protected ExecutorService createExecutorService() {
                return mock(ExecutorService.class);
            }
        });
        Assert.assertEquals(0, __MvcGraphHelper.getAllCachedInstances(Injector.getGraph()).size());

        class View1 {
            @Inject
            @EventBusC
            EventBus eventBus;
        }

        View1 v1 = new View1();
        Injector.getGraph().inject(v1);
        Assert.assertEquals(1, __MvcGraphHelper.getAllCachedInstances(Injector.getGraph()).size());

        class View2 {
            @Inject
            @EventBusC
            EventBus eventBus;
        }

        View2 v2 = new View2();
        Injector.getGraph().inject(v2);
        Assert.assertEquals(1, __MvcGraphHelper.getAllCachedInstances(Injector.getGraph()).size());

        class View3 {
            @Inject
            @EventBusV
            EventBus eventBus;
        }

        View3 v3 = new View3();
        Injector.getGraph().inject(v3);
        Assert.assertEquals(2, __MvcGraphHelper.getAllCachedInstances(Injector.getGraph()).size());
    }
}
