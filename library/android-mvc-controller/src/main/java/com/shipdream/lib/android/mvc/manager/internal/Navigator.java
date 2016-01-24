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

package com.shipdream.lib.android.mvc.manager.internal;

import com.shipdream.lib.android.mvc.Injector;
import com.shipdream.lib.android.mvc.MvcGraphException;
import com.shipdream.lib.android.mvc.NavLocation;
import com.shipdream.lib.android.mvc.event.BaseEventC;
import com.shipdream.lib.android.mvc.manager.NavigationManager;
import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * A navigator consists of data for a navigation.It is created by {@link NavigationManager#navigate(Object)}
 */
public class Navigator {
    /**
     * The callback when the navigation is settled. Since Android Fragment doesn't invoke its call
     * back like onCreate, onCreateView and etc after a fragment manager commits fragment transaction,
     * if something needs to be done after the fragment being navigated to is ready to show
     * (MvcFragment.onViewReady is called), put the actions in here.
     */
    public interface OnSettled {
        void run();
    }

    private static class PendingReleaseInstance<T> {
        private Class<T> type;
        private Annotation qualifier;
        private T instance;
    }

    private final Object sender;
    private OnSettled onSettled;
    private NavigationManagerImpl navigationManager;
    private BaseEventC navigateEvent;
    private List<PendingReleaseInstance> pendingReleaseInstances;

    /**
     * Construct a {@link Navigator}
     * @param sender Who wants to navigate
     * @param navigationManager The navigation manager
     */
    Navigator(Object sender, NavigationManagerImpl navigationManager) {
        this.sender = sender;
        this.navigationManager = navigationManager;
    }

    /**
     * Who wants to navigate
     * @return the sender
     */
    public Object getSender() {
        return sender;
    }

    /**
     * Prepare the instance subject to being injected with no qualifier for the fragment being
     * navigated to. This instance will be not be released until the navigation is settled. To
     * config the instance try {@link #with(Class, Preparer)} or {@link #with(Class, Annotation, Preparer)}
     *
     * @param type The class type of the instance needs to be prepared
     * @return This navigator
     * @throws MvcGraphException Raised when the required injectable object cannot be injected
     */
    public <T> Navigator with(Class<T> type) throws MvcGraphException {
        with(type, null, null);
        return this;
    }

    /**
     * Prepare the instance subject to being injected with no qualifier for the fragment being
     * navigated to. It's an equivalent way to pass arguments to the next fragment.For example, when
     * next fragment needs to have a pre set page title name, the controller referenced by the
     * fragment can be prepared here and set the title in the controller's model. Then in the
     * MvcFragment.onViewReady bind the value of the page title from the controller's model to the
     * fragment.
     *
     * <p>Example:</p>
     * To initialize the timer of a TimerFragment which counts down seconds,sets the initial value
     * of its controller by this with method.
     * <pre>
     class TimerFragment {
        @Inject
        TimerController timerController;
     }

     interface TimerController {
        void setInitialValue(long howManySeconds);
     }

     navigationManager.navigate(this).with(TimerController.class, new Preparer<TimerController>() {
        @Override
        public void prepare(TimerController instance) {
            long fiveMinutes = 60 * 5;
            instance.setInitialValue(fiveMinutes);

            //Then the value set to the controller will be guaranteed to be retained when
            //TimerFragment is ready to show
        }
     }).to(TimerFragment.class.getName());
     * </pre>
     * @param type The class type of the instance needs to be prepared
     * @param preparer The preparer in which the injected instance will be prepared
     * @return This navigator
     * @throws MvcGraphException Raised when the required injectable object cannot be injected
     */
    public <T> Navigator with(Class<T> type, Preparer<T> preparer) throws MvcGraphException {
        with(type, null, preparer);
        return this;
    }

    /**
     * Prepare the instance subject to being injected for the fragment being navigated to. It's an
     * equivalent way to pass arguments to the next fragment.For example, when next fragment needs
     * to have a pre set page title name, the controller referenced by the fragment can be prepared
     * here and set the title in the controller's model. Then in the MvcFragment.onViewReady bind
     * the value of the page title from the controller's model to the fragment.
     *
     * <p>Example:</p>
     * To initialize the timer of a TimerFragment which counts down seconds,sets the initial value
     * of its controller by this with method.
     * <pre>
     class TimerFragment {
        @Inject
        TimerController timerController;
     }

     interface TimerController {
        void setInitialValue(long howManySeconds);
     }

     navigationManager.navigate(this).with(TimerController.class, null, new Preparer<TimerController>() {
        @Override
        public void prepare(TimerController instance) {
            long fiveMinutes = 60 * 5;
            instance.setInitialValue(fiveMinutes);

            //Then the value set to the controller will be guaranteed to be retained when
            //TimerFragment is ready to show
        }
     }).to(TimerFragment.class.getName());
     * </pre>
     * @param type The class type of the instance needs to be prepared
     * @param qualifier The qualifier
     * @param preparer The preparer in which the injected instance will be prepared
     * @return This navigator
     * @throws MvcGraphException Raised when the required injectable object cannot be injected
     */
    public <T> Navigator with(Class<T> type, Annotation qualifier, Preparer<T> preparer) throws MvcGraphException {
        try {
            T instance = Injector.getGraph().reference(type, qualifier);

            if (preparer != null) {
                preparer.prepare(instance);
            }

            if (pendingReleaseInstances == null) {
                pendingReleaseInstances = new ArrayList<>();
            }
            PendingReleaseInstance pendingReleaseInstance = new PendingReleaseInstance();
            pendingReleaseInstance.instance = instance;
            pendingReleaseInstance.type = type;
            pendingReleaseInstance.qualifier = qualifier;
            pendingReleaseInstances.add(pendingReleaseInstance);
        } catch (PokeException e) {
            throw new MvcGraphException(e.getMessage(), e);
        }
        return this;
    }

    /**
     * Navigates to the specified location. Navigation only takes effect when the given locationId
     * is different from the current location and raises {@link NavigationManager.Event2C.OnLocationForward}
     *
     * <p>
     * To set argument for the next fragment navigating to, use {@link #with(Class, Annotation, Preparer)}
     * </p>
     *
     * <p>
     * Navigation will automatically manage continuity of state before and after the
     * navigation is performed. The injected instance will not be released until the next fragment
     * is settled. So when the current fragment and next fragment share same injected
     * controller their instance will be same.
     * </p>
     *
     * @param locationId           The id of the location navigate to
     */
    public void to(String locationId) {
        doNavigateTo(locationId, false, null);
        go();
    }

    /**
     * Navigates to a new location and exclusively clears history prior to the given
     * clearTopToLocationId (clearTopToLocationId will be last location below given location).
     * When clearTopToLocationId is null, it clears all history. In other words, the current given
     * location will be the only location in the history stack and all other previous locations
     * will be cleared. Navigation only takes effect when the given locationId is different from the
     * current location and raises {@link NavigationManager.Event2C.OnLocationForward}
     *
     * <p>
     * To set argument for the next fragment navigating to, use {@link #with(Class, Annotation, Preparer)}
     * </p>
     *
     * <p>
     * Navigation will automatically manage continuity of state before and after the
     * navigation is performed. The injected instance will not be released until the next fragment
     * is settled. So when the current fragment and next fragment share same injected
     * controller their instance will be same.
     * </p>
     *
     * @param locationId           The id of the location navigate to
     * @param clearTopToLocationId Null if all history locations want to be cleared otherwise, the
     *                             id of the location the history will be exclusively cleared up to
     *                             which will be the second last location after navigation.
     */
    public void to(String locationId, String clearTopToLocationId) {
        doNavigateTo(locationId, true, clearTopToLocationId);
        go();
    }

    private void doNavigateTo(String locationId, boolean clearTop,
                              String clearTopToLocationId) {
        NavLocation clearedTopToLocation = null;
        if (clearTop) {
            if (clearTopToLocationId != null) {
                //find out the top most location in the history stack with clearTopToLocationId
                NavLocation currentLoc = navigationManager.getModel().getCurrentLocation();
                while (currentLoc != null) {
                    if (clearTopToLocationId.equals(currentLoc.getLocationId())) {
                        //Reverse the history to this location
                        clearedTopToLocation = currentLoc;
                        break;
                    }
                    currentLoc = currentLoc.getPreviousLocation();
                }
                if (clearedTopToLocation == null) {
                    //The location to clear up to is not found. Disable clear top.
                    clearTop = false;
                }
            } else {
                clearedTopToLocation = null;
            }
        }

        NavLocation lastLoc = navigationManager.getModel().getCurrentLocation();
        boolean locationChanged = false;

        if (clearTop) {
            locationChanged = true;
        } else {
            if (locationId != null) {
                if(lastLoc == null) {
                    locationChanged = true;
                } else if(!locationId.equals(lastLoc.getLocationId())) {
                    locationChanged = true;
                }
            }
        }

        if (locationChanged) {
            NavLocation currentLoc = new NavLocation();
            currentLoc._setLocationId(locationId);
            if (!clearTop) {
                //Remember last location as previous location
                currentLoc._setPreviousLocation(lastLoc);
            } else {
                //Remember clear top location location as the previous location
                currentLoc._setPreviousLocation(clearedTopToLocation);
            }

            navigationManager.getModel().setCurrentLocation(currentLoc);

            navigateEvent = new NavigationManager.Event2C.OnLocationForward(sender, lastLoc,
                    currentLoc, clearTop, clearedTopToLocation, this);
        }
    }

    /**
     * Navigates one step back. If current location is null it doesn't take any effect otherwise
     * raises a {@link NavigationManager.Event2C.OnLocationBack} event when there is a previous
     * location.
     */
    public void back() {
        NavLocation currentLoc = navigationManager.getModel().getCurrentLocation();
        if (currentLoc == null) {
            navigationManager.logger.warn("Current location should never be null before navigating backwards.");
            return;
        }

        NavLocation previousLoc = currentLoc.getPreviousLocation();
        navigationManager.getModel().setCurrentLocation(previousLoc);

        navigateEvent = new NavigationManager.Event2C.OnLocationBack(sender, currentLoc, previousLoc, false, this);
        go();
    }

    /**
     * Navigates back. If current location is null it doesn't take any effect. When toLocationId
     * is null, navigate to the very first location and clear all history prior to it, otherwise
     * navigate to location with given locationId and clear history prior to it. Then a
     * {@link NavigationManager.Event2C.OnLocationBack} event will be raised.
     *
     * @param toLocationId Null when needs to navigate to the very first location and all history
     *                     locations will be above it will be cleared. Otherwise, the id of the
     *                     location where the history will be exclusively cleared up to. Then this
     *                     location will be the second last one.
     */
    public void back(String toLocationId) {
        NavLocation currentLoc = navigationManager.getModel().getCurrentLocation();
        if (currentLoc == null) {
            navigationManager.logger.warn("Current location should never be null before navigating backwards.");
            return;
        }

        if (currentLoc.getPreviousLocation() == null) {
            //Has already been the first location, don't do anything
            return;
        }

        boolean success = false;
        NavLocation previousLoc = currentLoc;

        if(toLocationId == null) {
            success = true;
        }
        while (currentLoc != null) {
            if(toLocationId != null) {
                if (toLocationId.equals(currentLoc.getLocationId())) {
                    success = true;
                    break;
                }
            } else {
                if(currentLoc.getPreviousLocation() == null) {
                    break;
                }
            }
            currentLoc = currentLoc.getPreviousLocation();
        }
        if(success) {
            navigationManager.getModel().setCurrentLocation(currentLoc);
            navigateEvent = new NavigationManager.Event2C.OnLocationBack(sender, previousLoc, currentLoc, true, this);
        }
        go();
    }

    /**
     * Sets the call back when fragment being navigated to is ready to show(MvcFragment.onViewReady
     * is called).
     * @param onSettled {@link OnSettled} call back
     * @return The navigator itself
     */
    public Navigator onSettled(OnSettled onSettled) {
        this.onSettled = onSettled;
        return this;
    }

    /**
     * Sends out the navigation event to execute the navigation
     */
    private void go() {
        if (navigateEvent != null) {

            navigationManager.postEvent2C(navigateEvent);

            if (navigateEvent instanceof NavigationManager.Event2C.OnLocationForward) {
                NavigationManager.Event2C.OnLocationForward event = (NavigationManager.Event2C.OnLocationForward) navigateEvent;
                String lastLocId = event.getLastValue() == null ? null
                        : event.getLastValue().getLocationId();
                navigationManager.logger.trace("Nav Manager: Forward: {} -> {}", lastLocId,
                        event.getCurrentValue().getLocationId());
            }

            if (navigateEvent instanceof NavigationManager.Event2C.OnLocationBack) {
                NavigationManager.Event2C.OnLocationBack event = (NavigationManager.Event2C.OnLocationBack) navigateEvent;
                NavLocation lastLoc = event.getLastValue();
                NavLocation currentLoc = event.getCurrentValue();
                navigationManager.logger.trace("Nav Manager: Backward: {} -> {}",
                        lastLoc.getLocationId(),
                        currentLoc == null ? "null" : currentLoc.getLocationId());

                checkAppExit(sender);
            }
        }
        dumpHistory();
    }

    /**
     * Internal use. Don't do it in your app.
     */
    void __destroy() {
        if (onSettled != null) {
            onSettled.run();
        }

        if (pendingReleaseInstances != null) {
            for (PendingReleaseInstance i : pendingReleaseInstances) {
                try {
                    Injector.getGraph().dereference(i.instance, i.type, i.qualifier);
                } catch (ProviderMissingException e) {
                    //should not happen
                    //in case this happens just logs it
                    navigationManager.logger.warn("Failed to auto release {} after navigation settled", i.type.getName());
                }
            }
        }
    }

    /**
     * Check the app is exiting
     * @param sender The sender
     */
    private void checkAppExit(Object sender) {
        NavLocation curLocation = navigationManager.getModel().getCurrentLocation();
        if (curLocation == null) {
            navigationManager.postEvent2C(new NavigationManager.Event2C.OnAppExit(sender));
        }
    }

    /**
     * Prints navigation history
     */
    private void dumpHistory() {
        if (navigationManager.dumpHistoryOnLocationChange) {
            navigationManager.logger.trace("");
            navigationManager.logger.trace("Nav Controller: dump: begin ---------------------------------------------->");
            NavLocation curLoc = navigationManager.getModel().getCurrentLocation();
            while (curLoc != null) {
                navigationManager.logger.trace("Nav Controller: dump: {}({})", curLoc.getLocationId());
                curLoc = curLoc.getPreviousLocation();
            }
            navigationManager.logger.trace("Nav Controller: dump: end   ---------------------------------------------->");
            navigationManager.logger.trace("");
        }
    }
}
