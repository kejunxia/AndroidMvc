package com.shipdream.lib.android.mvc.manager;

import com.shipdream.lib.android.mvc.MvcBean;
import com.shipdream.lib.android.mvc.event.BaseEventC2C;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC2C;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Abstract manager with state that needs to be managed. A stateful manager can be shared by multiple
 * controllers. For example, LoginManager is an good example that will manage the state of logged in
 * user. The log in user object usually is a part of the state to remember the logged in user.
 *
 * <p>
 * Managers should only be serving controllers and not visible to views.
 * </p>
 */
public abstract class BaseManagerImpl<STATE> extends MvcBean<STATE> {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    @EventBusC2C
    EventBus eventBusC2C;

    /**
     * Called when the manager is constructed. Note that it could be called either when the
     * manager is instantiated for the first time or restored by views.
     *
     * <p>The model of the manager will be instantiated by model's default no-argument constructor.
     * However, if the manager needs to be restored, a new instance of model restored by
     * {@link #restoreState(Object)} will replace the model created here.</p>
     */
    public void onConstruct() {
        super.onConstruct();
        eventBusC2C.register(this);
    }

    /**
     * Called when the manager is disposed. This occurs when the manager is de-referenced and
     * not retained by any objects.
     */
    @Override
    public void onDisposed() {
        super.onDisposed();
        eventBusC2C.unregister(this);
    }

    /**
     * Help function to post events to controllers
     *
     * @param eventC2C Controller to Controller event to be broadcast
     */
    protected void postC2CEvent(final BaseEventC2C eventC2C) {
        if (eventBusC2C != null) {
            eventBusC2C.post(eventC2C);
        } else {
            logger.warn("Trying to post event {} to EventBusC2C which is null", eventC2C.getClass().getName());
        }
    }
}
