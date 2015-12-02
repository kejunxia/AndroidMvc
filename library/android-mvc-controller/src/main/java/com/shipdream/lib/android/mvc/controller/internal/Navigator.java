package com.shipdream.lib.android.mvc.controller.internal;

import com.shipdream.lib.android.mvc.Constructable;
import com.shipdream.lib.android.mvc.Injector;
import com.shipdream.lib.android.mvc.MvcGraphException;
import com.shipdream.lib.android.mvc.NavLocation;
import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.poke.Consumer;
import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class Navigator {
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
    private NavigationControllerImpl navigationController;
    private NavigationController.EventC2V.OnLocationChanged navigateEvent;
    private List<PendingReleaseInstance> pendingReleaseInstances;

    Navigator(Object sender, NavigationControllerImpl navigationController) {
        this.sender = sender;
        this.navigationController = navigationController;
    }

    public Object getSender() {
        return sender;
    }

    public <T> Navigator prepare(Class<T> type, Consumer<T> consumer) throws MvcGraphException {
        prepare(type, null, consumer);
        return this;
    }

    public <T> Navigator prepare(Class<T> type, Annotation qualifier, Consumer<T> consumer) throws MvcGraphException {
        try {
            T instance = Injector.getGraph().reference(type, qualifier);

            consumer.consume(instance);

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

    public void to(String locationId) {
        doNavigateTo(locationId, false, null);
        go();
    }

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
                    currentLoc, clearTop, clearedTopToLocation, this);
        }
    }

    public void back() {
        NavLocation currentLoc = navigationController.getModel().getCurrentLocation();
        if (currentLoc == null) {
            navigationController.logger.warn("Current location should never be null before navigating backwards.");
            return;
        }

        NavLocation previousLoc = currentLoc.getPreviousLocation();
        navigationController.getModel().setCurrentLocation(previousLoc);

        navigateEvent = new NavigationController.EventC2V.OnLocationBack(sender, currentLoc, previousLoc, false, this);
        go();
    }

    public void back(String toLocationId) {
        NavLocation currentLoc = navigationController.getModel().getCurrentLocation();
        if (currentLoc == null) {
            navigationController.logger.warn("Current location should never be null before navigating backwards.");
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
            navigationController.getModel().setCurrentLocation(currentLoc);
            navigateEvent = new NavigationController.EventC2V.OnLocationBack(sender, previousLoc, currentLoc, true, this);
        }
        go();
    }

    public Navigator onSettled(OnSettled onSettled) {
        this.onSettled = onSettled;
        return this;
    }

    /**
     * Internal use. Don't do it in your app.
     */
    void destroy() {
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
                    navigationController.logger.warn("Failed to auto release {} after navigation settled", i.type.getName());
                }
            }
        }
    }

    private void go() {
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
