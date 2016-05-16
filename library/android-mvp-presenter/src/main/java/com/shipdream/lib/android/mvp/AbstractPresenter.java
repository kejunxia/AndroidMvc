package com.shipdream.lib.android.mvp;

import com.shipdream.lib.android.mvp.event.bus.EventBus;
import com.shipdream.lib.android.mvp.event.bus.annotation.EventBusC;

import javax.inject.Inject;

/**
 * Abstract view presenter. Presenter will subscribe to {@link EventBusC}
 * @param <MODEL> The view model of the presenter.
 */
public class AbstractPresenter<MODEL> extends MvpBean<MODEL> {
    @Inject
    @EventBusC
    EventBus eventBus2C;

    /**
     * Called when the controller is constructed. Note that it could be called either when the
     * controller is instantiated for the first time or restored by views.
     * <p/>
     * <p>The model of the controller will be instantiated by model's default no-argument
     * constructor here whe {@link #modelType()} doesn't return null.</p>
     */
    public void onConstruct() {
        super.onConstruct();
        eventBus2C.register(this);
    }

    /**
     * Called when the controller is disposed. This occurs when the controller is de-referenced and
     * not retained by any objects.
     */
    @Override
    public void onDisposed() {
        super.onDisposed();
        eventBus2C.unregister(this);
    }

    /**
     * Get the view model the presenter is holding. Don't write but only read the model from view.
     * Should only presenter write the model.
     *
     * @return Null if the controller doesn't need to get its model saved and restored automatically
     * when {@link #modelType()} returns null.
     */
    @Override
    public MODEL getModel() {
        return super.getModel();
    }

    @Override
    public Class<MODEL> modelType() {
        return null;
    }

    public void bindModel(Object sender, MODEL model) {
        super.bindModel(model);
    }

}
