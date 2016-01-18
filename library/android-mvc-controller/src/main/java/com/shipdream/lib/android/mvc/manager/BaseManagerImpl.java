package com.shipdream.lib.android.mvc.manager;

import com.shipdream.lib.android.mvc.MvcBean;
import com.shipdream.lib.android.mvc.event.BaseEventC;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Abstract manager with state that needs to be managed. A stateful manager can be shared by multiple
 * controllers. For example, LoginManager is an good example that will manage the state of logged in
 * user. The log in user object usually is a part of the state to remember the logged in user.
 *
 * <p>
 * Managers should only be serving controllers and not visible to views. Managers can post events
 * to controllers to notify them the state is changed in the shared manager. For example, a common
 * scenario is multiple controllers can share an AccountManager/LoginManager and monitor the account
 * change events.
 * </p>
 */
public abstract class BaseManagerImpl<STATE> extends MvcBean<STATE> {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    @EventBusC
    private EventBus eventBus2C;

    /**
     * Help function to post events to controllers
     *
     * @param event post events to controllers
     */
    protected void postToControllers(final BaseEventC event) {
        if (eventBus2C != null) {
            eventBus2C.post(event);
        } else {
            logger.warn("Trying to post event {} to EventBusC which is null", event.getClass().getName());
        }
    }
}
