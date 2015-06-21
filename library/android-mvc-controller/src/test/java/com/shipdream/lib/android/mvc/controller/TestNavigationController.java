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

package com.shipdream.lib.android.mvc.controller;

import com.shipdream.lib.android.mvc.NavLocation;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
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
        eventBusC2V.register(backListener);

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
        eventBusC2V.register(backListener);

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
        eventBusC2V.register(backListener);

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

    /**
     * Prepare 4 locations in the history
     */
    private ForwardListener prepareLocationHistory() {
        //mock the subscriber
        ForwardListener forwardListener = mock(ForwardListener.class);
        eventBusC2V.register(forwardListener);

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
