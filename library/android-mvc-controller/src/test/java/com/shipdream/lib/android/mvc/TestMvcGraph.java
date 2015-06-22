package com.shipdream.lib.android.mvc;

import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Graph;
import com.shipdream.lib.poke.ScopeCache;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestMvcGraph {
    private MvcGraph mvcGraph;
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

        mvcGraph = new MvcGraph(new MvcGraph.BaseDependencies() {
            @Override
            protected ExecutorService createExecutorService() {
                return executorService;
            }
        });
    }

    @Test
    public void should_delegate_mvc_graph_properly() throws ProvideException, ProviderConflictException {
        // Arrange
        Graph graphMock = mock(Graph.class);
        mvcGraph.graph = graphMock;

        // Act
        Graph.Monitor monitor = mock(Graph.Monitor.class);
        mvcGraph.registerMonitor(monitor);
        // Verify
        verify(graphMock).registerMonitor(eq(monitor));

        // Arrange
        reset(graphMock);
        // Act
        mvcGraph.unregisterMonitor(monitor);
        // Verify
        verify(graphMock).unregisterMonitor(eq(monitor));

        // Arrange
        reset(graphMock);
        // Act
        mvcGraph.clearMonitors();
        // Verify
        verify(graphMock).clearMonitors();

        // Arrange
        reset(graphMock);
        Graph.OnProviderFreedListener providerFreedListener = mock(Graph.OnProviderFreedListener.class);
        // Act
        mvcGraph.registerProviderFreedListener(providerFreedListener);
        // Verify
        verify(graphMock).registerProviderFreedListener(eq(providerFreedListener));

        // Arrange
        reset(graphMock);
        // Act
        mvcGraph.unregisterProviderFreedListener(providerFreedListener);
        // Verify
        verify(graphMock).unregisterProviderFreedListener(eq(providerFreedListener));

        // Arrange
        reset(graphMock);
        // Act
        mvcGraph.clearOnProviderFreedListeners();
        // Verify
        verify(graphMock).clearOnProviderFreedListeners();
    }

    @Test (expected = RuntimeException.class)
    public void should_throw_out_exceptions_when_registering_component()
            throws ProvideException, ProviderConflictException {
        // Arrange
        MvcGraph.DefaultProviderFinder providerFinder = mock(MvcGraph.DefaultProviderFinder.class);
        mvcGraph.defaultProviderFinder = providerFinder;

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                throw new RuntimeException();
            }
        }).when(providerFinder).register(any(Component.class));

        // Act
        mvcGraph.register(any(Component.class));
    }

    @Test
    public void should_be_able_to_hijack_singleton_cache()
            throws ProvideException, ProviderConflictException {
        ScopeCache scopeCache = mock(ScopeCache.class);

        // Pre-verify
        Assert.assertNotEquals(scopeCache, mvcGraph.singletonScopeCache);

        // Act
        mvcGraph.hijack(scopeCache);

        // Verify
        Assert.assertEquals(scopeCache, mvcGraph.singletonScopeCache);
    }

    @Test
    public void should_be_able_save_and_restore_state_correctly()
            throws ProvideException, ProviderConflictException {
        final StateManaged stateManagedMock = mock(StateManaged.class);
        Object mockState = mock(Object.class);
        when(stateManagedMock.getState()).thenReturn(mockState);
        when(stateManagedMock.getStateType()).thenReturn(Object.class);

        List<StateManaged> stateManagedList = new ArrayList();
        stateManagedList.add(stateManagedMock);
        mvcGraph.stateManagedObjects = stateManagedList;

        final StateKeeper stateKeeperMock = mock(StateKeeper.class);

        // Act
        mvcGraph.saveAllStates(stateKeeperMock);

        // Verify
        verify(stateManagedMock, times(1)).getState();
        verify(stateManagedMock, times(1)).getStateType();
        verify(stateKeeperMock).saveState(eq(mockState), eq(Object.class));

        // Arrange
        reset(stateKeeperMock);

        Object stateMock = mock(Object.class);
        when(stateKeeperMock.getState(eq(Object.class))).thenReturn(stateMock);

        mvcGraph.restoreAllStates(stateKeeperMock);

        // Verify
        verify(stateManagedMock).restoreState(eq(stateMock));
    }
}
