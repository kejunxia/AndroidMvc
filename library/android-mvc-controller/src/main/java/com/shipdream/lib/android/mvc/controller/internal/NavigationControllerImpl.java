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

import com.shipdream.lib.android.mvc.NavLocation;
import com.shipdream.lib.android.mvc.controller.NavigationController;

/**
 * Implementation of {@link NavigationController}
 */
public class NavigationControllerImpl extends BaseControllerImpl<NavigationController.Model>
        implements NavigationController {
    public static boolean dumpHistoryOnLocationChange = false;

    @Override
    public Class<Model> getModelClassType() {
        return NavigationController.Model.class;
    }

    @Override
    public void navigateTo(Object sender, String locationId) {
        doNavigateTo(sender, locationId, false, null);
    }

    @Override
    public void navigateTo(Object sender, String locationId, String clearTopToLocationId) {
        doNavigateTo(sender, locationId, true, clearTopToLocationId);
    }

    private void doNavigateTo(Object sender, String locationId, boolean clearTop,
                              String clearTopToLocationId) {
        NavLocation clearedTopToLocation = null;
        if (clearTop) {
            if (clearTopToLocationId != null) {
                //find out the top most location in the history stack with clearTopToLocationId
                NavLocation currentLoc = getModel().getCurrentLocation();
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

        NavLocation lastLoc = getModel().getCurrentLocation();
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

            getModel().setCurrentLocation(currentLoc);

            String lastLocId = lastLoc == null ? null : lastLoc.getLocationId();
            postC2VEvent(new EventC2V.OnLocationForward(sender, lastLoc, currentLoc, clearTop,
                    clearedTopToLocation));

            logger.debug("Nav Controller: Forward: {} -> {}", lastLocId, currentLoc.getLocationId());
        }

        dumpHistory();
    }

    @Override
    public void navigateBack(Object sender) {
        NavLocation currentLoc = getModel().getCurrentLocation();
        if (currentLoc == null) {
            logger.warn("Current location should never be null before navigating backwards.");
            return;
        }

        NavLocation previousLoc = currentLoc.getPreviousLocation();
        getModel().setCurrentLocation(previousLoc);
        postC2VEvent(new EventC2V.OnLocationBack(sender, currentLoc, previousLoc, false));

        logger.debug("Nav Controller: Backward: {} -> {}", currentLoc.getLocationId(),
                previousLoc == null ? "null" : previousLoc.getLocationId());

        dumpHistory();
    }

    @Override
    public void navigateBack(Object sender, String toLocationId) {
        NavLocation currentLoc = getModel().getCurrentLocation();
        if (currentLoc == null) {
            logger.warn("Current location should never be null before navigating backwards.");
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
            getModel().setCurrentLocation(currentLoc);
            postC2VEvent(new EventC2V.OnLocationBack(sender, previousLoc, currentLoc, true));
            logger.debug("Nav Controller: Backward: {} -> {}", currentLoc.getLocationId(),
                    previousLoc.getLocationId());

            dumpHistory();
        }
    }

    private void dumpHistory() {
        if (dumpHistoryOnLocationChange) {
            logger.trace("");
            logger.trace("Nav Controller: dump: begin ---------------------------------------------->");
            NavLocation curLoc = getModel().getCurrentLocation();
            while (curLoc != null) {
                logger.trace("Nav Controller: dump: {}({})", curLoc.getLocationId());
                curLoc = curLoc.getPreviousLocation();
            }
            logger.trace("Nav Controller: dump: end   ---------------------------------------------->");
            logger.trace("");
        }
    }

}
