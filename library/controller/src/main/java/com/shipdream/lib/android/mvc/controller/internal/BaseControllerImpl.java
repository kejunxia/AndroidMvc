/*
 * Copyright 2015 Kejun Xia
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

package com.shipdream.lib.android.mvc.controller.internal;


import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.shipdream.lib.android.mvc.Disposable;
import com.shipdream.lib.android.mvc.StateKeeper;
import com.shipdream.lib.android.mvc.StateManaged;
import com.shipdream.lib.android.mvc.controller.BaseController;
import com.shipdream.lib.android.mvc.event.BaseEventC2C;
import com.shipdream.lib.android.mvc.event.BaseEventC2V;
import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC2C;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusC2V;
import com.shipdream.lib.poke.util.ReflectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

/**
 *
 */
public abstract class BaseControllerImpl<MODEL> implements BaseController<MODEL>,
        StateManaged<MODEL>, Disposable {
    private static Handler sHandler;
    protected Logger mLogger = LoggerFactory.getLogger(getClass());

    @Inject
    @EventBusC2V
    EventBus mEventBusC2V;

    @Inject
    @EventBusC2C
    EventBus mEventBusC2C;

    @Inject
    ExecutorService mExecutorService;

    private Boolean sIsAndroid = null;
    private MODEL mModel;

    public BaseControllerImpl() {
        if (sIsAndroid == null) {
            try {
                Class.forName("android.os.Build");
                sIsAndroid = Build.VERSION.SDK_INT != 0;
            } catch (Exception e) {
                sIsAndroid = false;
            }
        }
    }

    @Override
    public void init() {
        mModel = createModelInstance();
        mEventBusC2C.register(this);
        onInitialized();
    }

    private MODEL createModelInstance() {
        if (getModelClassType() == null) {
            return null;
        } else {
            try {
                return new ReflectUtils.newObjectByType<>(getModelClassType()).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Fail to instantiate model by its default constructor");
            }
        }
    }

    /**
     * Called when the controller is newly initialized.
     */
    protected void onInitialized() {
    }

    /**
     * Called when the controller is disposed. When the controller is not referenced any more this
     * should be called.
     */
    @Override
    public void onDisposed() {
        mEventBusC2C.unregister(this);
        mLogger.trace("-Event bus unregistered for Controller - '{}'.", getClass().getName());
    }

    /**
     * @return @return Null if the controller doesn't need to get its state saved and restored
     * automatically when {@link #getModelClassType()} returns null. e.g. The controller
     * always loads resource from remote services so that its state can be thought persisted by the
     * remote services. Otherwise returns the model of the controller
     */
    @Override
    public MODEL getModel() {
        return mModel;
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
    final public MODEL getState() {
        return mModel;
    }

    /**
     * Subclass should override this method to provide the class type of the model of the controller.
     *
     * @return null when the controller doesn't need to get its state saved and restored
     * automatically by view. e.g. The controller always loads resource from remote services so that
     * its state can be thought managed by the remote services. Otherwise returns the class type
     */
    protected abstract Class<MODEL> getModelClassType();

    /**
     * Method of {@link StateManaged} that allows {@link StateKeeper} to save and get the state of
     * which is also the model the controller.
     *
     * @return The class type of the model of the controller
     */
    @Override
    final public Class<MODEL> getStateType() {
        return getModelClassType();
    }

    /**
     * Method of {@link StateManaged} that allows {@link StateKeeper} to save and get the state of
     * which is also the model the controller.
     * <p>
     * Note that if the controller doesn't need to get its state saved and restored
     * automatically. e.g. The controller always loads resource from remote services so that
     * its state can be thought persisted by the remote services when {@link #getModelClassType()}
     * returns null, the method will have no effect.
     * </p>
     *
     * @param restoredState The restored state by {@link StateKeeper} that will be bound to the
     *                      controller on the view referencing the controller is restored.
     */
    @Override
    final public void restoreState(MODEL restoredState) {
        if (getModelClassType() != null) {
            bindModel(this, restoredState);
        }
    }

    @Override
    public void bindModel(Object sender, MODEL model) {
        if (model == null) {
            throw new RuntimeException("Can't bind a null model to a controller explicitly.");
        }
        mModel = model;
    }

    /**
     * Help function to post the event to views on
     * <ul>
     * <li>Android main thread -- when detected android OS. Note that, if the caller is on main thread, event will be
     * execute immediately on the main thread. Otherwise it will be post to the main thread message queue.</li>
     * <li>Same thread of caller -- if on usual JVM</li>
     * </ul>
     *
     * @param c2vEvent Controller to View event to be broadcast
     */
    protected void postC2VEvent(final BaseEventC2V c2vEvent) {
        if (mEventBusC2V != null) {
            if (sIsAndroid) {
                if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                    mEventBusC2V.post(c2vEvent);
                } else {
                    //Android handler is presented, posting to the main thread on Android.
                    if (sHandler == null) {
                        sHandler = new Handler(Looper.getMainLooper());
                    }
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mEventBusC2V != null) {
                                mEventBusC2V.post(c2vEvent);
                            } else {
                                mLogger.warn("Trying to post event {} to EventBusC2V which is null", c2vEvent.getClass().getName());
                            }
                        }
                    });
                }
            } else {
                mEventBusC2V.post(c2vEvent);
            }
        } else {
            mLogger.warn("Trying to post event {} to EventBusC2V which is null", c2vEvent.getClass().getName());
        }
    }

    /**
     * Help function to post events to other controllers
     *
     * @param c2cEvent Controller to Controller event to be broadcast
     */
    protected void postC2CEvent(final BaseEventC2C c2cEvent) {
        if (mEventBusC2C != null) {
            mEventBusC2C.post(c2cEvent);
        } else {
            mLogger.warn("Trying to post event {} to EventBusC2C which is null", c2cEvent.getClass().getName());
        }
    }

    /**
     * Run async task on the default ExecutorService injected as a field of this class. Exceptions
     * occur during running the task will be suppressed but logged at warning level. <b>Be careful,
     * only use this method when you are sure there will be no exceptions occur during the execution
     * of async task, or you want to ignore all exceptions intentionally.</b> Otherwise use
     * {@link #runAsyncTask(Object, AsyncTask, AsyncExceptionHandler)} to handle errors explicitly.
     *
     * @param sender    who initiated this task
     * @param asyncTask task to execute
     * @return returns the reference of {@link AsyncTask} that can be used to query its state and cancel it.
     */
    protected AsyncTask runAsyncTask(Object sender, final AsyncTask asyncTask) {
        return runAsyncTask(sender, mExecutorService, asyncTask, null);
    }

    /**
     * Run async task on the default ExecutorService injected as a field of this class. Exceptions
     * occur during running the task will be handled by the given {@link AsyncExceptionHandler}.
     *
     * @param sender                who initiated this task
     * @param asyncTask             task to execute
     * @param asyncExceptionHandler error handler for the exception during running the task
     * @return the reference of {@link AsyncTask} that can be used to query its state and cancel it.
     */
    protected AsyncTask runAsyncTask(Object sender, final AsyncTask asyncTask,
                                     final AsyncExceptionHandler asyncExceptionHandler) {
        return runAsyncTask(sender, mExecutorService, asyncTask, asyncExceptionHandler);
    }

    /**
     * Run async task on the given ExecutorService. Exceptions occur during running the task will be
     * suppressed but logged at warning level. <b>Be careful, only use this method when you are sure
     * there will be no exceptions occur during the execution of async task, or you want to ignore
     * all exceptions intentionally.</b>  Otherwise use
     * {@link #runAsyncTask(Object, java.util.concurrent.ExecutorService, AsyncTask, AsyncExceptionHandler)}
     * to handle errors explicitly.
     *
     * @param sender          who initiated this task
     * @param executorService the executor service provided to execute the async task
     * @param asyncTask       task to execute
     * @return the reference of {@link AsyncTask} that can be used to query its state and cancel it.
     */
    protected AsyncTask runAsyncTask(Object sender, ExecutorService executorService,
                                     final AsyncTask asyncTask) {
        return runAsyncTask(sender, executorService, asyncTask, null);
    }


    /**
     * Run async task on the given ExecutorService. Exceptions occur during running the task will be
     * handled by the given {@link AsyncExceptionHandler}.
     *
     * @param sender                who initiated this task
     * @param executorService       the executor service provided to execute the async task
     * @param asyncTask             task to execute
     * @param asyncExceptionHandler error handler for the exception during running the task. If null
     *                              is given all exceptions occur during the execution of the async
     *                              task will be suppressed with warning level log.
     * @return the reference of {@link AsyncTask} that can be used to query its state and cancel it.
     */
    protected AsyncTask runAsyncTask(Object sender, ExecutorService executorService,
                                     final AsyncTask asyncTask,
                                     final AsyncExceptionHandler asyncExceptionHandler) {
        asyncTask.state = AsyncTask.State.RUNNING;
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    asyncTask.execute();
                    if (asyncTask.state != AsyncTask.State.CANCELED) {
                        asyncTask.state = AsyncTask.State.DONE;
                    }
                } catch (Exception e) {
                    asyncTask.state = AsyncTask.State.ERRED;
                    if (asyncExceptionHandler == null) {
                        mLogger.warn(e.getMessage(), e);
                    } else {
                        asyncExceptionHandler.handleException(e);
                    }
                }
            }
        });

        return asyncTask;
    }

}
