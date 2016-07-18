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

import com.shipdream.lib.android.mvc.event.ValueChangeEvent;

/**
 * The manager to navigate among different screen fragments in the SAME activity.
 */
public class NavigationManager extends Manager<NavigationManager.Model> {
    /**
     * Model/State of navigation manager manage the navigation history
     */
    public static class Model {
        private NavLocation currentLocation;

        public NavLocation getCurrentLocation() {
            return currentLocation;
        }

        public void setCurrentLocation(NavLocation currentLocation) {
            this.currentLocation = currentLocation;
        }
    }

    public interface Event {
        /**
         * Event2C to notify views navigation will move forward.
         */
        class OnLocationForward extends ValueChangeEvent<NavLocation> {
            private final Object sender;
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
                super(lastValue, currentValue);
                this.sender = sender;
                this.clearHistory = clearHistory;
                this.locationWhereHistoryClearedUpTo = locationWhereHistoryClearedUpTo;
                this.navigator = navigator;
            }

            /**
             * Who causes this event.
             * @return
             */
            public Object getSender() {
                return sender;
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
        class OnLocationBack extends ValueChangeEvent<NavLocation> {
            private final Object sender;
            private final Navigator navigator;
            private final boolean fastRewind;

            public OnLocationBack(Object sender, NavLocation lastValue, NavLocation currentValue,
                                  boolean fastRewind, Navigator navigator) {
                super(lastValue, currentValue);
                this.sender = sender;
                this.fastRewind = fastRewind;
                this.navigator = navigator;
            }

            /**
             * Who causes this event.
             * @return
             */
            public Object getSender() {
                return sender;
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
         * Fired when navigates away from the last item in the navigation history. Be aware, this
         * doesn't mean the process of the application is killed but only all navigable fragments
         * and their containing activity are destroyed since there might be services still running.
         *
         * <p><b>The references of fragment controllers should be all cleared. However the services
         * may still hold some controllers</b></p>
         */
        class OnAppExit {
            private final Object sender;
            public OnAppExit(Object sender) {
                this.sender = sender;
            }
            public Object getSender() {
                return sender;
            }
        }
    }

    boolean dumpHistoryOnLocationChange = false;

    @Override
    public Class<Model> modelType() {
        return NavigationManager.Model.class;
    }

    /**
     * Initiates a {@link Navigator} to start navigation.
     * @param sender Who wants to navigate
     * @return A new instance of {@link Navigator}
     */
    public Navigator navigate(Object sender) {
        return new Navigator(sender, this);
    }

}
