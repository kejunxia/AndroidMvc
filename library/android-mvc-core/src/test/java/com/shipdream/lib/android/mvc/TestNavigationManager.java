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

import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.TimerController;
import com.shipdream.lib.android.mvc.manager.internal.BaseNavigationManagerTest;
import com.shipdream.lib.poke.Consumer;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestNavigationManager extends BaseNavigationManagerTest {
    //Define a subscriber class
    class ForwardListener {
        public void onEvent(NavigationManager.Event.OnLocationForward event) {
        }
    }

    class BackListener {
        public void onEvent(NavigationManager.Event.OnLocationBack event) {
        }
    }

    abstract class Controller1 extends Controller {
    }
    abstract class Controller2 extends Controller {
    }
    abstract class Controller3 extends Controller {
    }
    abstract class Controller4 extends Controller {
    }
    abstract class Controller5 extends Controller {
    }


    private Class<? extends Controller> locId1 = Controller1.class;
    private Class<? extends Controller> locId2 = Controller2.class;
    private Class<? extends Controller> locId3 = Controller3.class;
    private Class<? extends Controller> locId4 = Controller4.class;
    private Class<? extends Controller> locId5 = Controller5.class;


    protected void navigateBackByFragment() {
        navigationManager.navigate(this).back();
    }

    @Test
    public void should_not_fail_when_logger_is_set_trace() {
        navigationManager.logger = mock(Logger.class);
        when(navigationManager.logger.isTraceEnabled()).thenReturn(true);

        prepareLocationHistory();
        navigationManager.navigate(this).back();
    }

    interface X{
    }

    @Test(expected = MvcGraphException.class)
    public void should_throw_MvcGraphException_when_mvcGraph_with_method_encounters_PokeException() {
        navigationManager.navigate(this).with(X.class);
    }

    class X_1 implements X {

    }

    @Test
    public void should_throw_MvcGraphException_when_mvcGraph_destroy_method_encounters_PokeException() throws ProvideException, ProviderConflictException, ProviderMissingException {
        navigationManager.logger = mock(Logger.class);
        Navigator navigator = navigationManager.navigate(this);

        Mvc.graph().getRootComponent().register(new Object(){
            @Provides
            public X x() {
                return new X_1();
            }
        });

        navigator.with(X.class);

        Mvc.graph().getRootComponent().unregister(X.class, null);

        navigator.destroy();

        verify(navigationManager.logger).warn(anyString(), anyString());
    }

    @Test
    public void shouldClearNavigationHistoryUpToSpecified() throws Exception {
        ForwardListener forwardListener = prepareLocationHistory();
        //loc1 -> loc2 -> loc3 -> loc4

        navigationManager.navigate(this).to(locId2);
        navigationManager.navigate(this).to(locId4);
        navigationManager.navigate(this).to(locId1);
        //loc1 -> loc2 -> loc3 -> loc4 -> loc2 -> loc4 -> loc1

        reset(forwardListener);
        navigationManager.navigate(this).to(locId5, new Forwarder().clearTo(locId3));
        //loc1 -> loc2 -> loc3 -> loc5
        ArgumentCaptor<NavigationManager.Event.OnLocationForward> event
                = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationForward.class);
        verify(forwardListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId1.getName());
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId5.getName());
        assertEquals(event.getValue().getLocationWhereHistoryClearedUpTo().getLocationId(), locId3.getName());
        assertEquals(event.getValue().isClearHistory(), true);

        NavigationManager.Model model = navigationManager.getModel();
        NavLocation curLoc = model.getCurrentLocation();
        assertEquals(curLoc.getLocationId(), locId5.getName());
        curLoc = curLoc.getPreviousLocation();
        assertEquals(curLoc.getLocationId(), locId3.getName());
        curLoc = curLoc.getPreviousLocation();
        assertEquals(curLoc.getLocationId(), locId2.getName());
        curLoc = curLoc.getPreviousLocation();
        assertEquals(curLoc.getLocationId(), locId1.getName());
        curLoc = curLoc.getPreviousLocation();
        assertEquals(curLoc, null);

        //loc1 -> loc2 -> loc3 -> loc5
        NavLocation currentLoc = navigationManager.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId5.getName());
        assertEquals(currentLoc.getPreviousLocation().getLocationId(), locId3.getName());
        assertEquals(currentLoc.getPreviousLocation().getPreviousLocation().getLocationId(), locId2.getName());
        assertEquals(currentLoc.getPreviousLocation().getPreviousLocation().getPreviousLocation().getLocationId(), locId1.getName());
        assertEquals(currentLoc.getPreviousLocation().getPreviousLocation().getPreviousLocation().getPreviousLocation(), null);
    }

    @Test
    public void shouldClearAllNavigationHistory() throws Exception {
        ForwardListener forwardListener = prepareLocationHistory();

        reset(forwardListener);
        navigationManager.navigate(this).to(locId5, new Forwarder().clearAll());

        ArgumentCaptor<NavigationManager.Event.OnLocationForward> event
                = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationForward.class);
        verify(forwardListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId4.getName());
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId5.getName());

        assertEquals(event.getValue().getLocationWhereHistoryClearedUpTo(), null);
        assertEquals(event.getValue().isClearHistory(), true);

        //Now the history should be loc1->loc2->loc5
        NavLocation currentLoc = navigationManager.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId5.getName());
        assertEquals(currentLoc.getPreviousLocation(), null);
    }

    @Test
    public void shouldBeAbleToNavigateBackOneByOne() throws Exception {
        //mock the subscriber
        BackListener backListener = mock(BackListener.class);
        eventBusC.register(backListener);

        prepareLocationHistory();

        reset(backListener);
        navigateBackByFragment();
        ArgumentCaptor<NavigationManager.Event.OnLocationBack> event
                = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationBack.class);
        verify(backListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId4.getName());
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId3.getName());
        NavLocation currentLoc = navigationManager.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId3.getName());
        assertEquals(currentLoc.getPreviousLocation().getLocationId(), locId2.getName());
        assertEquals(currentLoc.getPreviousLocation().getPreviousLocation().getLocationId(), locId1.getName());
        assertEquals(currentLoc.getPreviousLocation().getPreviousLocation().getPreviousLocation(), null);
        Assert.assertFalse(event.getValue().isFastRewind());

        reset(backListener);
        navigateBackByFragment();
        event = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationBack.class);
        verify(backListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId3.getName());
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId2.getName());
        currentLoc = navigationManager.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId2.getName());
        assertEquals(currentLoc.getPreviousLocation().getLocationId(), locId1.getName());
        assertEquals(currentLoc.getPreviousLocation().getPreviousLocation(), null);
        Assert.assertFalse(event.getValue().isFastRewind());

        reset(backListener);
        navigateBackByFragment();
        event = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationBack.class);
        verify(backListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId2.getName());
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId1.getName());
        currentLoc = navigationManager.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId1.getName());
        assertEquals(currentLoc.getPreviousLocation(), null);
        Assert.assertFalse(event.getValue().isFastRewind());

        reset(backListener);
        navigateBackByFragment();
        event = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationBack.class);
        verify(backListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId1.getName());
        assertEquals(event.getValue().getCurrentValue(), null);
        currentLoc = navigationManager.getModel().getCurrentLocation();
        assertEquals(currentLoc, null);
        Assert.assertFalse(event.getValue().isFastRewind());

        //has already reached the start of the navigation, should not be able to navigate back any more
        reset(backListener);
        navigateBackByFragment();
        event = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationBack.class);
        verify(backListener, times(0)).onEvent(event.capture());
    }

    @Test
    public void shouldBeAbleToNavigateBackToGivenLocation() throws Exception {
        //mock the subscriber
        BackListener backListener = mock(BackListener.class);
        eventBusC.register(backListener);

        prepareLocationHistory();

        reset(backListener);
        navigationManager.navigate(this).back(locId2);
        ArgumentCaptor<NavigationManager.Event.OnLocationBack> event
                = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationBack.class);
        verify(backListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId4.getName());
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId2.getName());
        NavLocation currentLoc = navigationManager.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId2.getName());
        assertEquals(currentLoc.getPreviousLocation().getLocationId(), locId1.getName());
        assertEquals(currentLoc.getPreviousLocation().getPreviousLocation(), null);

        Assert.assertTrue(event.getValue().isFastRewind());
    }

    @Test
    public void shouldBeAbleToNavigateBackToFirstLocation() throws Exception {
        //mock the subscriber
        BackListener backListener = mock(BackListener.class);
        eventBusC.register(backListener);

        prepareLocationHistory();

        reset(backListener);
        Navigator navigator = navigationManager.navigate(this);
        navigator.back(null);
        ArgumentCaptor<NavigationManager.Event.OnLocationBack> event
                = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationBack.class);
        verify(backListener).onEvent(event.capture());
        Assert.assertTrue(this == event.getValue().getSender());
        Assert.assertTrue(navigator == event.getValue().getNavigator());

        assertEquals(event.getValue().getLastValue().getLocationId(), locId4.getName());
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId1.getName());
        NavLocation currentLoc = navigationManager.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId1.getName());
        assertEquals(currentLoc.getPreviousLocation(), null);

        Assert.assertTrue(event.getValue().isFastRewind());
    }

    @Test
    public void should_skip_interim_location_on_back_navigation() throws Exception {
        //mock the subscriber
        BackListener backListener = mock(BackListener.class);
        eventBusC.register(backListener);

        prepareLocationHistory();

        reset(backListener);
        navigationManager.navigate(this).to(locId1, new Forwarder().setInterim(true));
        navigationManager.navigate(this).to(locId2, new Forwarder().setInterim(true));
        navigationManager.navigate(this).back();

        ArgumentCaptor<NavigationManager.Event.OnLocationBack> event
                = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationBack.class);
        verify(backListener).onEvent(event.capture());

        assertEquals(event.getValue().getLastValue().getLocationId(), locId2.getName());
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId4.getName());
        NavLocation currentLoc = navigationManager.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId4.getName());

        Assert.assertTrue(event.getValue().isFastRewind());
    }

    @Test
    public void should_post_app_exit_event_on_the_last_back_of_linear_back_navigation() {
        //mock the subscriber
        class AppExitListener {
            public void onEvent(NavigationManager.Event.OnAppExit event) {
            }
        }

        ArgumentCaptor<NavigationManager.Event.OnAppExit> event
                = ArgumentCaptor.forClass(NavigationManager.Event.OnAppExit.class);

        AppExitListener exitListener = mock(AppExitListener.class);
        eventBusC.register(exitListener);

        prepareLocationHistory();

        reset(exitListener);
        navigateBackByFragment();
        verify(exitListener, times(0)).onEvent(event.capture());

        navigateBackByFragment();
        verify(exitListener, times(0)).onEvent(event.capture());

        navigateBackByFragment();
        verify(exitListener, times(0)).onEvent(event.capture());

        navigateBackByFragment();
        verify(exitListener, times(1)).onEvent(event.capture());
    }

    @Test
    public void should_post_app_exit_event_on_the_last_back_of_fast_back_navigation() {
        //mock the subscriber
        class AppExitListener {
            public void onEvent(NavigationManager.Event.OnAppExit event) {
            }
        }

        ArgumentCaptor<NavigationManager.Event.OnAppExit> event
                = ArgumentCaptor.forClass(NavigationManager.Event.OnAppExit.class);

        AppExitListener exitListener = mock(AppExitListener.class);
        eventBusC.register(exitListener);

        prepareLocationHistory();

        reset(exitListener);
        navigationManager.navigate(this).back(null);
        verify(exitListener, times(0)).onEvent(event.capture());

        navigateBackByFragment();
        verify(exitListener, times(1)).onEvent(event.capture());

        Assert.assertTrue(this == event.getValue().getSender());
    }

    @Test
    public void should_not_raise_navigate_back_event_when_navigate_to_first_location_from_the_first_location() throws Exception {
        // Arrange
        BackListener backListener = mock(BackListener.class);
        eventBusC.register(backListener);

        navigationManager.navigate(this).to(locId1);

        // Arrange
        Navigator navigator = navigationManager.navigate(this);
        navigator.back(null);

        // Verify
        ArgumentCaptor<NavigationManager.Event.OnLocationBack> event
                = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationBack.class);
        verify(backListener, times(0)).onEvent(event.capture());
    }

    @Test
    public void should_not_raise_navigate_back_event_when_navigate_to_unknown_location() throws Exception {
        // Arrange
        BackListener backListener = mock(BackListener.class);
        eventBusC.register(backListener);

        prepareLocationHistory();

        // Arrange
        navigationManager.navigate(this).back(Controller.class);

        // Verify
        ArgumentCaptor<NavigationManager.Event.OnLocationBack> event
                = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationBack.class);
        verify(backListener, times(0)).onEvent(event.capture());
    }

    @Test
    public void should_not_raise_navigate_back_event_when_fast_back_navigate_from_null_location() throws Exception {
        // Arrange
        BackListener backListener = mock(BackListener.class);
        eventBusC.register(backListener);

        // Arrange
        navigationManager.navigate(this).back(Controller1.class);

        // Verify
        ArgumentCaptor<NavigationManager.Event.OnLocationBack> event
                = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationBack.class);
        verify(backListener, times(0)).onEvent(event.capture());
    }

    @Test
    public void should_be_able_to_log_navigation_history_with_trace_enabled() throws Exception {
        // Arrange
        Logger logger = mock(Logger.class);
        when(logger.isTraceEnabled()).thenReturn(true);
        navigationManager.dumpHistoryOnLocationChange = true;
        navigationManager.logger = logger;

        // Act
        navigationManager.navigate(this).to(Controller1.class, new Forwarder().clearTo(Controller.class));

        // Verify
        verify(logger, atLeast(1)).trace(anyString());

        // Arrange
        reset(logger);

        // Act
        navigationManager.navigate(this).to(Controller1.class);

        // Verify
        verify(logger, atLeast(1)).trace(anyString());

        // Arrange
        reset(logger);

        // Act
        navigateBackByFragment();

        // Verify
        verify(logger, atLeast(1)).trace(anyString());

        // Arrange
        reset(logger);
        navigationManager.navigate(this).to(Controller1.class);

        // Act
        navigationManager.navigate(this).back(null);

        // Verify
        verify(logger, atLeast(1)).trace(anyString());
    }

    @Test
    public void should_not_log_navigation_history_without_trace_eanbled() throws Exception {
        // Arrange
        Logger logger = mock(Logger.class);
        when(logger.isTraceEnabled()).thenReturn(false);
        navigationManager.dumpHistoryOnLocationChange = true;
        navigationManager.logger = logger;

        // Act
        navigationManager.navigate(this).to(Controller1.class, new Forwarder().clearTo(Controller.class));

        // Verify
        verify(logger, atLeast(0)).trace(anyString());

        // Arrange
        reset(logger);

        // Act
        navigationManager.navigate(this).to(Controller1.class);

        // Verify
        verify(logger, atLeast(0)).trace(anyString());

        // Arrange
        reset(logger);

        // Act
        navigateBackByFragment();

        // Verify
        verify(logger, atLeast(0)).trace(anyString());

        // Arrange
        reset(logger);
        navigationManager.navigate(this).to(Controller1.class);

        // Act
        navigationManager.navigate(this).back(null);

        // Verify
        verify(logger, atLeast(0)).trace(anyString());
    }

    private static class TimerFragmentX2 {
        @Inject
        @Slower2
        private TimerController timerController;
    }

    private static class TimerFragmentX3 {
        @Inject
        @Slower3
        private TimerController timerController;
    }

    @Qualifier
    @Retention(RUNTIME)
    @interface Slower2 {
    }

    @Qualifier
    @Retention(RUNTIME)
    @interface Slower3 {
    }

    @Slower2
    @Slower3
    static class SlowXHolder {

    }

    @Test(expected = MvcGraphException.class)
    public void should_catch_invocation_exception_when_NPE_detected_on_injection() throws Exception {
        Object com = new Object() {
            @Provides
            @Singleton
            @Slower2
            TimerController timerSlowerX2() {
                return new TimerController() {
                    {
                        onCreated();
                    }

                    @Override
                    public void setInitialValue(long value) {
                        super.setInitialValue(value * 2);
                    }
                };
            }
        };
        graph.getRootComponent().register(com);

        Annotation slower2Qualifier = SlowXHolder.class.getAnnotation(Slower2.class);

        graph.use(TimerController.class, slower2Qualifier, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should have now been released
                Assert.assertEquals(0, instance.getInitialValue());
            }
        });

        graph.getRootComponent().unregister(com);
    }

    @Test
    public void should_retain_prepared_instance_until_navigation_settled() throws Exception {
        // Arrange
        final long fiveMinutes = 60 * 5;
         Object module = new Object() {
            @Provides
            @Slower2
            TimerController timerSlowerX2() {
                return new TimerController() {
                    {
                        try {
                            onCreated();
                        } catch (Exception e) {
                        }

                    }

                    @Override
                    public void setInitialValue(long value) {
                        super.setInitialValue(value * 2);
                    }
                };
            }

            @Provides
            @Slower3
            TimerController timerSlowerX3() {
                return new TimerController() {
                    {
                        try {
                            onCreated();
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void setInitialValue(long value) {
                        super.setInitialValue(value * 3);
                    }
                };
            }
        };

        Mvc.graph().getRootComponent().register(module);

        Annotation slower2Qualifier = SlowXHolder.class.getAnnotation(Slower2.class);
        Annotation slower3Qualifier = SlowXHolder.class.getAnnotation(Slower3.class);

        Mvc.graph().use(TimerController.class, slower2Qualifier, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should have now been released
                Assert.assertEquals(0, instance.getInitialValue());
            }
        });

        // Act
        Navigator navigator = navigationManager.navigate(this).with(TimerController.class, slower2Qualifier, new Preparer<TimerController>() {
            @Override
            public void prepare(TimerController instance) {
                instance.setInitialValue(fiveMinutes);
            }
        });
        navigator.to(TimerController.class);

        Mvc.graph().use(TimerController.class, slower2Qualifier, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should not have been released yet
                Assert.assertEquals(fiveMinutes * 2, instance.getInitialValue());
            }
        });

        //destroy the navigator
        navigator.destroy();

        Mvc.graph().use(TimerController.class, slower2Qualifier, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should have now been released
                Assert.assertEquals(0, instance.getInitialValue());
            }
        });

        //Test fragment 3
        Mvc.graph().use(TimerController.class, slower3Qualifier, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should have now been released
                Assert.assertEquals(0, instance.getInitialValue());
            }
        });

        navigator = navigationManager.navigate(this).with(TimerController.class, slower3Qualifier, new Preparer<TimerController>() {
            @Override
            public void prepare(TimerController instance) {
                instance.setInitialValue(fiveMinutes);
            }
        });
        navigator.to(TimerController.class);

        Mvc.graph().use(TimerController.class, slower3Qualifier, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should not have been released yet
                Assert.assertEquals(fiveMinutes * 3, instance.getInitialValue());
            }
        });

        //destroy the navigator
        navigator.destroy();

        Mvc.graph().use(TimerController.class, slower3Qualifier, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should have now been released
                Assert.assertEquals(0, instance.getInitialValue());
            }
        });

        Mvc.graph().getRootComponent().unregister(module);
    }

    private static class TimerFragment {
        @Inject
        private TimerController timerController;
    }

    @Test
    public void should_retain_prepared_instance_until_navigation_settled_without_qualifier() throws Exception {
        // Arrange
        final long fiveMinutes = 60 * 5;

        // Act
        Navigator navigator = navigationManager.navigate(this).with(TimerController.class, new Preparer<TimerController>() {
            @Override
            public void prepare(TimerController instance) {
                instance.setInitialValue(fiveMinutes);
            }
        });
        navigator.to(TimerController.class);

        Mvc.graph().use(TimerController.class, null, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should not have been released yet
                Assert.assertEquals(fiveMinutes, instance.getInitialValue());
            }
        });

        //destroy the navigator
        navigator.destroy();

        Mvc.graph().use(TimerController.class, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should have now been released
                Assert.assertEquals(0, instance.getInitialValue());
            }
        });
    }

    @Test
    public void should_invoke_singular_argument_with_method_of_navigator_correct() throws Exception {
        Navigator navigator = navigationManager.navigate(this);
        Navigator spiedNavigator = spy(navigator);

        verify(spiedNavigator, times(0)).with(eq(NavigationManager.class), isNull(Annotation.class), isNull(Preparer.class));

        spiedNavigator.with(NavigationManager.class);

        verify(spiedNavigator).with(eq(NavigationManager.class), isNull(Annotation.class), isNull(Preparer.class));
    }

    @Test
    public void should_return_correct_sender_by_navigator() throws Exception {
        // Act
        Navigator navigator = navigationManager.navigate(this);

        Assert.assertTrue(this == navigator.getSender());
    }

    @Test
    public void should_invoke_on_settled_when_navigation_is_done() throws Exception {
        // Arrange
        Navigator.OnSettled onSettled = mock(Navigator.OnSettled.class);

        // Act
        Navigator navigator = navigationManager.navigate(this).with(TimerController.class)
                .onSettled(onSettled);
        navigator.to(TimerController.class);

        verify(onSettled, times(0)).run();

        //destroy the navigator
        navigator.destroy();

        verify(onSettled, times(1)).run();
    }

    @Test
    public void interim_forwarder_should_instruct_navigation_not_push_location_to_back_stack(){
        ForwardListener forwardListener = mock(ForwardListener.class);
        eventBusC.register(forwardListener);

        // Act
        Navigator navigator = navigationManager.navigate(this);
        Forwarder forwarder = new Forwarder();

        Assert.assertFalse(forwarder.isInterim());

        forwarder.setInterim(true);
        Assert.assertTrue(forwarder.isInterim());

        navigator.to(TimerController.class, forwarder);

        ArgumentCaptor<NavigationManager.Event.OnLocationForward> event
                = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationForward.class);

        verify(forwardListener).onEvent(event.capture());
        assertEquals(event.getValue().getCurrentValue().getLocationId(), TimerController.class.getName());
        assertTrue(event.getValue().getCurrentValue().isInterim());
        Assert.assertTrue(this == event.getValue().getSender());
        Assert.assertTrue(navigator == event.getValue().getNavigator());
    }

    /**
     * Prepare 4 locations in the history
     */
    private ForwardListener prepareLocationHistory() {
        //mock the subscriber
        ForwardListener forwardListener = mock(ForwardListener.class);
        eventBusC.register(forwardListener);

        navigationManager.navigate(this).to(locId1);

        ArgumentCaptor<NavigationManager.Event.OnLocationForward> event =
                ArgumentCaptor.forClass(NavigationManager.Event.OnLocationForward.class);
        verify(forwardListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue(), null);
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId1.getName());

        assertEquals(event.getValue().getLocationWhereHistoryClearedUpTo(), null);
        assertEquals(event.getValue().isClearHistory(), false);

        reset(forwardListener);
        navigationManager.navigate(this).to(locId2);

        event = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationForward.class);
        verify(forwardListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId1.getName());
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId2.getName());

        assertEquals(event.getValue().getLocationWhereHistoryClearedUpTo(), null);
        assertEquals(event.getValue().isClearHistory(), false);

        reset(forwardListener);
        navigationManager.navigate(this).to(locId3);

        event = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationForward.class);
        verify(forwardListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId2.getName());
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId3.getName());

        assertEquals(event.getValue().getLocationWhereHistoryClearedUpTo(), null);
        assertEquals(event.getValue().isClearHistory(), false);

        reset(forwardListener);
        navigationManager.navigate(this).to(locId4);

        event = ArgumentCaptor.forClass(NavigationManager.Event.OnLocationForward.class);
        verify(forwardListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId3.getName());
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId4.getName());

        assertEquals(event.getValue().getLocationWhereHistoryClearedUpTo(), null);
        assertEquals(event.getValue().isClearHistory(), false);

        return forwardListener;
    }

}
