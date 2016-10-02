/*
 * Copyright 2016 Kejun Xia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusV;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;

/**
 * <p>
 * A controller is a delegate of a Android view. It holds a {@link UiView} that needs to be implemented
 * by the corresponding Android view. Whenever the controller changes its model, it calls {@link UiView#update()}
 * to update the view. Be careful when you are calling {@link UiView#update()} on the non-UI thread,
 * because in this case, it needs to use {@link #uiThreadRunner} to post a runnable wrapping
 * {@link UiView#update()} to ensure the view is updated on UI thread. Use {@link #runTask(Task)} to
 * run heavy actions, especially network calls, on non-UI thread.
 * </p>
 *
 * <p>
 * In the above way the controller works as a presenter in MVP pattern. If you prefer MVVM pattern.
 * You can define events and {@link #postEvent(Object)} to the view. Since {@link #postEvent(Object)}
 * guarantees the event is posted onto UI thread, you don't need to worry about on which thread the
 * event is posted.
 * </p>
 *
 * <p>
 * When some code needs to run on non-UI thread, use {@link #runTask(Task)},
 * {@link #runTask(Task, Task.Callback)} to run it on a different thread. Make sure if
 * {@link UiView#update()} needs to be called in scope of @link Task#execute(Task.Monitor)} use
 * {@link #uiThreadRunner} to post back to UI thread.
 * </p>
 *
 * <p>
 * The controller has 4 injected fields. They can be replaced by providing special or mocking objects
 * in unit tests.
 * <ul>
 *     <li>a {@link EventBus} annotated by {@link EventBusC} to receive events from managers and other non ui components</li>
 *     <li>a {@link EventBus} annotated by {@link EventBusV} to send event to Android views</li>
 *     <li>a {@link ExecutorService} that runs {@link Task} via {@link #runTask(Task)} on non-UI thread.
 *     by default, it has a fixed thread tool at size 10. You can inject a mocked {@link ExecutorService}
 *     that runs everything on the main thread in your <b>Unit Tests</b> to avoid multi-thread complexity.</li>
 *     <li>a protected {@link UiThreadRunner} to post a {@link Runnable} onto UI thread. Make sure
 *     use it to {@link UiView#update()} view inside method block {@link Task#execute(Task.Monitor)}
 *     which is run on non-UI thread. It's NOT necessary to use it in callback methods in
 *     {@link Task.Callback} since the framework has already guaranteed it.
 *     In non-Android environment, typically in unit tests, the framework will also provide a default
 *     uiThreadRunner that runs everything on the same thread as the caller's.</li>
 * </ul>
 * </p>
 * @param <MODEL> The view model of the controller.
 */
public abstract class Controller<MODEL, VIEW extends UiView> extends Bean<MODEL> {
    protected VIEW view;

    @Inject
    @EventBusC
    private EventBus eventBusC;

    @Inject
    @EventBusV
    private EventBus eventBusV;

