package com.shipdream.lib.android.mvc.manager;

import com.shipdream.lib.android.mvc.Constructable;
import com.shipdream.lib.android.mvc.Disposable;
import com.shipdream.lib.android.mvc.StateKeeper;
import com.shipdream.lib.android.mvc.StateManaged;
import com.shipdream.lib.poke.util.ReflectUtils;

/**
 * Abstract manager with state that needs to be managed. A stateful manager can be shared by multiple
 * controllers. For example, LoginManager is an good example that will manage the state of logged in
 * user. The log in user object usually is a part of the state to remember the logged in user.
 *
 * <p>
 * Managers should only be serving controllers and not visible to views.
 * </p>
 */
public abstract class AbstractStatefulManager<STATE> implements StateManaged<STATE>,
        Constructable, Disposable {
    private STATE state;

    /**
     * Bind state to this manager.
     * @param state non-null state
     * @throws IllegalArgumentException thrown when null is being bound
     */
    public void bindState(STATE state) {
        if (state == null) {
            throw new IllegalArgumentException("Can't bind a null state to a manage explicitly.");
        }
        this.state = state;
    }

    /**
     * Called when the manager is injected for the first time or restored when a new instance of
     * this manager needs to be instantiated.
     *
     * <p>The model of the manager will be instantiated by model's default no-argument constructor.
     * However, if the manager needs to be restored, a new instance of state restored by
     * {@link #restoreState(Object)} will replace the state created by this method.</p>
     */
    public void onConstruct() {
        state = createModelInstance();
    }

    private STATE createModelInstance() {
        Class<STATE> type = getStateType();
        if (type == null) {
            return null;
        } else {
            try {
                return new ReflectUtils.newObjectByType<>(type).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Fail to instantiate state by its default constructor");
            }
        }
    }

    /**
     * Called when the manager is disposed. This occurs when the manager is de-referenced and
     * not retained by any other objects.
     */
    @Override
    public void onDisposed() {
    }

    /**
     * @return Null if the manager doesn't need to get its state saved and restored automatically.
     */
    @Override
    final public STATE getState() {
        return state;
    }

    /**
     * @return The class type of the state of the controller that will be used by this manager to
     * instantiate its state in {@link #onConstruct()}
     */
    @Override
    abstract public Class<STATE> getStateType();

    /**
     * Restore the state of the manager.
     * <p>
     * Note that if the manager doesn't need its state saved and restored automatically and return
     * null in {@link #getStateType()}, then this method will have no effect.
     * </p>
     *
     * @param restoredState The restored state by {@link StateKeeper} that will be bound to the
     *                      manager when the views of app are restored.
     */
    @Override
    final public void restoreState(STATE restoredState) {
        if (getStateType() != null) {
            bindState(restoredState);
        }
        onRestored();
    }

    /**
     * Called when the manager is restored after {@link #restoreState(Object)} is called.
     */
    public void onRestored() {
    }

}
