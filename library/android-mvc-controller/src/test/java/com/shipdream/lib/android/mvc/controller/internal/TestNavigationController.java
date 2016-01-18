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

package com.shipdream.lib.android.mvc.controller.internal;

import com.shipdream.lib.android.mvc.Injector;
import com.shipdream.lib.android.mvc.MvcGraphException;
import com.shipdream.lib.android.mvc.NavLocation;
import com.shipdream.lib.android.mvc.controller.BaseNavigationControllerTest;
import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.TimerController;
import com.shipdream.lib.android.mvc.inject.testNameMapping.controller.internal.TimerControllerImpl;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Consumer;
import com.shipdream.lib.poke.Provides;

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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestNavigationController extends BaseNavigationControllerTest {
    //Define a subscriber class
    class ForwardListener {
        public void onEvent(NavigationController.EventC2V.OnLocationForward event) {}
    }

    class BackListener {
        public void onEvent(NavigationController.EventC2V.OnLocationBack event) {}
    }

    private String locId1 = "LocId1";
    private String locId2 = "LocId2";
    private String locId3 = "LocId3";
    private String locId4 = "LocId4";
    private String locId5 = "LocId5";

    @Test
    public void shouldClearNavigationHistoryUpToSpecified() throws Exception {
        ForwardListener forwardListener = prepareLocationHistory();
        //loc1 -> loc2 -> loc3 -> loc4

        navigationController.navigateTo(this, locId2);
        navigationController.navigateTo(this, locId4);
        navigationController.navigateTo(this, locId1);
        //loc1 -> loc2 -> loc3 -> loc4 -> loc2 -> loc4 -> loc1

        reset(forwardListener);
        navigationController.navigateTo(this, locId5, locId3);
        //loc1 -> loc2 -> loc3 -> loc5
        ArgumentCaptor<NavigationController.EventC2V.OnLocationForward> event
                = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationForward.class);
        verify(forwardListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId1);
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId5);
        assertEquals(event.getValue().getLocationWhereHistoryClearedUpTo().getLocationId(), locId3);
        assertEquals(event.getValue().isClearHistory(), true);

        NavigationController.Model model = navigationController.getModel();
        NavLocation curLoc = model.getCurrentLocation();
        assertEquals(curLoc.getLocationId(), locId5);
        curLoc = curLoc.getPreviousLocation();
        assertEquals(curLoc.getLocationId(), locId3);
        curLoc = curLoc.getPreviousLocation();
        assertEquals(curLoc.getLocationId(), locId2);
        curLoc = curLoc.getPreviousLocation();
        assertEquals(curLoc.getLocationId(), locId1);
        curLoc = curLoc.getPreviousLocation();
        assertEquals(curLoc, null);

        //loc1 -> loc2 -> loc3 -> loc5
        NavLocation currentLoc = navigationController.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId5);
        assertEquals(currentLoc.getPreviousLocation().getLocationId(), locId3);
        assertEquals(currentLoc.getPreviousLocation().getPreviousLocation().getLocationId(), locId2);
        assertEquals(currentLoc.getPreviousLocation().getPreviousLocation().getPreviousLocation().getLocationId(), locId1);
        assertEquals(currentLoc.getPreviousLocation().getPreviousLocation().getPreviousLocation().getPreviousLocation(), null);
    }

    @Test
    public void shouldClearAllNavigationHistory() throws Exception {
        ForwardListener forwardListener = prepareLocationHistory();

        reset(forwardListener);
        navigationController.navigateTo(this, locId5, null);

        ArgumentCaptor<NavigationController.EventC2V.OnLocationForward> event
                = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationForward.class);
        verify(forwardListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId4);
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId5);

        assertEquals(event.getValue().getLocationWhereHistoryClearedUpTo(), null);
        assertEquals(event.getValue().isClearHistory(), true);

        //Now the history should be loc1->loc2->loc5
        NavLocation currentLoc = navigationController.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId5);
        assertEquals(currentLoc.getPreviousLocation(), null);
    }

    @Test
    public void shouldBeAbleToNavigateBackOneByOne() throws Exception {
        //mock the subscriber
        BackListener backListener = mock(BackListener.class);
        eventBusV.register(backListener);

        prepareLocationHistory();

        reset(backListener);
        navigationController.navigateBack(this);
        ArgumentCaptor<NavigationController.EventC2V.OnLocationBack> event
                = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationBack.class);
        verify(backListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId4);
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId3);
        NavLocation currentLoc = navigationController.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId3);
        assertEquals(currentLoc.getPreviousLocation().getLocationId(), locId2);
        assertEquals(currentLoc.getPreviousLocation().getPreviousLocation().getLocationId(), locId1);
        assertEquals(currentLoc.getPreviousLocation().getPreviousLocation().getPreviousLocation(), null);
        Assert.assertFalse(event.getValue().isFastRewind());

        reset(backListener);
        navigationController.navigateBack(this);
        event = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationBack.class);
        verify(backListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId3);
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId2);
        currentLoc = navigationController.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId2);
        assertEquals(currentLoc.getPreviousLocation().getLocationId(), locId1);
        assertEquals(currentLoc.getPreviousLocation().getPreviousLocation(), null);
        Assert.assertFalse(event.getValue().isFastRewind());

        reset(backListener);
        navigationController.navigateBack(this);
        event = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationBack.class);
        verify(backListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId2);
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId1);
        currentLoc = navigationController.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId1);
        assertEquals(currentLoc.getPreviousLocation(), null);
        Assert.assertFalse(event.getValue().isFastRewind());

        reset(backListener);
        navigationController.navigateBack(this);
        event = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationBack.class);
        verify(backListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId1);
        assertEquals(event.getValue().getCurrentValue(), null);
        currentLoc = navigationController.getModel().getCurrentLocation();
        assertEquals(currentLoc, null);
        Assert.assertFalse(event.getValue().isFastRewind());

        //has already reached the start of the navigation, should not be able to navigate back any more
        reset(backListener);
        navigationController.navigateBack(this);
        event = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationBack.class);
        verify(backListener, times(0)).onEvent(event.capture());
    }

    @Test
    public void shouldBeAbleToNavigateBackToGivenLocation() throws Exception {
        //mock the subscriber
        BackListener backListener = mock(BackListener.class);
        eventBusV.register(backListener);

        prepareLocationHistory();

        reset(backListener);
        navigationController.navigateBack(this, locId2);
        ArgumentCaptor<NavigationController.EventC2V.OnLocationBack> event
                = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationBack.class);
        verify(backListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId4);
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId2);
        NavLocation currentLoc = navigationController.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId2);
        assertEquals(currentLoc.getPreviousLocation().getLocationId(), locId1);
        assertEquals(currentLoc.getPreviousLocation().getPreviousLocation(), null);

        Assert.assertTrue(event.getValue().isFastRewind());
    }

    @Test
    public void shouldBeAbleToNavigateBackToFirstLocation() throws Exception {
        //mock the subscriber
        BackListener backListener = mock(BackListener.class);
        eventBusV.register(backListener);

        prepareLocationHistory();

        reset(backListener);
        navigationController.navigateBack(this, null);
        ArgumentCaptor<NavigationController.EventC2V.OnLocationBack> event
                = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationBack.class);
        verify(backListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId4);
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId1);
        NavLocation currentLoc = navigationController.getModel().getCurrentLocation();
        assertEquals(currentLoc.getLocationId(), locId1);
        assertEquals(currentLoc.getPreviousLocation(), null);

        Assert.assertTrue(event.getValue().isFastRewind());
    }

    @Test
    public void should_post_app_exit_event_on_the_last_back_of_linear_back_navigation() {
        //mock the subscriber
        class AppExitListener {
            public void onEvent(NavigationController.EventC2C.OnAppExit event) {}
        }

        ArgumentCaptor<NavigationController.EventC2C.OnAppExit> event
                = ArgumentCaptor.forClass(NavigationController.EventC2C.OnAppExit.class);

        AppExitListener exitListener = mock(AppExitListener.class);
        eventBusC.register(exitListener);

        prepareLocationHistory();

        reset(exitListener);
        navigationController.navigateBack(this);
        verify(exitListener, times(0)).onEvent(event.capture());

        navigationController.navigateBack(this);
        verify(exitListener, times(0)).onEvent(event.capture());

        navigationController.navigateBack(this);
        verify(exitListener, times(0)).onEvent(event.capture());

        navigationController.navigateBack(this);
        verify(exitListener, times(1)).onEvent(event.capture());
    }

    @Test
    public void should_post_app_exit_event_on_the_last_back_of_fast_back_navigation() {
        //mock the subscriber
        class AppExitListener {
            public void onEvent(NavigationController.EventC2C.OnAppExit event) {}
        }

        ArgumentCaptor<NavigationController.EventC2C.OnAppExit> event
                = ArgumentCaptor.forClass(NavigationController.EventC2C.OnAppExit.class);

        AppExitListener exitListener = mock(AppExitListener.class);
        eventBusC.register(exitListener);

        prepareLocationHistory();

        reset(exitListener);
        navigationController.navigateBack(this, null);
        verify(exitListener, times(0)).onEvent(event.capture());

        navigationController.navigateBack(this);
        verify(exitListener, times(1)).onEvent(event.capture());
    }

    @Test
         public void should_not_raise_navigate_back_event_when_navigate_to_first_location_from_the_first_location() throws Exception {
        // Arrange
        BackListener backListener = mock(BackListener.class);
        eventBusV.register(backListener);

        navigationController.navigateTo(this, locId1);

        // Arrange
        navigationController.navigateBack(this, null);

        // Verify
        ArgumentCaptor<NavigationController.EventC2V.OnLocationBack> event
                = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationBack.class);
        verify(backListener, times(0)).onEvent(event.capture());
    }

    @Test
    public void should_not_raise_navigate_back_event_when_navigate_to_unknown_location() throws Exception {
        // Arrange
        BackListener backListener = mock(BackListener.class);
        eventBusV.register(backListener);

        prepareLocationHistory();

        // Arrange
        navigationController.navigateBack(this, "Bad Location");

        // Verify
        ArgumentCaptor<NavigationController.EventC2V.OnLocationBack> event
                = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationBack.class);
        verify(backListener, times(0)).onEvent(event.capture());
    }

    @Test
    public void should_not_raise_navigate_back_event_when_fast_back_navigate_from_null_location() throws Exception {
        // Arrange
        BackListener backListener = mock(BackListener.class);
        eventBusV.register(backListener);

        // Arrange
        navigationController.navigateBack(this, "any location");

        // Verify
        ArgumentCaptor<NavigationController.EventC2V.OnLocationBack> event
                = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationBack.class);
        verify(backListener, times(0)).onEvent(event.capture());
    }

    @Test
    public void should_be_able_to_log_navigation_history() throws Exception {
        // Arrange
        Logger logger = mock(Logger.class);
        navigationController.dumpHistoryOnLocationChange = true;
        navigationController.logger = logger;

        // Act
        navigationController.navigateTo(this, "any location", "back to location");

        // Verify
        verify(logger, atLeast(1)).trace(anyString());

        // Arrange
        reset(logger);

        // Act
        navigationController.navigateTo(this, "any location");

        // Verify
        verify(logger, atLeast(1)).trace(anyString());

        // Arrange
        reset(logger);

        // Act
        navigationController.navigateBack(this);

        // Verify
        verify(logger, atLeast(1)).trace(anyString());

        // Arrange
        reset(logger);
        navigationController.navigateTo(this, "some location");

        // Act
        navigationController.navigateBack(this, null);

        // Verify
        verify(logger, atLeast(1)).trace(anyString());
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
    @interface Slower2 {}

    @Qualifier
    @Retention(RUNTIME)
    @interface Slower3 {}

    @Slower2
    @Slower3
    static class SlowXHolder {

    }

    @Test(expected = MvcGraphException.class)
    public void should_catch_invocation_exception_when_NPE_detected_on_injection() throws Exception {
        Component com = new Component() {
            @Provides
            @Singleton
            @Slower2
            TimerController timerSlowerX2() {
                return new TimerControllerImpl(){
                    {
                        onConstruct();
                    }
                    @Override
                    public void setInitialValue(long value) {
                        super.setInitialValue(value * 2);
                    }
                };
            }
        };
        Injector.getGraph().register(com);

        Annotation slower2Qualifier = SlowXHolder.class.getAnnotation(Slower2.class);

        Injector.getGraph().use(TimerController.class, slower2Qualifier, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should have now been released
                Assert.assertEquals(0, instance.getInitialValue());
            }
        });
    }

    @Test
    public void should_retain_prepared_instance_until_navigation_settled() throws Exception {
        // Arrange
        final long fiveMinutes = 60 * 5;

        Component com = new Component() {
            @Provides
            @Singleton
            @Slower2
            TimerController timerSlowerX2() {
                return new TimerControllerImpl(){
                    {
                        try {
                            onConstruct();
                        } catch (Exception e) {}

                    }
                    @Override
                    public void setInitialValue(long value) {
                        super.setInitialValue(value * 2);
                    }
                };
            }

            @Provides
            @Singleton
            @Slower3
            TimerController timerSlowerX3() {
                return new TimerControllerImpl(){
                    {
                        try {
                            onConstruct();
                        } catch (Exception e) {}
                    }
                    @Override
                    public void setInitialValue(long value) {
                        super.setInitialValue(value * 3);
                    }
                };
            }
        };

        Injector.getGraph().register(com);

        Annotation slower2Qualifier = SlowXHolder.class.getAnnotation(Slower2.class);
        Annotation slower3Qualifier = SlowXHolder.class.getAnnotation(Slower3.class);

        Injector.getGraph().use(TimerController.class, slower2Qualifier, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should have now been released
                Assert.assertEquals(0, instance.getInitialValue());
            }
        });

        // Act
        Navigator navigator = navigationController.navigate(this).with(TimerController.class, slower2Qualifier, new Preparer<TimerController>() {
            @Override
            public void prepare(TimerController instance) {
                instance.setInitialValue(fiveMinutes);
            }
        });
        navigator.to(TimerFragmentX2.class.getName());

        Injector.getGraph().use(TimerController.class, slower2Qualifier, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should not have been released yet
                Assert.assertEquals(fiveMinutes * 2, instance.getInitialValue());
            }
        });

        //destroy the navigator
        navigator.__destroy();

        Injector.getGraph().use(TimerController.class, slower2Qualifier, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should have now been released
                Assert.assertEquals(0, instance.getInitialValue());
            }
        });

        //Test fragment 3
        Injector.getGraph().use(TimerController.class, slower3Qualifier, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should have now been released
                Assert.assertEquals(0, instance.getInitialValue());
            }
        });

        navigator = navigationController.navigate(this).with(TimerController.class, slower3Qualifier, new Preparer<TimerController>() {
            @Override
            public void prepare(TimerController instance) {
                instance.setInitialValue(fiveMinutes);
            }
        });
        navigator.to(TimerFragmentX3.class.getName());

        Injector.getGraph().use(TimerController.class, slower3Qualifier, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should not have been released yet
                Assert.assertEquals(fiveMinutes * 3, instance.getInitialValue());
            }
        });

        //destroy the navigator
        navigator.__destroy();

        Injector.getGraph().use(TimerController.class, slower3Qualifier, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should have now been released
                Assert.assertEquals(0, instance.getInitialValue());
            }
        });
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
        Navigator navigator = navigationController.navigate(this).with(TimerController.class, new Preparer<TimerController>() {
            @Override
            public void prepare(TimerController instance) {
                instance.setInitialValue(fiveMinutes);
            }
        });
        navigator.to(TimerFragment.class.getName());

        Injector.getGraph().use(TimerController.class, null, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should not have been released yet
                Assert.assertEquals(fiveMinutes, instance.getInitialValue());
            }
        });

        //destroy the navigator
        navigator.__destroy();

        Injector.getGraph().use(TimerController.class, new Consumer<TimerController>() {
            @Override
            public void consume(TimerController instance) {
                //Controller should have now been released
                Assert.assertEquals(0, instance.getInitialValue());
            }
        });
    }

    @Test
    public void should_invoke_singular_argument_with_method_of_navigator_correct() throws Exception {
        Navigator navigator = navigationController.navigate(this);
        Navigator spiedNavigator = spy(navigator);

        verify(spiedNavigator, times(0)).with(eq(NavigationController.class), isNull(Annotation.class), isNull(Preparer.class));

        spiedNavigator.with(NavigationController.class);

        verify(spiedNavigator).with(eq(NavigationController.class), isNull(Annotation.class), isNull(Preparer.class));
    }

    @Test
    public void should_return_correct_sender_by_navigator() throws Exception {
        // Act
        Navigator navigator = navigationController.navigate(this);

        Assert.assertTrue(this == navigator.getSender());
    }

    @Test
    public void should_invoke_on_settled_when_navigation_is_done() throws Exception {
        // Arrange
        Navigator.OnSettled onSettled = mock(Navigator.OnSettled.class);

        // Act
        Navigator navigator = navigationController.navigate(this).with(TimerController.class)
                .onSettled(onSettled);
        navigator.to(TimerFragment.class.getName());

        verify(onSettled, times(0)).run();

        //destroy the navigator
        navigator.__destroy();

        verify(onSettled, times(1)).run();
    }

    /**
     * Prepare 4 locations in the history
     */
    private ForwardListener prepareLocationHistory() {
        //mock the subscriber
        ForwardListener forwardListener = mock(ForwardListener.class);
        eventBusV.register(forwardListener);

        navigationController.navigateTo(this, locId1);

        ArgumentCaptor<NavigationController.EventC2V.OnLocationForward> event =
                ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationForward.class);
        verify(forwardListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue(), null);
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId1);

        assertEquals(event.getValue().getLocationWhereHistoryClearedUpTo(), null);
        assertEquals(event.getValue().isClearHistory(), false);

        reset(forwardListener);
        navigationController.navigateTo(this, locId2);

        event = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationForward.class);
        verify(forwardListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId1);
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId2);

        assertEquals(event.getValue().getLocationWhereHistoryClearedUpTo(), null);
        assertEquals(event.getValue().isClearHistory(), false);

        reset(forwardListener);
        navigationController.navigateTo(this, locId3);

        event = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationForward.class);
        verify(forwardListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId2);
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId3);

        assertEquals(event.getValue().getLocationWhereHistoryClearedUpTo(), null);
        assertEquals(event.getValue().isClearHistory(), false);

        reset(forwardListener);
        navigationController.navigateTo(this, locId4);

        event = ArgumentCaptor.forClass(NavigationController.EventC2V.OnLocationForward.class);
        verify(forwardListener).onEvent(event.capture());
        assertEquals(event.getValue().getLastValue().getLocationId(), locId3);
        assertEquals(event.getValue().getCurrentValue().getLocationId(), locId4);

        assertEquals(event.getValue().getLocationWhereHistoryClearedUpTo(), null);
        assertEquals(event.getValue().isClearHistory(), false);

        return forwardListener;
    }

}
