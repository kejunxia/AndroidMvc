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

package com.shipdream.lib.poke;

import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Named;
import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestInjectionWithQualifier extends BaseTestCases {
    @Qualifier
    @Documented
    @Retention(RUNTIME)
    @interface Google {
    }

    @Qualifier
    @Documented
    @Retention(RUNTIME)
    @interface Microsoft {
    }

    interface Os {
    }

    static class iOs implements Os {
    }

    @Google
    static class Android implements Os {
    }

    @Microsoft
    static class Windows implements Os {
    }

    @Test(expected = ProviderConflictException.class)
    public void shouldDetectConflictProviderException() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        SimpleGraph graph = new SimpleGraph();
        graph.register(Os.class, iOs.class);
        graph.register(Os.class, Android.class);
        graph.register(Os.class, Android.class);
    }

    private static class Device {
        @MyInject
        private Os ios;

        @Google
        @MyInject
        private Os android;

        @Microsoft
        @MyInject
        private Os windows;
    }

    @Test
    public void should_use_cached_instance_if_inject_instance_is_referenced_more_then_once() throws Exception {
        final SimpleGraph graph = new SimpleGraph();
        ScopeCache scopeCache = new ScopeCache();
        graph.register(Os.class, iOs.class, scopeCache);
        graph.register(Os.class, Android.class, scopeCache);
        graph.register(Os.class, Windows.class, scopeCache);

        //Retain = 0

        final Device device = new Device();
        graph.inject(device, MyInject.class);
        //Retain  = 1

        final Device device2 = new Device();
        graph.inject(device2, MyInject.class);
        //Retain = 2

        graph.use(Os.class, null, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                Assert.assertTrue(device.ios == instance);
                Assert.assertTrue(device2.ios == instance);
            }
        });

        graph.release(device, MyInject.class);
        //Retain = 1

        graph.use(Os.class, null, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                Assert.assertTrue(device.ios == instance);
                Assert.assertTrue(device2.ios == instance);
            }
        });

        graph.release(device2, MyInject.class);
        //Retain = 0

        graph.use(Os.class, null, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                Assert.assertTrue(device.ios != null);
                Assert.assertTrue(device.ios != instance);
            }
        });
    }

    @Test
    public void should_retain_instance_in_use_method_until_exit() throws Exception {
        final SimpleGraph graph = new SimpleGraph();
        ScopeCache scopeCache = new ScopeCache();
        graph.register(Os.class, iOs.class, scopeCache);
        graph.register(Os.class, Android.class, scopeCache);
        graph.register(Os.class, Windows.class, scopeCache);

        final Device device = new Device();

        graph.use(Os.class, null, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                try {
                    graph.inject(device, MyInject.class);
                } catch (PokeException e) {
                    throw new RuntimeException(e);
                }

                Assert.assertTrue(device.ios != null);
                Assert.assertTrue(device.ios == instance);
            }
        });

        graph.use(Os.class, null, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                Assert.assertTrue(device.ios == instance);
            }
        });

        graph.release(device, MyInject.class);

        graph.use(Os.class, null, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //New instance has created
                Assert.assertTrue(device.ios != instance);
            }
        });
    }

    @Test
    public void should_retain_instance_in_use_method_until_exit_without_qualifier() throws Exception {
        final SimpleGraph graph = new SimpleGraph();
        ScopeCache scopeCache = new ScopeCache();
        graph.register(Os.class, iOs.class, scopeCache);
        graph.register(Os.class, Android.class, scopeCache);
        graph.register(Os.class, Windows.class, scopeCache);

        final Device device = new Device();

        graph.use(Os.class, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                try {
                    graph.inject(device, MyInject.class);
                } catch (PokeException e) {
                    throw new RuntimeException(e);
                }

                Assert.assertTrue(device.ios != null);
                Assert.assertTrue(device.ios == instance);
            }
        });

        graph.use(Os.class, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                Assert.assertTrue(device.ios == instance);
            }
        });

        graph.release(device, MyInject.class);

        graph.use(Os.class, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                //New instance has created
                Assert.assertTrue(device.ios != instance);
            }
        });
    }

    @Test
    public void should_be_able_to_use_instance_injected_with_qualifier() throws Exception {
        final SimpleGraph graph = new SimpleGraph();
        ScopeCache scopeCache = new ScopeCache();
        graph.register(Os.class, iOs.class, scopeCache);
        graph.register(Os.class, Android.class, scopeCache);
        graph.register(Os.class, Windows.class, scopeCache);

        final Device device = new Device();

        @Google
        class GoogleHolder{}

        graph.use(Os.class, GoogleHolder.class.getAnnotation(Google.class), MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                try {
                    graph.inject(device, MyInject.class);
                } catch (PokeException e) {
                    throw new RuntimeException(e);
                }

                Assert.assertTrue(device.android == instance);
            }
        });
    }

    @Test
    public void use_method_should_notify_injection_and_freed() throws
            ProviderConflictException, ProvideException, CircularDependenciesException, ProviderMissingException {
        final SimpleGraph graph = new SimpleGraph();
        final Provider<Os> provider = new Provider<Os>(Os.class) {
            @Override
            protected Os createInstance() throws ProvideException {
                return new Android();
            }
        };
        provider.setScopeCache(new ScopeCache());
        graph.register(provider);

        class Phone {
            @MyInject
            private Os os;
        }

        final Provider.OnInjectedListener<Os> injectListener = mock(Provider.OnInjectedListener.class);
        provider.registerOnInjectedListener(injectListener);

        final Provider.OnFreedListener osOnFreedListener = mock(Provider.OnFreedListener.class);
        graph.registerProviderFreedListener(osOnFreedListener);

        final Phone phone = new Phone();

        graph.inject(phone, MyInject.class);

        graph.use(Os.class, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                verify(injectListener, times(1)).onInjected(phone.os);

                Assert.assertTrue(phone.os == instance);
                verify(osOnFreedListener, times(0)).onFreed(provider);

                try {
                    graph.release(phone, MyInject.class);
                } catch (ProviderMissingException e) {
                    throw new RuntimeException(e);
                }

                verify(osOnFreedListener, times(0)).onFreed(provider);
            }
        });

        verify(injectListener, times(1)).onInjected(phone.os);

        verify(osOnFreedListener, times(1)).onFreed(provider);
    }

    interface Connector{
    }

    static class SamSungOs implements Os {
        @MyInject
        Connector connector;
    }

    static class TypeC implements Connector{
    }

    @Test
    public void use_method_should_inject_fields_recursively() throws
            ProviderConflictException, ProvideException, CircularDependenciesException, ProviderMissingException {
        final SimpleGraph graph = new SimpleGraph();
        ScopeCache scopeCache = new ScopeCache();
        graph.register(Os.class, SamSungOs.class, scopeCache);
        graph.register(Connector.class, TypeC.class, scopeCache);

        graph.use(Os.class, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                Assert.assertNotNull(((SamSungOs) instance).connector);
            }
        });
    }

    @Test
    public void use_method_should_release_fields_recursively() throws
            ProviderConflictException, ProvideException, CircularDependenciesException, ProviderMissingException {

        final SimpleGraph graph = new SimpleGraph();
        ScopeCache scopeCache = new ScopeCache();
        graph.register(Os.class, SamSungOs.class, scopeCache);
        graph.register(Connector.class, TypeC.class, scopeCache);

        class Phone {
            @MyInject
            private Os os;
        }

        final Phone phone = new Phone();

        class ConnectorHolder {
            Connector connector;
        }

        final ConnectorHolder connectorHolder = new ConnectorHolder();

        graph.use(Os.class, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                Assert.assertNotNull(((SamSungOs) instance).connector);

                connectorHolder.connector = ((SamSungOs) instance).connector;
            }
        });

        graph.inject(phone, MyInject.class);

        Assert.assertTrue(connectorHolder.connector != ((SamSungOs) phone.os).connector);
    }

    @Test
    public void inject_in_use_method_should_retain_instances() throws
            ProviderConflictException, ProvideException, CircularDependenciesException, ProviderMissingException {

        final SimpleGraph graph = new SimpleGraph();
        ScopeCache scopeCache = new ScopeCache();
        graph.register(Os.class, SamSungOs.class, scopeCache);
        graph.register(Connector.class, TypeC.class, scopeCache);

        class Phone {
            @MyInject
            private Os os;
        }

        final Phone phone = new Phone();

        class ConnectorHolder {
            Connector connector;
        }

        final ConnectorHolder connectorHolder = new ConnectorHolder();

        graph.use(Os.class, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                Assert.assertNotNull(((SamSungOs) instance).connector);

                try {
                    graph.inject(phone, MyInject.class);
                } catch (PokeException e) {
                    throw new RuntimeException(e);
                }

                connectorHolder.connector = ((SamSungOs) instance).connector;
            }
        });

        Assert.assertTrue(connectorHolder.connector == ((SamSungOs) phone.os).connector);
    }

    @Test
    public void shouldInjectQualifiedWithDifferentInstances() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        SimpleGraph graph = new SimpleGraph();
        graph.register(Os.class, iOs.class);
        graph.register(Os.class, Android.class);
        graph.register(Os.class, Windows.class);

        Device device = new Device();
        graph.inject(device, MyInject.class);

        Device device2 = new Device();
        graph.inject(device2, MyInject.class);

        Assert.assertEquals(device.ios.getClass(), iOs.class);
        Assert.assertEquals(device.android.getClass(), Android.class);
        Assert.assertEquals(device.windows.getClass(), Windows.class);

        Assert.assertTrue(device.ios != device2.ios);
        Assert.assertTrue(device.android != device2.android);
        Assert.assertTrue(device.windows != device2.windows);
    }

    @Test
    public void shouldInjectQualifiedSingletonInstance() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        SimpleGraph graph = new SimpleGraph();
        ScopeCache scopeCache = new ScopeCache();
        graph.register(Os.class, iOs.class, scopeCache);
        graph.register(Os.class, Android.class, scopeCache);
        graph.register(Os.class, Windows.class, scopeCache);

        Device device = new Device();
        graph.inject(device, MyInject.class);

        Device device2 = new Device();
        graph.inject(device2, MyInject.class);

        Assert.assertEquals(device.ios.getClass(), iOs.class);
        Assert.assertEquals(device.android.getClass(), Android.class);
        Assert.assertEquals(device.windows.getClass(), Windows.class);

        Assert.assertTrue(device.ios == device2.ios);
        Assert.assertTrue(device.android == device2.android);
        Assert.assertTrue(device.windows == device2.windows);
    }

    static class ContainerComponent extends Component {
        @Provides
        public Os providesOs() {
            return new iOs();
        }

        //Mismatch os intentionally to test if provides qualifier overrides qualifier of impl class
        @Microsoft
        @Provides
        public Os providesOs1() {
            return new Android();
        }

        //Mismatch os intentionally to test if provides qualifier overrides qualifier of impl class
        @Google
        @Provides
        public Os providesOs2() {
            return new Windows();
        }
    }

    @Test
    public void componentProvidesQualifierShouldOverrideImplClassQualifier() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        SimpleGraph graph = new SimpleGraph();
        graph.register(new ContainerComponent());

        Device device = new Device();
        graph.inject(device, MyInject.class);

        Device device2 = new Device();
        graph.inject(device2, MyInject.class);

        Assert.assertEquals(device.ios.getClass(), iOs.class);
        Assert.assertEquals(device.android.getClass(), Windows.class);
        Assert.assertEquals(device.windows.getClass(), Android.class);

        Assert.assertTrue(device.ios != device2.ios);
        Assert.assertTrue(device.android != device2.android);
        Assert.assertTrue(device.windows != device2.windows);
    }

    interface Book {
    }

    @Named("A")
    static class BookA implements Book {
    }

    @Named("B")
    static class BookB implements Book {
    }

    @Test
    public void namedQualifierShouldBeRecognized() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        class Library {
            @MyInject
            @Named("A")
            private Book b1;

            @MyInject
            @Named("B")
            private Book b2;
        }

        SimpleGraph graph = new SimpleGraph();
        graph.register(Book.class, BookA.class);
        graph.register(Book.class, BookB.class);

        Library library = new Library();
        graph.inject(library, MyInject.class);

        Assert.assertEquals(library.b1.getClass(), BookA.class);
        Assert.assertEquals(library.b2.getClass(), BookB.class);
    }

    @Test
    public void incorrectNamedQualifierShouldBeRecognized() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        class Library {
            @MyInject
            @Named("B")
            private Book b1;
        }

        SimpleGraph graph = new SimpleGraph();
        graph.register(Book.class, BookA.class);
        graph.register(Book.class, BookB.class);

        Library library = new Library();
        graph.inject(library, MyInject.class);

        Assert.assertFalse(library.b1.getClass() == BookA.class);
    }

    @Test(expected = ProviderMissingException.class)
    public void badNamedQualifierShouldBeTreatedAsMissing() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        class Library {
            @MyInject
            @Named("C")
            private Book b1;
        }

        SimpleGraph graph = new SimpleGraph();
        graph.register(Book.class, BookA.class);
        graph.register(Book.class, BookB.class);

        Library library = new Library();
        graph.inject(library, MyInject.class);

        Assert.assertEquals(library.b1.getClass(), BookA.class);
    }

    @Test(expected = ProviderMissingException.class)
    public void badEmptyNamedQualifierShouldBeTreatedAsMissing() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        class Library {
            //Empty named qualifier is allowed but will be different with any non empty string
            //Named qualifier
            @MyInject
            @Named
            private Book b1;
        }

        SimpleGraph graph = new SimpleGraph();
        graph.register(Book.class, BookA.class);
        graph.register(Book.class, BookB.class);

        Library library = new Library();
        graph.inject(library, MyInject.class);

        Assert.assertEquals(library.b1.getClass(), BookA.class);
    }

    interface Food {
    }

    @Named
    static class Rice implements Food {
    }

    static class Wheat implements Food {
    }

    @Test
    public void emptyNamedQualifierShouldBeTreatedAsNormalQualifier() throws ProviderConflictException,
            ProvideException, CircularDependenciesException, ProviderMissingException {
        class Basket {
            @MyInject
            @Named
            private Food r;

            @MyInject
            private Food w;
        }

        SimpleGraph graph = new SimpleGraph();
        graph.register(Food.class, Rice.class);
        graph.register(Food.class, Wheat.class);

        Basket basket = new Basket();
        graph.inject(basket, MyInject.class);

        Assert.assertEquals(basket.r.getClass(), Rice.class);
        Assert.assertEquals(basket.w.getClass(), Wheat.class);
    }
}
