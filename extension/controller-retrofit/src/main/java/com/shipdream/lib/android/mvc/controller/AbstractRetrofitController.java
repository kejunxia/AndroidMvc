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

package com.shipdream.lib.android.mvc.controller;

import com.shipdream.lib.android.mvc.controller.internal.AsyncExceptionHandler;
import com.shipdream.lib.android.mvc.controller.internal.AsyncTask;
import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;

import java.util.concurrent.ExecutorService;

import retrofit.RetrofitError;

/**
 * Controller will execute http async tasks with retrofit.
 */
public abstract class AbstractRetrofitController<MODEL> extends BaseControllerImpl<MODEL>{

    /**
     * Run a http async task with retrofit services on the given ExecutorService.
     * <ul>
     *     <li>{@link OnNetworkErrorEvent} will be fired when a retrofit network error is detected</li>
     * </ul>
     * @param sender                  Who initiated this task
     * @param httpTask                A runnable task wrapping retrofit service
     * @param retrofitErrorHandler    Error handler to handle retrofit error
     * @param nonRetrofitErrorHandler Error handler for errors other than retrofit error.
     *                                <strong>Note:</strong> When null is given, all non-retrofit
     *                                exceptions will be suppressed with a warning level log.
     */
    protected void runHttpTask(final Object sender, final AsyncTask httpTask,
                               final RetrofitErrorHandler retrofitErrorHandler,
                               final AsyncExceptionHandler nonRetrofitErrorHandler){
        runAsyncTask(sender,
                new AsyncTask(){
                    @Override
                    public void execute() throws Exception{
                        httpTask.execute();
                    }
                },
                new AsyncExceptionHandler(){
                    @Override
                    public void handleException(Exception e){
                        handleAsyncError(sender, e, retrofitErrorHandler, nonRetrofitErrorHandler);
                    }
                });
    }

    /**
     * Run a http async task with retrofit services on the given ExecutorService.
     * <ul>
     *     <li>{@link OnNetworkErrorEvent} will be fired when a retrofit network error is detected</li>
     * </ul>
     *
     * @param sender                  Who initiated this task
     * @param executorService         The executor service provided to execute the async task
     * @param httpTask                A runnable task wrapping retrofit service
     * @param retrofitErrorHandler    Error handler to handle retrofit error
     * @param nonRetrofitErrorHandler Error handler for errors other than retrofit error.
     *                                <strong>Note:</strong> When null is given, all non-retrofit
     *                                exceptions will be suppressed with a warning level log.
     */
    protected void runHttpTask(final Object sender, ExecutorService executorService,
                               final AsyncTask httpTask,
                               final RetrofitErrorHandler retrofitErrorHandler,
                               final AsyncExceptionHandler nonRetrofitErrorHandler){
        runAsyncTask(sender, executorService,
                new AsyncTask(){
                    @Override
                    public void execute() throws Exception{
                        httpTask.execute();
                    }
                },
                new AsyncExceptionHandler(){
                    @Override
                    public void handleException(Exception e){
                        handleAsyncError(sender, e, retrofitErrorHandler, nonRetrofitErrorHandler);
                    }
                });
    }

    private void handleAsyncError(Object sender, Exception e, RetrofitErrorHandler retrofitErrorHandler,
                                  AsyncExceptionHandler nonRetrofitErrorHandler) {
        if (e instanceof RetrofitError){
            RetrofitError error = (RetrofitError) e;
            boolean handUpNetworkErrorEvent = !retrofitErrorHandler.handleError(sender, error);
            if (handUpNetworkErrorEvent && error.getKind() == RetrofitError.Kind.NETWORK){
                //the retrofit error handler passed on the error so we can post a network error event.
                postC2VEvent(new OnNetworkErrorEvent(sender, error));
            }
        } else{
            if (nonRetrofitErrorHandler == null){
                logger.warn("Unhandled exception detected: {}", e.getMessage(), e);
            } else{
                nonRetrofitErrorHandler.handleException(e);
            }
        }
    }
}
