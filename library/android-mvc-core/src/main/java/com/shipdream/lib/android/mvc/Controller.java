package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusV;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

/**
 * Abstract view controller. Presenter will subscribe to {@link EventBusC}
 * @param <MODEL> The view model of the controller.
 */
public abstract class Controller<MODEL, VIEW extends UiView> extends Bean<MODEL> {
    protected VIEW view;

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
     * Called when the controller is created. Note that it could be called either when the
     * controller is instantiated for the first time or restored by views.
     * <p/>
     * <p>The model of the controller will be instantiated by model's default no-argument
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

    /**
     * Called when the controller is destroyed. This occurs when the controller is de-referenced and
     * not retained by any objects.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        eventBus2C.unregister(this);
    }

    /**
     * Get the view model the controller is holding. Don't write but only read the model from view.
     * Should only controller write the model.
     *
     * @return Null if the controller doesn't need to get its model saved and restored automatically
     * when {@link #modelType()} returns null.
     */
    @Override
    public MODEL getModel() {
        return super.getModel();
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
    protected <RESULT> Task.Monitor<RESULT> runTask(Object sender, final Task<RESULT> task) {
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
    protected <RESULT> Task.Monitor<RESULT> runTask(Object sender, final Task<RESULT> task,
                                                    final Task.Callback<RESULT> callback) {
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
    protected void postEvent(final Object eventV) {
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
