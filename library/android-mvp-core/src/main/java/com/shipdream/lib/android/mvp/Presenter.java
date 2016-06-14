package com.shipdream.lib.android.mvp;

import com.shipdream.lib.android.mvp.event.bus.EventBus;
import com.shipdream.lib.android.mvp.event.bus.annotation.EventBusC;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

/**
 * Abstract view presenter. Presenter will subscribe to {@link EventBusC}
 * @param <MODEL> The view model of the presenter.
 */
public abstract class Presenter<MODEL> extends Bean<MODEL> {
    interface UiThreadRunner {
        /**
         * Indicates whether current thread is UI thread.
         * @return
         */
        boolean isOnUiThread();
        /**
         * Run the runnable on Android UI thread
         * @param runnable
         */
        void run(Runnable runnable);
    }

    @Inject
    @EventBusC
    EventBus eventBus2C;

    @Inject
    protected ExecutorService executorService;

    static UiThreadRunner uiThreadRunner;

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
        eventBus2C.register(this);
    }

    /**
     * Called when the presenter is disposed. This occurs when the presenter is de-referenced and
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
    protected Task.Monitor runTask(Object sender, ExecutorService executorService,
                                   final Task task, final Task.Callback callback) {
        final Task.Monitor monitor = new Task.Monitor(task, callback);

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
                    task.execute(monitor);

                    if (monitor.getState() != Task.Monitor.State.CANCELED) {
                        monitor.setState(Task.Monitor.State.DONE);

                        if (callback != null) {
                            postToUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (Presenter.uiThreadRunner.isOnUiThread()) {
                                        callback.onSuccess();
                                        callback.onFinally();
                                    } else {
                                        Presenter.uiThreadRunner.run(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onSuccess();
                                                callback.onFinally();
                                            }
                                        });
                                    }
                                }
                            });
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
                                postToUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (Presenter.uiThreadRunner.isOnUiThread()) {
                                            callback.onException(e);
                                            callback.onFinally();
                                        } else {
                                            Presenter.uiThreadRunner.run(new Runnable() {
                                                @Override
                                                public void run() {
                                                    callback.onException(e);
                                                    callback.onFinally();
                                                }
                                            });
                                        }
                                    }
                                });
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
     * Run the runnable on the UI thread
     * @param runnable runnable
     */
    protected void postToUiThread(@NotNull final Runnable runnable) {
        uiThreadRunner.run(new Runnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }
}
