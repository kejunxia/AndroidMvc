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

import com.shipdream.lib.android.mvc.MvcGraph;
import com.shipdream.lib.android.mvc.NavLocation;
import com.shipdream.lib.android.mvc.controller.internal.Navigator;
import com.shipdream.lib.android.mvc.event.BaseEventC2C;
import com.shipdream.lib.android.mvc.event.ValueChangeEventC2V;
import com.shipdream.lib.poke.Consumer;

/**
 * Controller to navigate among different fragments in the SAME activity.
 */
public interface NavigationController extends BaseController<NavigationController.Model> {
    Navigator navigate(Object sender);

    Navigator navigate(Object sender, Class... preparedControllers);

    /**
     *
     * Navigate to a new location. Current location will be saved/stacked into history which can be
     * popped out by {@link #navigateBack(Object, String)} or {@link #navigateTo(Object, String, String)}.
     * Navigation only takes effect when the given locationId is different from the current location
     * and raises {@link EventC2V.OnLocationForward}
     *
     * <p>
     * Forward navigating will automatically manage continuity of state before and after the
     * navigation is performed. This is useful when the next navigation location needs be configured
     * with some initial state. <br>
     *
     * For example, when navigate to a fragment called TimerFragment which counts down from an
     * initial time value. We can create a TimerController[TimerModel] with the initial time
     * value and inject it into TimerFragment. Before we navigate to TimerFragment, we can set the
     * initial time value in TimerController[TimerModel] either by an injected field in current
     * object who is calling this method or use {@link MvcGraph#use(Class, Consumer)} to inject and
     * set the value on the fly. This value will be carried on to TimerFragment when it's created
     * and ready to show.
     * </p>
     *
     * <p><b>Deprecated: use {@link #navigate(Object)} or {@link #navigate(Object, Class[])} instead</b></p>
     *
     * @param sender     Who wants to navigate
     * @param locationId The id of the location navigate to
     *
     */
    @Deprecated
    void navigateTo(Object sender, String locationId);

    /**
     * Navigates to a new location and exclusively clears history prior to the given
     * clearTopToLocationId (clearTopToLocationId will be second last location below given location).
     * When clearTopToLocationId is null, it clears all history. In other words, the current given
     * location will be the only location in the history stack and all other previous locations
     * will be cleared. Navigation only takes effect when the given locationId is different from the
     * current location and raises {@link EventC2V.OnLocationForward}
     *
     * <p>
     * Forward navigating will automatically manage continuity of state before and after the
     * navigation is performed. This is useful when the next navigation location needs be configured
     * with some initial state. <br>
     *
     * For example, when navigate to a fragment called TimerFragment which counts down from an
     * initial time value. We can create a TimerController[TimerModel] with the initial time
     * value and inject it into TimerFragment. Before we navigate to TimerFragment, we can set the
     * initial time value in TimerController[TimerModel] either by an injected field in current
     * object who is calling this method or use {@link MvcGraph#use(Class, Consumer)} to inject and
     * set the value on the fly. This value will be carried on to TimerFragment when it's created
     * and ready to show.
     * </p>
     *
     * <p><b>Deprecated: use {@link #navigate(Object)} or {@link #navigate(Object, Class[])} instead</b></p>
     *
     * @param sender               Who wants to navigate
     * @param locationId           The id of the location navigate to
     * @param clearTopToLocationId Null if all history locations want to be cleared otherwise, the
     *                             id of the location the history will be exclusively cleared up to
     *                             which will be the second last location after navigation.
     */
    @Deprecated
    void navigateTo(Object sender, String locationId, String clearTopToLocationId);

    /**
     * Navigates back. If current location is null it doesn't take any effect otherwise
     * raises a {@link EventC2V.OnLocationBack} event when there is a previous location.
     *
     * <p><b>Deprecated: use {@link #navigate(Object)} or {@link #navigate(Object, Class[])} instead</b></p>
     *
     * @param sender Who wants to navigate back
     */
    @Deprecated
    void navigateBack(Object sender);

