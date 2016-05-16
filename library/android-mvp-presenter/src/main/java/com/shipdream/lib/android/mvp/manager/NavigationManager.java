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

package com.shipdream.lib.android.mvp.manager;

import com.shipdream.lib.android.mvp.NavLocation;
import com.shipdream.lib.android.mvp.event.BaseEventC;
import com.shipdream.lib.android.mvp.event.ValueChangeEventC;
import com.shipdream.lib.android.mvp.manager.internal.Navigator;

/**
 * Controller to navigate among different fragments in the SAME activity.
 */
public interface NavigationManager extends BaseManager<NavigationManager.Model> {
    /**
     * Model/State of navigation manager manage the navigation history
     */
    class Model {
        private NavLocation currentLocation;

        public NavLocation getCurrentLocation() {
            return currentLocation;
        }

        public void setCurrentLocation(NavLocation currentLocation) {
            this.currentLocation = currentLocation;
        }
    }

    /**
     * Initiates a {@link Navigator} to start navigation.
     * @param sender Who wants to navigate
     * @return A new instance of {@link Navigator}
     */
    Navigator navigate(Object sender);

    interface Event2C {
        /**
         * Event2C to notify views navigation will move forward.
         */
        class OnLocationForward extends ValueChangeEventC<NavLocation> {
            private final Navigator navigator;
            private final boolean clearHistory;
            private final NavLocation locationWhereHistoryClearedUpTo;

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
                super(sender, lastValue, currentValue);
                this.clearHistory = clearHistory;
                this.locationWhereHistoryClearedUpTo = locationWhereHistoryClearedUpTo;
                this.navigator = navigator;
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

            /**
             * Gets the navigator
             * @return {@link Navigator}
             */
            public Navigator getNavigator() {
                return navigator;
            }
        }

        /**
         * Event2C to notify views navigation will move backward.
         */
        class OnLocationBack extends ValueChangeEventC<NavLocation> {
            private final Navigator navigator;
            private final boolean fastRewind;

            public OnLocationBack(Object sender, NavLocation lastValue, NavLocation currentValue,
                                  boolean fastRewind, Navigator navigator) {
                super(sender, lastValue, currentValue);
                this.fastRewind = fastRewind;
                this.navigator = navigator;
            }

            /**
             * Indicates will this navigation jump more then 1 step
             */
            public boolean isFastRewind() {
                return fastRewind;
            }

            /**
             * Gets the navigator
             * @return {@link Navigator}
             */
            public Navigator getNavigator() {
                return navigator;
            }
        }

        /**
         * Event2C to notify the controllers the app exists, for example by back button. Be aware, this
         * doesn't mean the process of the application is killed but only all navigable fragments
         * and their containing activity are destroyed since there might be services still running.
         *
         * <p><b>This is a good point to notify controllers to clear the their state.</b></p>
         */
        class OnAppExit extends BaseEventC {
            public OnAppExit(Object sender) {
                super(sender);
            }
        }
    }

}