    @Inject
    private ExecutorService executorService;

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
            //Use mvc graph's default uiThreadRunner
            uiThreadRunner = Mvc.graph().uiThreadRunner;
        }

        eventBusC.register(this);
    }

    /**
     * Called when the controller is destroyed. This occurs when the controller is de-referenced and
     * not retained by any objects.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        eventBusC.unregister(this);
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
     *
     * <p><b>
     * User the protected property {@link UiThreadRunner} to post action back to main UI thread
     * in the method block of {@link Task#execute(Task.Monitor)}.
     * </b></p>
     * <pre>
     *      uiThreadRunner.post(new Runnable() {
     *          @Override
     *          public void run() {
     *              view.update();
     *          }
     *      });
     * </pre>
     *
     * @param task            The task
     * @return The monitor to track the state of the execution of the task. It also can cancel the
     * task.
     *
     */
    protected <RESULT> Task.Monitor<RESULT> runTask(final Task<RESULT> task) {
        return runTask(executorService, task, null);
    }

    /**
     * Run a task on threads supplied by injected {@link ExecutorService}. By default it runs tasks
     * on separate threads by {@link ExecutorService} injected from AndroidMvc framework. A simple
     * {@link ExecutorService} that runs tasks on the same thread in test cases to make the test
     * easier.
     *
     * <p>The methods of callback will be guaranteed to be run Android's UI thread</p>
     *
     * <p><b>
     * User the protected property {@link UiThreadRunner} to post action back to main UI thread
     * in the method block of {@link Task#execute(Task.Monitor)}.
     * </b></p>
     * <pre>
     *      uiThreadRunner.post(new Runnable() {
     *          @Override
     *          public void run() {
     *              view.update();
     *          }
     *      });
     * </pre>
     *
     * @param task            The task
     * @param callback        The callback
     * @return The monitor to track the state of the execution of the task. It also can cancel the
     * task.
     */
    protected <RESULT> Task.Monitor<RESULT> runTask(final Task<RESULT> task,
                                                    final Task.Callback<RESULT> callback) {
        return runTask(executorService, task, callback);
    }

    /**
     * Run a task on the threads supplied by the given {@link ExecutorService}. The task could be
     * run either asynchronously or synchronously depending on the given executorService.
     *
     * <p>The methods of callback will be guaranteed to be run Android's UI thread</p>
     *
     * <p><b>
     * User the protected property {@link UiThreadRunner} to post action back to main UI thread
     * in the method block of {@link Task#execute(Task.Monitor)}.
     * </b></p>
     * <pre>
     *      uiThreadRunner.post(new Runnable() {
     *          @Override
     *          public void run() {
     *              view.update();
     *          }
     *      });
     * </pre>
     *
     * @param executorService The executor service managing how the task will be run
     * @param task            The task
     * @param callback        The callback
     * @return The monitor to track the state of the execution of the task. It also can cancel the
     * task.
     *
     */
    protected <RESULT> Task.Monitor<RESULT> runTask(ExecutorService executorService,
                                   final Task<RESULT> task, final Task.Callback<RESULT> callback) {
        final Task.Monitor<RESULT> monitor = new Task.Monitor(task, uiThreadRunner, callback);

        Future<Void> future = executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (monitor.getState() == Task.Monitor.State.CANCELED) {
                    return null;
                }

                monitor.setState(Task.Monitor.State.STARTED);

                if (callback != null) {
                    uiThreadRunner.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onStarted(monitor);
                        }
                    });
                }

                try {
                    final RESULT result = task.execute(monitor);

                    if (monitor.getState() != Task.Monitor.State.CANCELED) {
                        monitor.setState(Task.Monitor.State.DONE);

                        if (callback != null) {
                            uiThreadRunner.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(result);
                                    callback.onFinally();
                                }
                            });
                        }
                    }
                } catch (final Exception e) {
                    if (e instanceof MvcGraphException) {
                        //Injection exception will always be thrown out since it's a development
                        //time error
                        uiThreadRunner.post(new Runnable() {
                            @Override
                            public void run() {
                                throw new RuntimeException(e);
                            }
                        });
                    }

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
                            uiThreadRunner.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        callback.onException(e);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    } finally {
                                        callback.onFinally();
                                    }
                                }
                            });
                        } else {
                            uiThreadRunner.post(new Runnable() {
                                @Override
                                public void run() {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }
                }

                return null;
            }
        });

        monitor.setFuture(future);

        return monitor;
    }

    /**
     * <p>
     * Post the event to views. It automatically guarantees the event will be received
     * and run on UI thread of Android.
     * </p>
     *
     * <p>
     * The event will be captured by views or any objects registered to {@link EventBus} annotated
     * by {@link EventBusV} and has corresponding method named onEvent() with single parameter with
     * the same type of the event. For example
     * </p>
     * <pre>
     *  public class OnTextChangedEvent {
     *      private String text;
     *
     *      public OnTextChangedEvent(String text) {
     *          this.text = text;
     *      }
     *
     *      public String getText() {
     *          return text;
     *      }
     *  }
     *
     *  public class SomeView {
     *      @ Inject
     *      @ EventBusV
     *      private EventBus eventBusV;
     *
     *      private TextView textView;
     *
     *      public class SomeView() {
     *          //This is just needed when you have a view not inheriting MvcFragment, MvcService or etc.
     *          //In MvcFragment or MvcService will register to the event bus in onCreate automatically.
     *          eventBusV.register(this);
     *      }
     *
     *      public void onEvent(OnTextChangedEvent onTextChangedEvent) {
     *          textView.setText(onTextChangedEvent.getText());
     *      }
     *  }
     *
     *  public class SomeController{
     *      private void func() {
     *          postEvent(new OnTextChangedEvent("Controller Wants to change text"));
     *      }
     *  }
     * </pre>
     *
     * @param event2V The event
     * @deprecated Use {@link #postEvent2V(Object)} instead
     */
    protected void postEvent(final Object event2V) {
        postEvent2V(event2V);
    }

    /**
     * <p>
     * Post the event to other core components such as controllers and managers. Event will be
     * captured on the thread of the invoker.
     * </p>
     *
     * <p>
     * The event will be captured by {@link Controller}s, {@link Manager}s or others registered to
     * {@link EventBus} annotated by {@link EventBusC} and has corresponding method named onEvent()
     * with single parameter with the same type of the event. For example
     * </p>
     *
     * @param event2C The event to other {@link Controller}s, {@link Manager}s
     */
    protected void postEvent2C(final Object event2C) {
        eventBusC.post(event2C);
    }

    /**
     * <p>
     * Post the event to views. It automatically guarantees the event will be received
     * and run on UI thread of Android.
     * </p>
     *
     * <p>
     * The event will be captured by views or any objects registered to {@link EventBus} annotated
     * by {@link EventBusV} and has corresponding method named onEvent() with single parameter with
     * the same type of the event. For example
     * </p>
     * <pre>
     *  public class OnTextChangedEvent {
     *      private String text;
     *
     *      public OnTextChangedEvent(String text) {
     *          this.text = text;
     *      }
     *
     *      public String getText() {
     *          return text;
     *      }
     *  }
     *
     *  public class SomeView {
     *      @ Inject
     *      @ EventBusV
     *      private EventBus eventBusV;
     *
     *      private TextView textView;
     *
     *      public class SomeView() {
     *          //This is just needed when you have a view not inheriting MvcFragment, MvcService or etc.
     *          //In MvcFragment or MvcService will register to the event bus in onCreate automatically.
     *          eventBusV.register(this);
     *      }
     *
     *      public void onEvent(OnTextChangedEvent onTextChangedEvent) {
     *          textView.setText(onTextChangedEvent.getText());
     *      }
     *  }
     *
     *  public class SomeController{
     *      private void func() {
     *          postEvent(new OnTextChangedEvent("Controller Wants to change text"));
     *      }
     *  }
     * </pre>
     *
     * @param eventV The event
     */
    protected void postEvent2V(final Object eventV) {
        eventBusV.post(eventV);
    }
}
