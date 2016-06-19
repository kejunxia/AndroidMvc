package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.event.BaseEventV;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusV;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

/**
 * Abstract view presenter. Presenter will subscribe to {@link com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC}
 * @param <MODEL> The view model of the presenter.
 */
public abstract class Controller<MODEL> extends Bean<MODEL> {
    Orientation orientation;

    @Inject
    @EventBusC
    private EventBus eventBus2C;

    @Inject
    @EventBusV
    private EventBus eventBus2V;

    @Inject
    protected ExecutorService executorService;

    @Inject
    protected UiThreadRunner uiThreadRunner;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Called when the presenter is created. Note that it could be called either when the
     * presenter is instantiated for the first time or restored by views.
     * <p/>
     * <p>The model of the presenter will be instantiated by model's default no-argument
     * constructor here whe {@link #modelType()} doesn't return null.</p>
     */
    public void onCreated() {
        super.onCreated();

        if (uiThreadRunner == null) {
            uiThreadRunner = new UiThreadRunner() {
                @Override
                public boolean isOnUiThread() {
                    return true;
                }

                @Override
                public void run(Runnable runnable) {
                    runnable.run();
                }
            };
        }

        eventBus2C.register(this);
    }

    protected Orientation currentOrientation() {
        return orientation;
    }

    /**
     * Bind the model to the controller that will be reflected in the corresponding fragment.
     * @param reason Why the model needs to be bound
     */
    public void onBindModel(Reason reason) {
    }

    /**
     * Called when corresponding fragment's onResume is called
     */
    public void onResume() {
    }

    /**
     * Called when corresponding fragment is about to be pushed to background
     */
    public void onPushingToBackground() {
    }

    /**
     * Called when corresponding fragment returns foreground from background <b>ONLY</b> when the
     * model doesn't need to be rebound to the controller. For example, if the fragment is rotated
     * or recreated then this method won't be called. But if home button pressed and then then the
     * app is brought back to front without being killed by the OS, this method will be called.
     */
    public void onReturnForeground() {
    }

    /**
     * Called when corresponding fragment popped out from back history
     */
    public void onPoppedOutToFront() {
    }

    /**
     * Called when corresponding fragment's orientation changed
     */
    public void onOrientationChanged(Orientation last, Orientation current) {
        orientation = current;
    }

    /**
     * Called when corresponding fragment's onPause is called
     */
    public void onPause() {
    }

    /**
     * Called when the presenter is disposed. This occurs when the presenter is de-referenced and
     * not retained by any objects.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        eventBus2C.unregister(this);
    }

    /**
     * Get the view model the presenter is holding. Don't write but only read the model from view.
     * Should only presenter write the model.
     *
     * @return Null if the presenter doesn't need to get its model saved and restored automatically
     * when {@link #modelType()} returns null.
     */
    @Override
    public MODEL getModel() {
        return super.getModel();
    }

    @Override
    public abstract Class<MODEL> modelType();

    public void bindModel(Object sender, MODEL model) {
        super.bindModel(model);
    }

    /**
     * Run a task on threads supplied by injected {@link ExecutorService} without a callback. By
     * default it runs tasks on separate threads by {@link ExecutorService} injected from AndroidMvc
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
     * on separate threads by {@link ExecutorService} injected from AndroidMvc framework. A simple
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
     * <p>The callback will be guaranteed to be run Android's UI thread</p>
     *
     * @param sender          Who wants run the task
     * @param executorService The executor service managing how the task will be run
     * @param task            The task
     * @param callback        The callback
     * @return The monitor to track the state of the execution of the task. It also can cancel the
     * task.
     *
     */
    protected <RESULT> Task.Monitor<RESULT> runTask(Object sender, ExecutorService executorService,
                                   final Task<RESULT> task, final Task.Callback<RESULT> callback) {
        final Task.Monitor<RESULT> monitor = new Task.Monitor(task, uiThreadRunner, callback);

        if (monitor.getState() == Task.Monitor.State.CANCELED) {
            return null;
        }

        monitor.setState(Task.Monitor.State.STARTED);

        if (callback != null) {
            callback.onStarted();
        }

        monitor.setFuture(executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    final RESULT result = task.execute(monitor);

                    if (monitor.getState() != Task.Monitor.State.CANCELED) {
                        monitor.setState(Task.Monitor.State.DONE);

                        if (callback != null) {
                            if (uiThreadRunner.isOnUiThread()) {
                                callback.onSuccess(result);
                                callback.onFinally();
                            } else {
                                uiThreadRunner.run(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onSuccess(result);
                                        callback.onFinally();
                                    }
                                });
                            }
                        }
                    }
                } catch (final Exception e) {
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
                            if (callback != null) {
                                if (uiThreadRunner.isOnUiThread()) {
                                    callback.onException(e);
                                    callback.onFinally();
                                } else {
                                    uiThreadRunner.run(new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onException(e);
                                            callback.onFinally();
                                        }
                                    });
                                }
                            }
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

    /**
     * Post the event to views. It automatically guarantees the event will be received
     * and run on UI thread of Android
     * @param eventV The event
     */
    protected void postEvent(final BaseEventV eventV) {
        if (uiThreadRunner.isOnUiThread()) {
            eventBus2V.post(eventV);
        } else {
            uiThreadRunner.run(new Runnable() {
                @Override
                public void run() {
                    eventBus2V.post(eventV);
                }
            });
        }

    }
}
