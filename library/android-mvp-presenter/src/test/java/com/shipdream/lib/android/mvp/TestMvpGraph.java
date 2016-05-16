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

package com.shipdream.lib.android.mvp;

import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Consumer;
import com.shipdream.lib.poke.Graph;
import com.shipdream.lib.poke.Provider.OnFreedListener;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.ScopeCache;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestMvpGraph {
    private MvpGraph mvpGraph;
    private ExecutorService executorService;

    @Before
    public void setUp() throws Exception {
        executorService = mock(ExecutorService.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = (Runnable) invocation.getArguments()[0];
                runnable.run();
                return null;
            }
        }).when(executorService).submit(any(Runnable.class));

        mvpGraph = new MvpGraph(new MvpGraph.BaseDependencies() {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }
        });
    }

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

    static class DeviceComponent extends Component {
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

    @Test
    public void use_method_should_retain_and_release_instance_without_qualifier_correctly() {
        mvpGraph.register(new DeviceComponent());

        //OsReferenceCount = 0
        mvpGraph.use(Os.class, new Consumer<Os>() {
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
        mvpGraph.inject(device);  //OsReferenceCount = 1
        //New instance created and cached

        mvpGraph.use(Os.class, new Consumer<Os>() {
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

        mvpGraph.release(device);  //OsReferenceCount = 0
        //Last instance released, so next time a new instance will be created

        mvpGraph.use(Os.class, new Consumer<Os>() {
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

        mvpGraph.use(Os.class, new Consumer<Os>() {
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

        mvpGraph.use(Os.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //OsReferenceCount = 1
                mvpGraph.inject(device);
                //Injection will reuse the cached instance and increment the reference count
                //OsReferenceCount = 2

                //Since the cached instance is cleared, the new instance is a newly created one.
                Assert.assertTrue(device.android == instance);
                Assert.assertTrue(instance instanceof Android);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 1

        mvpGraph.release(device);  //OsReferenceCount = 0
    }

    @Test
    public void use_method_should_retain_and_release_instance_correctly() {
        mvpGraph.register(new DeviceComponent());

        @Apple
        class NeedIoS {

        }

        Annotation iosQualifier = NeedIoS.class.getAnnotation(Apple.class);

        //OsReferenceCount = 0
        mvpGraph.use(Os.class, iosQualifier, new Consumer<Os>() {
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
        mvpGraph.inject(device);  //OsReferenceCount = 1
        //New instance created and cached

        mvpGraph.use(Os.class, iosQualifier, new Consumer<Os>() {
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

        mvpGraph.release(device);  //OsReferenceCount = 0
        //Last instance released, so next time a new instance will be created

        mvpGraph.use(Os.class, iosQualifier, new Consumer<Os>() {
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

        mvpGraph.use(Os.class, iosQualifier, new Consumer<Os>() {
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

        mvpGraph.use(Os.class, iosQualifier, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //OsReferenceCount = 1
                mvpGraph.inject(device);
                //Injection will reuse the cached instance and increment the reference count
                //OsReferenceCount = 2

                //Since the cached instance is cleared, the new instance is a newly created one.
                Assert.assertTrue(device.os == instance);
                Assert.assertTrue(instance instanceof iOS);
            }
        });
        //Reference count decremented by use method automatically
        //OsReferenceCount = 1

        mvpGraph.release(device);  //OsReferenceCount = 0
    }

    @Test
    public void should_delegate_mvp_graph_properly() throws ProvideException, ProviderConflictException {
        // Arrange
        Graph graphMock = mock(Graph.class);
        mvpGraph.graph = graphMock;

        // Act
        Graph.Monitor monitor = mock(Graph.Monitor.class);
        mvpGraph.registerMonitor(monitor);
        // Verify
        verify(graphMock).registerMonitor(eq(monitor));

        // Arrange
        reset(graphMock);
        // Act
        mvpGraph.unregisterMonitor(monitor);
        // Verify
        verify(graphMock).unregisterMonitor(eq(monitor));

        // Arrange
        reset(graphMock);
        // Act
        mvpGraph.clearMonitors();
        // Verify
        verify(graphMock).clearMonitors();

        // Arrange
        reset(graphMock);
        OnFreedListener providerFreedListener = mock(OnFreedListener.class);
        // Act
        mvpGraph.registerProviderFreedListener(providerFreedListener);
        // Verify
        verify(graphMock).registerProviderFreedListener(eq(providerFreedListener));

        // Arrange
        reset(graphMock);
        // Act
        mvpGraph.unregisterProviderFreedListener(providerFreedListener);
        // Verify
        verify(graphMock).unregisterProviderFreedListener(eq(providerFreedListener));

        // Arrange
        reset(graphMock);
        // Act
        mvpGraph.clearOnProviderFreedListeners();
        // Verify
        verify(graphMock).clearOnProviderFreedListeners();
    }

    @Test (expected = RuntimeException.class)
    public void should_throw_out_exceptions_when_registering_component()
            throws ProvideException, ProviderConflictException {
        // Arrange
        MvpGraph.DefaultProviderFinder providerFinder = mock(MvpGraph.DefaultProviderFinder.class);
        mvpGraph.defaultProviderFinder = providerFinder;

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                throw new RuntimeException();
            }
        }).when(providerFinder).register(any(Component.class));

        // Act
        mvpGraph.register(any(Component.class));
    }

    @Test
    public void should_be_able_to_hijack_singleton_cache()
            throws ProvideException, ProviderConflictException {
        ScopeCache scopeCache = mock(ScopeCache.class);

        // Pre-verify
        Assert.assertNotEquals(scopeCache, mvpGraph.singletonScopeCache);

        // Act
        mvpGraph.hijack(scopeCache);

        // Verify
        Assert.assertEquals(scopeCache, mvpGraph.singletonScopeCache);
    }

    @Test
    public void should_be_able_save_and_restore_state_correctly()
            throws ProvideException, ProviderConflictException {
        Bean beanMock = mock(Bean.class);
        Object mockState = mock(Object.class);
        when(beanMock.getModel()).thenReturn(mockState);
        when(beanMock.modelType()).thenReturn(Object.class);

        List<Bean> mvpBeen = new ArrayList();
        mvpBeen.add(beanMock);
        mvpGraph.mvpBeen = mvpBeen;

        final ModelKeeper modelKeeperMock = mock(ModelKeeper.class);

        // Act
        mvpGraph.saveAllModels(modelKeeperMock);

        // Verify
        verify(beanMock, times(1)).getModel();
        verify(beanMock, times(1)).modelType();
        verify(modelKeeperMock).saveModel(eq(mockState), eq(Object.class));

        // Arrange
        reset(modelKeeperMock);

        Object stateMock = mock(Object.class);
        when(modelKeeperMock.retrieveModel(eq(Object.class))).thenReturn(stateMock);

        mvpGraph.restoreAllModels(modelKeeperMock);

        // Verify
        verify(beanMock).restoreModel(eq(stateMock));
    }

    interface UnimplementedInterface{}

    @Test(expected = MvpGraph.Exception.class)
    public void should_raise_mvp_graph_exception_when_inject_on_poke_exception() {
        class View {
            @Inject
            UnimplementedInterface unimplementedInterface;
        }
        mvpGraph.inject(new View());
    }

    @Test(expected = MvpGraph.Exception.class)
    public void should_raise_mvp_graph_exception_when_release_on_poke_exception() {
        class View {
            @Inject
            UnimplementedInterface unimplementedInterface;
        }
        View view = new View();
        view.unimplementedInterface = new UnimplementedInterface() {
        };
        mvpGraph.release(view);
    }

    @Test(expected = MvpGraph.Exception.class)
    public void should_raise_mvp_graph_exception_when_use_on_poke_exception() {
        class View {
            @Inject
            UnimplementedInterface unimplementedInterface;
        }
        mvpGraph.use(UnimplementedInterface.class, new Consumer<UnimplementedInterface>() {
            @Override
            public void consume(UnimplementedInterface instance) {
            }
        });
    }
}
