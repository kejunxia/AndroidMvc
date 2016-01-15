package com.shipdream.lib.android.mvc.manager;

import com.shipdream.lib.android.mvc.Constructable;
import com.shipdream.lib.android.mvc.Disposable;
import com.shipdream.lib.android.mvc.StateKeeper;
import com.shipdream.lib.android.mvc.StateManaged;
import com.shipdream.lib.poke.util.ReflectUtils;

/**
 * Abstract manager with state that needs to be managed.
 */
public abstract class AbstractStatefulManager<STATE> implements StateManaged<STATE>,
        Constructable, Disposable {
    private STATE state;

    public void bindState(STATE state) {
        if (state == null) {
            throw new IllegalArgumentException("Can't bind a null state to a manage explicitly.");
        }
        this.state = state;
    }

    /**
     * Called when the controller is constructed. Note that it could be called either when the
     * controller is instantiated for the first time or restored by views.
     *
     * <p>The model of the controller will be instantiated by model's default no-argument constructor.
     * However, if the controller needs to be restored, a new instance of model restored by
     * {@link #restoreState(Object)} will replace the model created here.</p>
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
     * Called when the controller is disposed. This occurs when the controller is de-referenced and
     * not retained by any objects.
     */
    @Override
    public void onDisposed() {
    }

    /**
     * Method of {@link StateManaged} that allows {@link StateKeeper} to save and get the state of
     * which is also the model the controller.
     *
     * @return Null if the controller doesn't need to get its state saved and restored
     * automatically. e.g. The controller always loads resource from remote services so that
     * its state can be thought persisted by the remote services. Otherwise the model of the controller
     */
    @Override
    final public STATE getState() {
        return state;
    }

    /**
     * Method of {@link StateManaged} that allows {@link StateKeeper} to save and get the state of
     * which is also the model the controller.
     *
     * @return The class type of the model of the controller
     */
    @Override
    abstract public Class<STATE> getStateType();

    /**
     * Method of {@link StateManaged} that allows {@link StateKeeper} to save and get the state of
     * which is also the model the controller.
     * <p>
     * Note that if the controller doesn't need its state saved and restored automatically return
     * null in {@link #getStateType()} and then this method will have no effect.
     * </p>
     *
     * @param restoredState The restored state by {@link StateKeeper} that will be bound to the
     *                      controller on the view referencing the controller is restored.
     */
    @Override
    final public void restoreState(STATE restoredState) {
        if (getStateType() != null) {
            bindState(restoredState);
        }
        onRestored();
    }

    /**
     * Called when the controller is restored after {@link #restoreState(Object)} is called.
     */
    public void onRestored() {
    }

}
