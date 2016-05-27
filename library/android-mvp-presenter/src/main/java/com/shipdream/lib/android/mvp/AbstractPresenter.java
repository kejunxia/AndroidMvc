package com.shipdream.lib.android.mvp;

import com.shipdream.lib.android.mvp.event.bus.EventBus;
import com.shipdream.lib.android.mvp.event.bus.annotation.EventBusC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

/**
 * Abstract view presenter. Presenter will subscribe to {@link EventBusC}
 * @param <MODEL> The view model of the presenter.
 */
public class AbstractPresenter<MODEL> extends Bean<MODEL> {
    @Inject
    @EventBusC
    EventBus eventBus2C;

    @Inject
    protected ExecutorService executorService;

    protected Logger logger = LoggerFactory.getLogger(getClass());

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

    /**
     * Run a task on threads supplied by injected {@link ExecutorService} without a callback. By
     * default it runs tasks on separate threads by {@link ExecutorService} injected from AndroidMvp
     * framework. A simple {@link ExecutorService} that runs tasks on the same thread in test cases
     * to make the test easier.
     * @param sender          Who wants run the task
     * @param task            The task
     * @return The monitor to track the state of the execution of the task. It also can cancel the
     * task.
     *
     */
    protected Task.Monitor runTask(Object sender, final Task task) {
        return runTask(sender, executorService, task, null);
    }

    /**
     * Run a task on threads supplied by injected {@link ExecutorService}. By default it runs tasks
     * on separate threads by {@link ExecutorService} injected from AndroidMvp framework. A simple
     * {@link ExecutorService} that runs tasks on the same thread in test cases to make the test
     * easier.
     * @param sender          Who wants run the task
     * @param task            The task
     * @param callback        The callback
     * @return The monitor to track the state of the execution of the task. It also can cancel the
     * task.
     */
    protected Task.Monitor runTask(Object sender, final Task task, final Task.Callback callback) {
        return runTask(sender, executorService, task, callback);
    }

    /**
     * Run a task on the threads supplied by the given {@link ExecutorService}. The task could be
     * run either asynchronously or synchronously depending on the given executorService.
     *
     * @param sender          Who wants run the task
     * @param executorService The executor service managing how the task will be run
     * @param task            The task
     * @param callback        The callback
     * @return The monitor to track the state of the execution of the task. It also can cancel the
     * task.
     *
     */
    protected Task.Monitor runTask(Object sender, ExecutorService executorService,
                                   final Task task, final Task.Callback callback) {
        final Task.Monitor monitor = new Task.Monitor(task, callback);

        monitor.setFuture(executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (monitor.getState() == Task.Monitor.State.CANCELED) {
                    return null;
                }

                monitor.setState(Task.Monitor.State.STARTED);
                if (callback != null) {
                    callback.onStarted();
                }

                try {
                    task.execute(monitor);

                    if (monitor.getState() != Task.Monitor.State.CANCELED) {
                        monitor.setState(Task.Monitor.State.DONE);

                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                } catch (Exception e) {
                    boolean interruptedByCancel = false;
                    if (e instanceof InterruptedException) {
                        if (monitor.getState() == Task.Monitor.State.INTERRUPTED) {
                            interruptedByCancel = true;
                        }
                    }
                    //If the exception is an interruption caused by cancelling, then ignore it
                    if (!interruptedByCancel) {
                        monitor.setState(Task.Monitor.State.ERRED);
                        if (callback != null) {
                            callback.onException(e);
                        } else {
                            logger.warn(e.getMessage(), e);
                        }
                    }
                }

                return null;
            }
        }));

        return monitor;
    }

}
