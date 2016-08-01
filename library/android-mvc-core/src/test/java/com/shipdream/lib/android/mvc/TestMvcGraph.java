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

package com.shipdream.lib.android.mvc;

import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Consumer;
import com.shipdream.lib.poke.Graph;
import com.shipdream.lib.poke.Provider;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestMvcGraph extends BaseTest{
    interface Os {
    }

    @Qualifier
    @Documented
    @Retention(RUNTIME)
    @interface Apple {
    }

    @Qualifier
    @Documented
    @Retention(RUNTIME)
    @interface Google {
    }

    static class iOS implements Os {

    }

    static class Android implements Os {

    }

    static class DeviceModule {
        @Provides
        @Singleton
        public Os provide() {
            return new Android();
        }

        @Provides
        @Singleton
        @Apple
        public Os provideIos() {
            return new iOS();
        }

        @Provides
        @Singleton
        @Google
        public Os provideAndroid() {
            return new Android();
        }
    }

    class Device {
        @Inject
        private Os android;

        @Inject
        @Apple
        private Os os;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        graph.uiThreadRunner = mock(UiThreadRunner.class);
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(true);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = (Runnable) invocation.getArguments()[0];
                runnable.run();
                return null;
            }
        }).when(graph.uiThreadRunner).post(any(Runnable.class));
    }

    @Test
    public void should_be_able_to_post_on_default_ui_thread_runner() {
        new MvcGraph().uiThreadRunner.post(mock(Runnable.class));

        new MvcGraph().uiThreadRunner.postDelayed(mock(Runnable.class), 100);
    }

    @Test
    public void should_throw_IllegalRootComponentException_on_graph_preparation_with_attached_component() throws Component.MultiParentException, ProviderConflictException {
        MvcComponent parent = new MvcComponent("");
        MvcComponent child = new MvcComponent("");
        parent.attach(child);

        boolean caught = false;
        try {
            graph.prepareInternalGraph(new Graph(), child);
        } catch (MvcGraphException e) {
            if (e.getCause() instanceof Graph.IllegalRootComponentException) {
                caught = true;
            }
        }
        Assert.assertTrue(caught);
    }

    @Test
    public void should_throw_ProviderConflictException_on_graph_preparation_by_duplicate_registration() throws Component.MultiParentException, ProviderConflictException, ProvideException {
        MvcComponent component = new MvcComponent("");
        component.register(new Object() {
            @Provides
            public UiThreadRunner uiThreadRunner() {
                return mock(UiThreadRunner.class);
            }
        });
        boolean caught = false;
        try {
            graph.prepareInternalGraph(new Graph(), component);
        } catch (MvcGraphException e) {
            if (e.getCause() instanceof ProviderConflictException) {
                caught = true;
            }
        }
        Assert.assertTrue(caught);
    }

    @Test
    public void use_method_should_retain_and_release_instance_without_qualifier_correctly() throws ProvideException, ProviderConflictException {
        graph.getRootComponent().register(new DeviceModule());

        //OsReferenceCount = 0
        graph.use(Os.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //First time to create the instance.
                //OsReferenceCount = 1
                Assert.assertTrue(instance instanceof Android);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 0

        final Device device = new Device();
        graph.inject(device);  //OsReferenceCount = 1
        //New instance created and cached

        graph.use(Os.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //Since reference count is greater than 0, cached instance will be reused
                //OsReferenceCount = 2
                Assert.assertTrue(device.android == instance);
                Assert.assertTrue(instance instanceof Android);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 1

        graph.release(device);  //OsReferenceCount = 0
        //Last instance released, so next time a new instance will be created

        graph.use(Os.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //OsReferenceCount = 1
                //Since the cached instance is cleared, the new instance is a newly created one.
                Assert.assertTrue(device.android != instance);
                Assert.assertTrue(instance instanceof Android);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 0

        graph.use(Os.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //OsReferenceCount = 1
                //Since the cached instance is cleared, the new instance is a newly created one.
                Assert.assertTrue(device.android != instance);
                Assert.assertTrue(instance instanceof Android);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 0
        //Cached instance cleared again

        graph.use(Os.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //OsReferenceCount = 1
                graph.inject(device);
                //Injection will reuse the cached instance and increment the reference count
                //OsReferenceCount = 2

                //Since the cached instance is cleared, the new instance is a newly created one.
                Assert.assertTrue(device.android == instance);
                Assert.assertTrue(instance instanceof Android);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 1

        graph.release(device);  //OsReferenceCount = 0
    }

    private void do_use_method_should_retain_and_release_instance_correctly() {
        @Apple
        class NeedIoS {

        }

        Annotation iosQualifier = NeedIoS.class.getAnnotation(Apple.class);

        //OsReferenceCount = 0
        graph.use(Os.class, iosQualifier, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //First time to create the instance.
                //OsReferenceCount = 1
                Assert.assertTrue(instance instanceof iOS);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 0

        final Device device = new Device();
        graph.inject(device);  //OsReferenceCount = 1
        //New instance created and cached

        graph.use(Os.class, iosQualifier, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //Since reference count is greater than 0, cached instance will be reused
                //OsReferenceCount = 2
                Assert.assertTrue(device.os == instance);
                Assert.assertTrue(instance instanceof iOS);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 1

        graph.release(device);  //OsReferenceCount = 0
        //Last instance released, so next time a new instance will be created

        graph.use(Os.class, iosQualifier, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //OsReferenceCount = 1
                //Since the cached instance is cleared, the new instance is a newly created one.
                Assert.assertTrue(device.os != instance);
                Assert.assertTrue(instance instanceof iOS);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 0

        graph.use(Os.class, iosQualifier, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //OsReferenceCount = 1
                //Since the cached instance is cleared, the new instance is a newly created one.
                Assert.assertTrue(device.os != instance);
                Assert.assertTrue(instance instanceof iOS);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 0
        //Cached instance cleared again

        graph.use(Os.class, iosQualifier, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //OsReferenceCount = 1
                graph.inject(device);
                //Injection will reuse the cached instance and increment the reference count
                //OsReferenceCount = 2

                //Since the cached instance is cleared, the new instance is a newly created one.
                Assert.assertTrue(device.os == instance);
                Assert.assertTrue(instance instanceof iOS);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 1

        graph.release(device);  //OsReferenceCount = 0
    }

    @Test
    public void use_method_should_retain_and_release_instance_correctly() throws ProvideException, ProviderConflictException {
        graph.getRootComponent().register(new DeviceModule());

        do_use_method_should_retain_and_release_instance_correctly();
    }

    @Test
    public void should_delegate_mvc_graph_properly() throws ProvideException, ProviderConflictException {
        // Arrange
        Graph graphMock = mock(Graph.class);

        graph.graph = graphMock;

        // Act
        Graph.Monitor monitor = mock(Graph.Monitor.class);
        graph.registerMonitor(monitor);
        // Verify
        verify(graphMock).registerMonitor(eq(monitor));

        // Arrange
        reset(graphMock);
        // Act
        graph.unregisterMonitor(monitor);
        // Verify
        verify(graphMock).unregisterMonitor(eq(monitor));

        // Arrange
        reset(graphMock);
        // Act
        graph.clearMonitors();
        // Verify
        verify(graphMock).clearMonitors();

        // Arrange
        reset(graphMock);
        Provider.DereferenceListener providerFreedListener = mock(Provider.DereferenceListener.class);
        // Act
        graph.registerDereferencedListener(providerFreedListener);
        // Verify
        verify(graphMock).registerDereferencedListener(eq(providerFreedListener));

        // Arrange
        reset(graphMock);
        // Act
        graph.unregisterDereferencedListener(providerFreedListener);
        // Verify
        verify(graphMock).unregisterDereferencedListener(eq(providerFreedListener));

        // Arrange
        reset(graphMock);
        // Act
        graph.clearDereferencedListeners();
        // Verify
        verify(graphMock).clearDereferencedListeners();
    }

    @Test
    public void non_ui_thread_should_delegate_mvc_graph_properly() throws Exception {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        should_delegate_mvc_graph_properly();
    }

    @Test (expected = IllegalStateException.class)
    public void should_throw_out_exceptions_when_registering_component()
            throws ProvideException, ProviderConflictException, Graph.IllegalRootComponentException {
        // Arrange
        MvcComponent badComponent = mock(MvcComponent.class);
        MvcGraph mvcGraph = new MvcGraph();
        mvcGraph.setRootComponent(badComponent);

        Object obj = new Object();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                throw new IllegalStateException();
            }
        }).when(badComponent).register(any(Object.class));

        // Act
        mvcGraph.getRootComponent().register(obj);
    }

    interface UnimplementedInterface{}

    @Test(expected = MvcGraphException.class)
    public void should_raise_mvc_graph_exception_when_inject_on_poke_exception() {
        class View {
            @Inject
            UnimplementedInterface unimplementedInterface;
        }
        graph.inject(new View());
    }

    @Test(expected = MvcGraphException.class)
    public void should_raise_mvc_graph_exception_when_inject_on_poke_exception_on_non_ui_thread() {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);

        should_raise_mvc_graph_exception_when_inject_on_poke_exception();
    }

    @Test(expected = MvcGraphException.class)
    public void should_raise_mvc_graph_exception_when_use_on_poke_exception_on_non_ui_thread() {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);

        class View {
            @Inject
            UnimplementedInterface unimplementedInterface;
        }
        graph.use(UnimplementedInterface.class, null, mock(Consumer.class));
    }

    @Test(expected = MvcGraphException.class)
    public void should_raise_mvc_graph_exception_when_release_on_poke_exception() {
        class View {
            @Inject
            UnimplementedInterface unimplementedInterface;
        }
        View view = new View();
        view.unimplementedInterface = new UnimplementedInterface() {
        };
        graph.release(view);
    }

    @Test(expected = MvcGraphException.class)
    public void should_raise_mvc_graph_exception_when_release_on_poke_exception_on_non_ui_thread() {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);

        should_raise_mvc_graph_exception_when_release_on_poke_exception();
    }

    @Test(expected = MvcGraphException.class)
    public void should_raise_mvc_graph_exception_when_use_on_poke_exception() {
        class View {
            @Inject
            UnimplementedInterface unimplementedInterface;
        }
        graph.use(UnimplementedInterface.class, new Consumer<UnimplementedInterface>() {
            @Override
            public void consume(UnimplementedInterface instance) {
            }
        });
    }

    @Test(expected = MvcGraphException.class)
    public void should_raise_mvc_graph_exception_when_use_on_poke_exception_on_non_thread() {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        should_raise_mvc_graph_exception_when_use_on_poke_exception();
    }

    @Test
    public void should_throw_exception_when_mvc_graph_use_consumer_on_non_main_thread() {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        graph.use(String.class, mock(Consumer.class));
    }

    @Test
    public void should_throw_exception_when_mvc_graph_use_consumer_without_annotation_on_non_main_thread() {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        graph.use(String.class, null, mock(Consumer.class));
    }

    @Test(expected = MvcGraphException.class)
    public void should_throw_exception_when_mvc_graph_reference_on_non_main_thread() throws ProvideException, CircularDependenciesException, ProviderMissingException {
        graph.reference(String.class, null);

        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        graph.reference(String.class, null);
    }

    @Test
    public void should_throw_exception_when_mvc_graph_dreference_on_non_main_thread() throws ProvideException, CircularDependenciesException, ProviderMissingException {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        graph.dereference(this, TestMvcGraph.class, null);

        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(true);
        graph.dereference(this, TestMvcGraph.class, null);
    }

    @Test
    public void should_throw_exception_when_mvc_graph_inject_on_non_main_thread() {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        graph.inject(this);

        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(true);
        graph.inject(this);
    }

    @Test
    public void should_throw_exception_when_mvc_graph_release_on_non_main_thread() {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        graph.release(this);

        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(true);
        graph.release(this);
    }

    @Test(expected = MvcGraphException.class)
    public void should_throw_exception_when_set_attached_root_component_to_graph_on_main_thread() throws Graph.IllegalRootComponentException, Component.MultiParentException, ProviderConflictException {
        MvcComponent parent = new MvcComponent("parent");
        MvcComponent child = new MvcComponent("");
        parent.attach(child);
        graph.setRootComponent(child);
    }

    @Test(expected = MvcGraphException.class)
    public void should_throw_exception_when_set_attached_root_component_to_graph_on_non_main_thread() throws Graph.IllegalRootComponentException, Component.MultiParentException, ProviderConflictException {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        MvcComponent parent = new MvcComponent("parent");
        MvcComponent child = new MvcComponent("");
        parent.attach(child);
        graph.setRootComponent(child);
    }

    @Test
    public void should_throw_exception_when_mvc_graph_set_rootComponent_on_non_main_thread() throws Graph.IllegalRootComponentException {
        graph.setRootComponent(new MvcComponent(""));

        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        graph.setRootComponent(new MvcComponent(""));
    }

    @Test
    public void should_throw_exception_when_mvc_graph_register_deferenceListener_on_non_main_thread() throws Graph.IllegalRootComponentException {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        graph.registerDereferencedListener(mock(Provider.DereferenceListener.class));

        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(true);
        graph.registerDereferencedListener(mock(Provider.DereferenceListener.class));
    }

    @Test
    public void should_throw_exception_when_mvc_graph_unregister_deferenceListener_on_non_main_thread() throws Graph.IllegalRootComponentException {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        graph.unregisterDereferencedListener(mock(Provider.DereferenceListener.class));

        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(true);
        graph.unregisterDereferencedListener(mock(Provider.DereferenceListener.class));
    }

    @Test
    public void should_throw_exception_when_mvc_graph_clear_deferenceListeners_on_non_main_thread() throws Graph.IllegalRootComponentException {

        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        graph.clearDereferencedListeners();

        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(true);
        graph.clearDereferencedListeners();
    }

    @Test
    public void should_throw_exception_when_mvc_graph_register_monitor_on_non_main_thread() throws Graph.IllegalRootComponentException {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        graph.registerMonitor(mock(Graph.Monitor.class));

        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(true);
        graph.registerMonitor(mock(Graph.Monitor.class));
    }

    @Test
    public void should_throw_exception_when_mvc_graph_unregister_monitor_on_non_main_thread() throws Graph.IllegalRootComponentException {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        graph.unregisterMonitor(mock(Graph.Monitor.class));

        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(true);
        graph.unregisterMonitor(mock(Graph.Monitor.class));
    }

    @Test
    public void should_throw_exception_when_mvc_graph_clear_monitors_on_non_main_thread() throws Graph.IllegalRootComponentException {
        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(false);
        graph.clearMonitors();

        when(graph.uiThreadRunner.isOnUiThread()).thenReturn(true);
        graph.clearMonitors();
    }

    @Test
    public void default_uiThreadRunner_should_post_on_same_thread() {
        final Thread thread = Thread.currentThread();
        graph.uiThreadRunner.post(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(Thread.currentThread() == thread);
            }
        });
    }

    @Test
    public void default_uiThreadRunner_should_post_with_delay_on_same_thread() {
        final Thread thread = Thread.currentThread();
        graph.uiThreadRunner.postDelayed(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(Thread.currentThread() == thread);
            }
        }, 100);
    }
}
