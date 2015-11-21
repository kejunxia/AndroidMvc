package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.controller.NavigationController;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;

public class TestMvcGraphHelper {
    @After
    public void tearDown() throws Exception {
        Injector.mvcGraph = null;
    }

    class View {
        @Inject
        private NavigationController navigationController;
    }

    @Test
    public void should_retain_and_release_cached_instance_by_mvcgraph_helper() {
        //Prepare graph
        MvcGraph.BaseDependencies baseDependencies = new MvcGraph.BaseDependencies() {
            @Override
            protected ExecutorService createExecutorService() {
                return mock(ExecutorService.class);
            }
        };

        Injector.configGraph(baseDependencies);

        View view = new View();
        Injector.getGraph().inject(view);

        int initSize = __MvcGraphHelper.getAllCachedInstances(Injector.getGraph()).size();

        __MvcGraphHelper.retainCachedObjectsBeforeNavigation(Injector.getGraph());
        Assert.assertNotEquals(initSize, __MvcGraphHelper.getAllCachedInstances(Injector.getGraph()));

        __MvcGraphHelper.releaseCachedItemsAfterNavigation(Injector.getGraph());
        Assert.assertEquals(initSize, __MvcGraphHelper.getAllCachedInstances(Injector.getGraph()).size());
    }

}
