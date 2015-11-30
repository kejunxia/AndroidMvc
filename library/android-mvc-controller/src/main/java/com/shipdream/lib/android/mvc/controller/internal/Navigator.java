package com.shipdream.lib.android.mvc.controller.internal;

import com.shipdream.lib.android.mvc.NavLocation;
import com.shipdream.lib.android.mvc.controller.NavigationController;

public class Navigator {
    public interface OnSettled {
        void run();
    }

    private final Object sender;
    private OnSettled onSettled;
    private NavigationControllerImpl navigationController;
    private NavigationController.EventC2V.OnLocationChanged navigateEvent;

    Navigator(Object sender, NavigationControllerImpl navigationController) {
        this.sender = sender;
        this.navigationController = navigationController;
    }

    public Object getSender() {
        return sender;
    }

    public OnSettled getOnSettled() {
        return onSettled;
    }

    public Navigator to(String locationId) {
        doNavigateTo(locationId, false, null);
        return this;
    }

    public Navigator to(String locationId, String clearTopToLocationId) {
        doNavigateTo(locationId, true, clearTopToLocationId);
        return this;
    }

    private void doNavigateTo(String locationId, boolean clearTop,
                              String clearTopToLocationId) {
        NavLocation clearedTopToLocation = null;
        if (clearTop) {
            if (clearTopToLocationId != null) {
                //find out the top most location in the history stack with clearTopToLocationId
                NavLocation currentLoc = navigationController.getModel().getCurrentLocation();
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

        NavLocation lastLoc = navigationController.getModel().getCurrentLocation();
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

            navigationController.getModel().setCurrentLocation(currentLoc);

            navigateEvent = new NavigationController.EventC2V.OnLocationForward(sender, lastLoc,
                    currentLoc, clearTop, clearedTopToLocation);
        }
    }

    public Navigator navigateBack() {
        NavLocation currentLoc = navigationController.getModel().getCurrentLocation();
        if (currentLoc == null) {
            navigationController.logger.warn("Current location should never be null before navigating backwards.");
            return this;
        }

        NavLocation previousLoc = currentLoc.getPreviousLocation();
        navigationController.getModel().setCurrentLocation(previousLoc);

        navigateEvent = new NavigationController.EventC2V.OnLocationBack(sender, currentLoc, previousLoc, false);
        return this;
    }

    public Navigator navigateBack(String toLocationId) {
        NavLocation currentLoc = navigationController.getModel().getCurrentLocation();
        if (currentLoc == null) {
            navigationController.logger.warn("Current location should never be null before navigating backwards.");
            return this;
        }

        if (currentLoc.getPreviousLocation() == null) {
            //Has already been the first location, don't do anything
            return this;
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
            navigationController.getModel().setCurrentLocation(currentLoc);
            navigateEvent = new NavigationController.EventC2V.OnLocationBack(sender, previousLoc, currentLoc, true);
        }
        return this;
    }

    public Navigator onSettled(OnSettled onSettled) {
        this.onSettled = onSettled;
        return this;
    }

    public void go() {
        if (navigateEvent != null) {
            navigationController.postC2VEvent(navigateEvent);

            if (navigateEvent instanceof NavigationController.EventC2V.OnLocationForward) {
                String lastLocId = navigateEvent.getLastValue() == null ? null
                        : navigateEvent.getLastValue().getLocationId();
                navigationController.logger.trace("Nav Controller: Forward: {} -> {}", lastLocId,
                        navigateEvent.getCurrentValue().getLocationId());
            }

            if (navigateEvent instanceof NavigationController.EventC2V.OnLocationBack) {
                NavLocation lastLoc = navigateEvent.getLastValue();
                NavLocation currentLoc = navigateEvent.getCurrentValue();
                navigationController.logger.trace("Nav Controller: Backward: {} -> {}",
                        lastLoc.getLocationId(),
                        currentLoc == null ? "null" : currentLoc.getLocationId());

                checkAppExit(sender);
            }

            dumpHistory();
        }

    }

    private void checkAppExit(Object sender) {
        NavLocation curLocation = navigationController.getModel().getCurrentLocation();
        if (curLocation == null) {
            navigationController.postC2CEvent(new NavigationController.EventC2C.OnAppExit(sender));
        }
    }

    private void dumpHistory() {
        if (navigationController.dumpHistoryOnLocationChange) {
            navigationController.logger.trace("");
            navigationController.logger.trace("Nav Controller: dump: begin ---------------------------------------------->");
            NavLocation curLoc = navigationController.getModel().getCurrentLocation();
            while (curLoc != null) {
                navigationController.logger.trace("Nav Controller: dump: {}({})", curLoc.getLocationId());
                curLoc = curLoc.getPreviousLocation();
            }
            navigationController.logger.trace("Nav Controller: dump: end   ---------------------------------------------->");
            navigationController.logger.trace("");
        }
    }
}
