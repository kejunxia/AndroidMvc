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
import com.shipdream.lib.android.mvc.event.ValueChangeEventC2V;

/**
 * Controller to navigate among different fragments in the SAME activity.
 */
public interface NavigationController extends BaseController<NavigationController.Model> {
    /**
     * Navigate to a new location. Current location will be saved/stacked into history which can be
     * popped out by {@link #navigateBack(Object, String)} or {@link #navigateTo(Object, String, String)}.
     * Navigation only takes effect when the given locationId is different from the current location
     * and raises {@link EventC2V.OnLocationForward}
     *
     * @param sender     Who wants to navigate
     * @param locationId The id of the location navigate to
     */
    void navigateTo(Object sender, String locationId);

    /**
     * Navigates to a new location and exclusively clears history prior to the given
     * clearTopToLocationId (clearTopToLocationId will be second last location below given location).
     * When clearTopToLocationId is null, it clears all history. In other words, the current given
     * location will be the only location in the history stack and all other previous locations
     * will be cleared. Navigation only takes effect when the given locationId is different from the
     * current location and raises {@link EventC2V.OnLocationForward}
     *
     * @param sender               Who wants to navigate
     * @param locationId           The id of the location navigate to
     * @param clearTopToLocationId Null if all history locations want to be cleared otherwise, the
     *                             id of the location the history will be exclusively cleared up to
     *                             which will be the second last location after navigation.
     */
    void navigateTo(Object sender, String locationId, String clearTopToLocationId);

    /**
     * Navigates back. If current location is null it doesn't take any effect otherwise
     * raises a {@link EventC2V.OnLocationBack} event when there is a previous location.
     *
     * @param sender Who wants to navigate back
     */
    void navigateBack(Object sender);

    /**
     * Navigates back. If current location is null it doesn't take any effect. When toLocationId
     * is null, navigate to the very first location and clear all history prior to it, otherwise
     * navigate to location with given locationId and clear history prior to it. Then a
     * {@link EventC2V.OnLocationBack} event will be raised.
     *
     * @param sender       Who wants to navigate
     * @param toLocationId Null when needs to navigate to the very first location and all history
     *                     locations will be above it will be cleared. Otherwise, the id of the
     *                     location where the history will be exclusively cleared up to. Then this
     *                     location will be the second last one.
     */
    void navigateBack(Object sender, String toLocationId);

    interface EventC2V {
        /**
         * Event to notify views navigation will move forward.
         */
        class OnLocationForward extends ValueChangeEventC2V<NavLocation> {
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
                                     boolean clearHistory, NavLocation locationWhereHistoryClearedUpTo) {
                super(sender, lastValue, currentValue);
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
        class OnLocationBack extends ValueChangeEventC2V<NavLocation> {
            private boolean fastRewind;

            public OnLocationBack(Object sender, NavLocation lastValue, NavLocation currentValue,
                                  boolean fastRewind) {
                super(sender, lastValue, currentValue);
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
