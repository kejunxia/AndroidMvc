package com.shipdream.lib.poke;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

import javax.inject.Inject;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestGraph extends BaseTestCases {
    @Test
    public void should_get_correct_rootComponent() throws Exception{
        Graph graph = new Graph();
        Component c = new Component();
        Assert.assertNull(graph.getRootComponent());

        graph.setRootComponent(c);
        Assert.assertTrue(graph.getRootComponent() == c);
    }

    @Test
    public void should_call_on_instance_created_callback() throws Exception{
        Graph graph = new Graph();

        Provider.DisposeListener disposeListener = mock(Provider.DisposeListener.class);
        graph.registerDisposeListener(disposeListener);

        graph.setRootComponent(new Component());
        graph.getRootComponent().register(new Object(){
            @Provides
            String string() {
                return "";
            }
        });

        String instance = graph.reference(String.class, null, Inject.class);
        String instance2 = graph.reference(String.class, null, Inject.class);

        graph.dereference(instance, String.class, null, Inject.class);
        verify(disposeListener, times(0)).onDisposed(any(Provider.class), eq(instance));

        graph.dereference(instance2, String.class, null, Inject.class);

        verify(disposeListener, times(1)).onDisposed(any(Provider.class), eq(instance2));

        Assert.assertTrue(instance == instance2);
    }

    @Test
    public void should_call_on_dispose_when_dereference_without_component_cache() throws Exception{
        Graph graph = new Graph();

        Provider.DisposeListener disposeListener = mock(Provider.DisposeListener.class);
        graph.registerDisposeListener(disposeListener);

        //Component without cache, every injection will create a new string instance
        Component c = new Component(false);
        graph.setRootComponent(c);
        graph.getRootComponent().register(new Object(){
            @Provides
            String string() {
                return String.valueOf(new Random().nextDouble());
            }
        });

        String instance = graph.reference(String.class, null, Inject.class);
        String instance2 = graph.reference(String.class, null, Inject.class);

        graph.dereference(instance, String.class, null, Inject.class);
        verify(disposeListener, times(1)).onDisposed(any(Provider.class), eq(instance));

        graph.dereference(instance2, String.class, null, Inject.class);

        verify(disposeListener, times(1)).onDisposed(any(Provider.class), eq(instance2));

        Assert.assertTrue(instance != instance2);
    }

    @Test
    public void should_be_able_to_unregister_dispose_listener() throws Exception{
        Graph graph = new Graph();

        Provider.DisposeListener disposeListener = mock(Provider.DisposeListener.class);
        graph.registerDisposeListener(disposeListener);

        graph.setRootComponent(new Component());
        graph.getRootComponent().register(new Object(){
            @Provides
            String string() {
                return "";
            }
        });

        String instance = graph.reference(String.class, null, Inject.class);

        graph.unregisterDisposeListener(disposeListener);

        graph.dereference(instance, String.class, null, Inject.class);
        verify(disposeListener, times(0)).onDisposed(any(Provider.class), eq(instance));
    }

    @Test
    public void should_be_able_to_clear_dispose_listeners() throws Exception{
        Graph graph = new Graph();

        Provider.DisposeListener disposeListener = mock(Provider.DisposeListener.class);
        graph.registerDisposeListener(disposeListener);

        graph.setRootComponent(new Component());
        graph.getRootComponent().register(new Object(){
            @Provides
            String string() {
                return "";
            }
        });

        String instance = graph.reference(String.class, null, Inject.class);

        graph.clearDisposeListeners();

        graph.dereference(instance, String.class, null, Inject.class);
        verify(disposeListener, times(0)).onDisposed(any(Provider.class), eq(instance));
    }
}
