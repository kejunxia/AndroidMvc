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

public class NavLocation {
    private String locationId;
    private boolean interim = false;
    private NavLocation previousLocation;

    /**
     * The location id which must be unique for the app.
     */
    public String getLocationId() {
        return locationId;
    }

    /**
     * Indicates whether the location is interim. Interim location will be skipped on back navigation.
     * @return Whether this location is interim.
     */
    public boolean isInterim() {
        return interim;
    }

    /**
     * Internal use. Don't set this from view layer.
     */
    public void _setLocationId(String locationId) {
        this.locationId = locationId;
    }

    /**
     * Internal use. Don't set this from view layer.
     */
    public void _setInterim(boolean interim) {
        this.interim = interim;
    }

    /**
     * Internal use. Don't set this from view layer.
     */
    public void _setPreviousLocation(NavLocation location) {
        this.previousLocation = location;
    }

    /**
     * Previous location
     */
    public NavLocation getPreviousLocation() {
        return previousLocation;
    }
}