    /**
     * Navigates back. If current location is null it doesn't take any effect. When toLocationId
     * is null, navigate to the very first location and clear all history prior to it, otherwise
     * navigate to location with given locationId and clear history prior to it. Then a
     * {@link EventC2V.OnLocationBack} event will be raised.
     *
     * <p><b>Deprecated: use {@link #navigate(Object)} or {@link #navigate(Object, Class[])} instead</b></p>
     *
     * @param sender       Who wants to navigate
     * @param toLocationId Null when needs to navigate to the very first location and all history
     *                     locations will be above it will be cleared. Otherwise, the id of the
     *                     location where the history will be exclusively cleared up to. Then this
     *                     location will be the second last one.
     */
    @Deprecated
    void navigateBack(Object sender, String toLocationId);

    /**
     * Event t
     */
    interface EventC2V {
        abstract class OnLocationChanged extends ValueChangeEventC2V<NavLocation> {
            private final Navigator navigator;

            public OnLocationChanged(Object sender, NavLocation lastValue, NavLocation currentValue,
                                     Navigator navigator) {
                super(sender, lastValue, currentValue);
                this.navigator = navigator;
            }

            public Navigator getNavigator() {
                return navigator;
            }
        }

        /**
         * Event to notify views navigation will move forward.
         */
        class OnLocationForward extends OnLocationChanged {
            private boolean clearHistory;
            private NavLocation locationWhereHistoryClearedUpTo;

            /**
             * Construct event to notify views navigation will move forward.
             * @param sender Who wanted to navigate
             * @param lastValue The previous location before the navigation
             * @param currentValue The location navigating to
             * @param clearHistory Whether or not need to clear history locations
             * @param locationWhereHistoryClearedUpTo If need to clear location, up to where
             */
            public OnLocationForward(Object sender, NavLocation lastValue, NavLocation currentValue,
                                     boolean clearHistory, NavLocation locationWhereHistoryClearedUpTo,
                                     Navigator navigator) {
                super(sender, lastValue, currentValue, navigator);
                this.clearHistory = clearHistory;
                this.locationWhereHistoryClearedUpTo = locationWhereHistoryClearedUpTo;
            }

            /**
             * Indicates whether to clear some history locations
             */
            public boolean isClearHistory() {
                return clearHistory;
            }

            /**
             * The location where the history will be cleared up to. In other words, this location
             * will be the second last location underneath the top most one.
             *
             * @return The location where the history will be exclusively cleared up to
             */
            public NavLocation getLocationWhereHistoryClearedUpTo() {
                return locationWhereHistoryClearedUpTo;
            }
        }

        /**
         * Event to notify views navigation will move backward.
         */
        class OnLocationBack extends OnLocationChanged {
            private boolean fastRewind;

            public OnLocationBack(Object sender, NavLocation lastValue, NavLocation currentValue,
                                  boolean fastRewind, Navigator navigator) {
                super(sender, lastValue, currentValue, navigator);
                this.fastRewind = fastRewind;
            }

            /**
             * Indicates will this navigation jump more then 1 step
             */
            public boolean isFastRewind() {
                return fastRewind;
            }
        }

    }

    interface EventC2C {
        /**
         * Event to notify the controllers the app exists, for example by back button. Be aware, this
         * doesn't mean the process of the application is killed but only all navigable fragments
         * and their containing activity are destroyed since there might be services still running.
         *
         * <p><b>This is a good point to notify controllers to clear the their state.</b></p>
         */
        class OnAppExit extends BaseEventC2C {
            public OnAppExit(Object sender) {
                super(sender);
            }
        }
    }

    class Model {
        private NavLocation currentLocation;

        public NavLocation getCurrentLocation() {
            return currentLocation;
        }

        public void setCurrentLocation(NavLocation currentLocation) {
            this.currentLocation = currentLocation;
        }
    }

}
