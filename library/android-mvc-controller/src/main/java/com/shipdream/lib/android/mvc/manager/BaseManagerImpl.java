package com.shipdream.lib.android.mvc.manager;

import com.shipdream.lib.android.mvc.MvcBean;
import com.shipdream.lib.android.mvc.event.BaseEventC;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Abstract manager with model that needs to be managed. A manager can be shared by multiple
 * controllers. A LoginManager is an good example that manages the state of logged in user.
 *
 * <p>
 * Managers should only be serving controllers and not visible to views. Managers can post events
 * to controllers to notify the state change in the shared manager.
 * </p>
 */
public abstract class BaseManagerImpl<MODEL> extends MvcBean<MODEL> {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    @EventBusC
    private EventBus eventBus2C;

    /**
     * Post an event to other controllers. Event will be posted on the same thread as the caller.
     *
     * @param event event to controllers
     */
    protected void postControllerEvent(final BaseEventC event) {
        if (eventBus2C != null) {
            eventBus2C.post(event);
        } else {
            logger.warn("Trying to post event {} to EventBusC which is null", event.getClass().getName());
        }
    }
}
