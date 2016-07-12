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

package com.shipdream.lib.poke;

import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Named;
import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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

    private ScopeCache scopeCache;
    private Component component;
    private Graph graph;

    @Before
    public void setUp() throws Exception {
        scopeCache = new ScopeCache();
        component = new Component("AppSingleton");
        graph = new Graph();
        graph.setRootComponent(component);
    }

    @Test(expected = ProviderConflictException.class)
    public void shouldDetectConflictProviderException() throws PokeException {
        component.register(new ProviderByClassType(Os.class, iOs.class));
        component.register(new ProviderByClassType(Os.class, Android.class));
        component.register(new ProviderByClassType(Os.class, Android.class));
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
        component.register(new ProviderByClassType(Os.class, iOs.class));
        component.register(new ProviderByClassType(Os.class, Android.class));
        component.register(new ProviderByClassType(Os.class, Windows.class));

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
    public void should_retain_instance_in_use_method_until_exit() throws PokeException {
        ScopeCache scopeCache = new ScopeCache();
        component.register(new ProviderByClassType(Os.class, iOs.class));
        component.register(new ProviderByClassType(Os.class, Android.class));
        component.register(new ProviderByClassType(Os.class, Windows.class));

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
    public void should_retain_instance_in_use_method_until_exit_without_qualifier() throws PokeException {
        ScopeCache scopeCache = new ScopeCache();
        component.register(new ProviderByClassType(Os.class, iOs.class));
        component.register(new ProviderByClassType(Os.class, Android.class));
        component.register(new ProviderByClassType(Os.class, Windows.class));

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
    public void should_be_able_to_use_instance_injected_with_qualifier() throws PokeException {
        component.register(new ProviderByClassType(Os.class, iOs.class));
        component.register(new ProviderByClassType(Os.class, Android.class));
        component.register(new ProviderByClassType(Os.class, Windows.class));

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
    public void use_method_should_notify_injection_and_freed() throws PokeException {
        final Provider<Os> provider = new Provider<Os>(Os.class) {
            @Override
            protected Os createInstance() throws ProvideException {
                return new Android();
            }
        };
        component.register(provider);

        class Phone {
            @MyInject
            private Os os;
        }

        final int[] onCreatedCalled = {0};
        final Object[] injected = new Object[1];
        final Provider.ReferencedListener<Os> injectListener = new Provider.ReferencedListener() {
            @Override
            public void onReferenced(Provider provider, Object instance) {
                onCreatedCalled[0]++;
                injected[0] = instance;

                provider.unregisterOnReferencedListener(this);
            }
        };
        provider.registerOnReferencedListener(injectListener);

        final Provider.DereferenceListener osDereferenceListener = mock(Provider.DereferenceListener.class);
        graph.registerDereferencedListener(new Provider.DereferenceListener() {
            @Override
            public <T> void onDereferenced(Provider<T> provider, T instance) {
                if (provider.type() == Os.class && provider.getReferenceCount() == 0) {
                    osDereferenceListener.onDereferenced(provider, instance);
                }
            }
        });

        final Phone phone = new Phone();

        graph.inject(phone, MyInject.class);

        graph.use(Os.class, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                Assert.assertTrue(injected[0] == phone.os);
                Assert.assertEquals(1, onCreatedCalled[0]);

                Assert.assertTrue(phone.os == instance);
                verify(osDereferenceListener, times(0)).onDereferenced(eq(provider), any(Os.class));

                try {
                    graph.release(phone, MyInject.class);
                } catch (ProviderMissingException e) {
                    throw new RuntimeException(e);
                }

                verify(osDereferenceListener, times(0)).onDereferenced(eq(provider), any(Os.class));
            }
        });

        Assert.assertTrue(injected[0] == phone.os);
        Assert.assertEquals(1, onCreatedCalled[0]);

        verify(osDereferenceListener, times(1)).onDereferenced(eq(provider), any(Os.class));

        //OnInject listener has been unregistered so the count should not increment
        graph.inject(phone, MyInject.class);
        Assert.assertEquals(1, onCreatedCalled[0]);
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
    public void use_method_should_inject_fields_recursively() throws PokeException {

        ScopeCache scopeCache = new ScopeCache();
        component.register(new ProviderByClassType(Os.class, SamSungOs.class));
        component.register(new ProviderByClassType(Connector.class, TypeC.class));

        graph.use(Os.class, MyInject.class, new Consumer<Os>() {
            @Override
            public void consume(Os instance) {
                Assert.assertNotNull(((SamSungOs) instance).connector);
            }
        });
    }

    @Test
    public void use_method_should_release_fields_recursively() throws PokeException {
        component.register(new ProviderByClassType(Os.class, SamSungOs.class));
        component.register(new ProviderByClassType(Connector.class, TypeC.class));

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
    public void inject_in_use_method_should_retain_instances() throws PokeException {
        component.register(new ProviderByClassType(Os.class, SamSungOs.class));
        component.register(new ProviderByClassType(Connector.class, TypeC.class));

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
    public void shouldInjectQualifiedWithDifferentInstances() throws PokeException {
        Graph g = new Graph();
        Component c;
        c = new Component(false);
        g.setRootComponent(c);

        c.register(new ProviderByClassType(Os.class, iOs.class));
        c.register(new ProviderByClassType(Os.class, Android.class));
        c.register(new ProviderByClassType(Os.class, Windows.class));

        Device device = new Device();
        g.inject(device, MyInject.class);

        Device device2 = new Device();
        g.inject(device2, MyInject.class);

        Assert.assertEquals(device.ios.getClass(), iOs.class);
        Assert.assertEquals(device.android.getClass(), Android.class);
        Assert.assertEquals(device.windows.getClass(), Windows.class);

        Assert.assertTrue(device.ios != device2.ios);
        Assert.assertTrue(device.android != device2.android);
        Assert.assertTrue(device.windows != device2.windows);
    }

    @Test
    public void shouldInjectQualifiedSingletonInstance() throws PokeException {
        component.register(new ProviderByClassType(Os.class, iOs.class));
        component.register(new ProviderByClassType(Os.class, Android.class));
        component.register(new ProviderByClassType(Os.class, Windows.class));

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

    static class ContainerModule {
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
    public void unscoped_commponent_should_always_create_new_instances() throws PokeException {
        Component unscopedComponenent = new Component(false);
        unscopedComponenent.register(new ContainerModule());
        graph.setRootComponent(unscopedComponenent);

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
    public void namedQualifierShouldBeRecognized() throws PokeException {
        class Library {
            @MyInject
            @Named("A")
            private Book b1;

            @MyInject
            @Named("B")
            private Book b2;
        }

        component.register(new ProviderByClassType(Book.class, BookA.class));
        component.register(new ProviderByClassType(Book.class, BookB.class));

        Library library = new Library();
        graph.inject(library, MyInject.class);

        Assert.assertEquals(library.b1.getClass(), BookA.class);
        Assert.assertEquals(library.b2.getClass(), BookB.class);
    }

    @Test
    public void incorrectNamedQualifierShouldBeRecognized() throws PokeException {
        class Library {
            @MyInject
            @Named("B")
            private Book b1;
        }

        component.register(new ProviderByClassType(Book.class, BookA.class));
        component.register(new ProviderByClassType(Book.class, BookB.class));

        Library library = new Library();
        graph.inject(library, MyInject.class);

        Assert.assertFalse(library.b1.getClass() == BookA.class);
    }

    @Test(expected = ProviderMissingException.class)
    public void badNamedQualifierShouldBeTreatedAsMissing() throws PokeException {
        class Library {
            @MyInject
            @Named("C")
            private Book b1;
        }

        component.register(new ProviderByClassType(Book.class, BookA.class));
        component.register(new ProviderByClassType(Book.class, BookB.class));

        Library library = new Library();
        graph.inject(library, MyInject.class);

        Assert.assertEquals(library.b1.getClass(), BookA.class);
    }

    @Test(expected = ProviderMissingException.class)
    public void badEmptyNamedQualifierShouldBeTreatedAsMissing() throws PokeException {
        class Library {
            //Empty named qualifier is allowed but will be different with any non empty string
            //Named qualifier
            @MyInject
            @Named
            private Book b1;
        }

        component.register(new ProviderByClassType(Book.class, BookA.class));
        component.register(new ProviderByClassType(Book.class, BookB.class));

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
    public void emptyNamedQualifierShouldBeTreatedAsNormalQualifier() throws PokeException {
        class Basket {
            @MyInject
            @Named
            private Food r;

            @MyInject
            private Food w;
        }

        component.register(new ProviderByClassType(Food.class, Rice.class));
        component.register(new ProviderByClassType(Food.class, Wheat.class));

        Basket basket = new Basket();
        graph.inject(basket, MyInject.class);

        Assert.assertEquals(basket.r.getClass(), Rice.class);
        Assert.assertEquals(basket.w.getClass(), Wheat.class);
    }
}
