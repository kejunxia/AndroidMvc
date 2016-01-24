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

package com.shipdream.lib.android.mvc.controller;

import com.shipdream.lib.android.mvc.NavLocation;
import com.shipdream.lib.android.mvc.event.ValueChangeEventV;
import com.shipdream.lib.android.mvc.manager.internal.Navigator;

public interface MvcController extends BaseController{
    /**
     * Initiates a {@link Navigator} to start navigation.
     * @param sender Who wants to navigate
     * @return A new instance of {@link Navigator}
     */
    Navigator navigate(Object sender);

    /**
     * Get the current navigation location.
     * @return Current navigation location
     */
    NavLocation getCurrentLocation();

    /**
     * Events
     */
    interface EventC2V {
        abstract class OnLocationChanged extends ValueChangeEventV<NavLocation> {
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
         * Event2C to notify views navigation will move forward.
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
         * Event2C to notify views navigation will move backward.
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
}
