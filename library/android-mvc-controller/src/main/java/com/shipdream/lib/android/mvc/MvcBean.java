package com.shipdream.lib.android.mvc;

import com.shipdream.lib.poke.util.ReflectUtils;

public abstract class MvcBean<STATE> implements StateManaged<STATE>, Constructable, Disposable {
    private STATE state;

    /**
     * Bind state to MvcBean
     * @param state non-null state
     * @throws IllegalArgumentException thrown when null is being bound
     */
    public void bindState(STATE state) {
        if (state == null) {
            throw new IllegalArgumentException("Can't bind null state explicitly.");
        }
        this.state = state;
    }

    /**
     * Called when the MvcBean is injected for the first time or restored when a new instance of
     * this MvcBean needs to be instantiated.
     *
     * <p>The state of the MvcBean will be instantiated by state's default no-argument constructor.
     * However, if the MvcBean needs to be restored, a new instance of state restored by
     * {@link #restoreState(Object)} will replace the state created by this method.</p>
     */
    public void onConstruct() {
        state = createStateInstance();
    }

    private STATE createStateInstance() {
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
     * Called when the MvcBean is disposed. This occurs when the MvcBean is de-referenced and
     * not retained by any other objects.
     */
    @Override
    public void onDisposed() {
    }

    /**
     * @return Null if the MvcBean doesn't need to get its state saved and restored automatically.
     */
    @Override
    final public STATE getState() {
        return state;
    }

    /**
     * @return The class type of the state of the controller that will be used by this MvcBean to
     * instantiate its state in {@link #onConstruct()}
     */
    @Override
    abstract public Class<STATE> getStateType();

    /**
     * Restore the state of this MvcBean.
     * <p>
     * Note that if the MvcBean doesn't need its state saved and restored automatically and return
     * null in {@link #getStateType()}, then this method will have no effect.
     * </p>
     *
     * @param restoredState The restored state by {@link StateKeeper} that will be rebound to the
     *                      MvcBean.
     */
    @Override
    public void restoreState(STATE restoredState) {
        if (getStateType() != null) {
            bindState(restoredState);
        }
        onRestored();
    }

    /**
     * Called after {@link #restoreState(Object)} is called.
     */
    public void onRestored() {
    }
}
