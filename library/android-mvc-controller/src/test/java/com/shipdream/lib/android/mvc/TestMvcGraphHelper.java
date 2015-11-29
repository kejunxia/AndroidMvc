package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

        NavLocation lastValue = new NavLocation();
        NavLocation currentValue = new NavLocation();

        NavigationController.EventC2V.OnLocationForward event = new NavigationController.EventC2V.OnLocationForward(
                this, lastValue, currentValue, false, null);

        __MvcGraphHelper.retainCachedObjectsBeforeNavigation(event, Injector.getGraph());
        Assert.assertNotEquals(initSize, __MvcGraphHelper.getAllCachedInstances(Injector.getGraph()));

        __MvcGraphHelper.releaseCachedItemsAfterNavigation(event, Injector.getGraph());
        Assert.assertEquals(initSize, __MvcGraphHelper.getAllCachedInstances(Injector.getGraph()).size());
    }


    interface ControllerA extends Disposable {

    }

    static class ControllerAImpl implements ControllerA {
        @Inject
        ManageA manageA;

        /**
         * Execute onDisposed logic of given object
         */
        @Override
        public void onDisposed() {

        }
    }

    interface ManageA extends Disposable {

    }

    ManageA manageAMock;

    static class ViewA {
        @Inject
        ControllerA controllerA;
    }

    static class ViewB {
        @Inject
        ControllerA controllerA;
    }

    static class ViewC {
        @Inject
        ControllerA controllerA;
    }

    static class TestComp extends Component {
        TestMvcGraphHelper testMvcGraphHelper;

        /**
         * Constructor with a stand alone scope cache used only by this component
         */
        public TestComp(TestMvcGraphHelper testMvcGraphHelper) {
            this.testMvcGraphHelper = testMvcGraphHelper;
        }

        @Singleton
        @Provides
        public ControllerA providesController() {
            return new ControllerAImpl();
        }

        @Singleton
        @Provides
        public ManageA providersManageA() {
            testMvcGraphHelper.manageAMock = mock(ManageA.class);
            return testMvcGraphHelper.manageAMock;
        }
    }

    @Test
    public void should_release_nested_injection_until_last_instance_dereferenced() throws
            ProvideException, ProviderConflictException, CircularDependenciesException,
            ProviderMissingException {
        //Prepare graph
        MvcGraph.BaseDependencies baseDependencies = new MvcGraph.BaseDependencies() {
            @Override
            protected ExecutorService createExecutorService() {
                return mock(ExecutorService.class);
            }
        };

        Injector.configGraph(baseDependencies);

        ViewA viewA = new ViewA();
        ViewB viewB = new ViewB();
        ViewC viewC = new ViewC();

        Component component = new TestComp(this);
        Injector.getGraph().register(component);

        Injector.getGraph().inject(viewA);
        ControllerA controllerAInViewA = viewA.controllerA;
        ManageA manageAInViewA = ((ControllerAImpl)viewA.controllerA).manageA;

        //Simulate navigate to viewB
        NavigationController.EventC2V.OnLocationForward navEventB = mock(
                NavigationController.EventC2V.OnLocationForward.class);
        __MvcGraphHelper.retainCachedObjectsBeforeNavigation(navEventB, Injector.getGraph());

        Injector.getGraph().inject(viewB);
        ControllerA controllerAInViewB = viewB.controllerA;
        ManageA manageAInViewB = ((ControllerAImpl)viewA.controllerA).manageA;

        //release viewA
        Injector.getGraph().release(viewA);
        __MvcGraphHelper.releaseCachedItemsAfterNavigation(navEventB, Injector.getGraph());
        verify(manageAMock, times(0)).onDisposed();

        //Simulate navigate to viewC
        NavigationController.EventC2V.OnLocationForward navEventC = mock(
                NavigationController.EventC2V.OnLocationForward.class);
        __MvcGraphHelper.retainCachedObjectsBeforeNavigation(navEventC, Injector.getGraph());

        Injector.getGraph().inject(viewC);
        ControllerA controllerAInViewC = viewB.controllerA;
        ManageA manageAInViewC = ((ControllerAImpl)viewA.controllerA).manageA;

        //release viewB
        Injector.getGraph().release(viewB);
        __MvcGraphHelper.releaseCachedItemsAfterNavigation(navEventC, Injector.getGraph());
        verify(manageAMock, times(0)).onDisposed();

        Assert.assertTrue(manageAInViewA == manageAInViewB);
        Assert.assertTrue(manageAInViewA == manageAInViewC);
    }

}
