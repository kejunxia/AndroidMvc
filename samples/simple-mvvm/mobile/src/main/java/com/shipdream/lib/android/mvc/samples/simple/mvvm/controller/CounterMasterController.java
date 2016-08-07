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

package com.shipdream.lib.android.mvc.samples.simple.mvvm.controller;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.Task;
import com.shipdream.lib.android.mvc.UiView;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.dto.IpPayload;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.factory.ServiceFactory;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.http.IpService;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.manager.CounterManager;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.BR;
import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Response;


public class CounterMasterController extends AbstractScreenController<CounterMasterController.Model,
        CounterMasterController.View> {
    @Override
    public Class<Model> modelType() {
        return Model.class;
    }

    /**
     * The view model of the CounterMasterScreen
     */
    public static class Model extends BaseObservable {
        private String count;
        private String ipAddress;
        private boolean progressVisible;

        public void setCount(String count) {
            this.count = count;
            notifyPropertyChanged(BR.count);
        }

        @Bindable
        public String getCount() {
            return count;
        }

        @Bindable
        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            notifyPropertyChanged(BR.ipAddress);
        }

        @Bindable
        public boolean isProgressVisible() {
            return progressVisible;
        }

        public void setProgressVisible(boolean progressVisible) {
            this.progressVisible = progressVisible;
            notifyPropertyChanged(BR.progressVisible);
        }
    }

    public interface View extends UiView{
        void showHttpError(int statusCode, String message);
        void showNetworkError(IOException e);
    }

    @Inject
    private NavigationManager navigationManager;

    @Inject
    private CounterManager counterManager;

    @Inject
    private ServiceFactory serviceFactory;

    @Override
    public void onViewReady(Reason reason) {
        super.onViewReady(reason);
        //Initialize model
        getModel().setCount(String.valueOf(counterManager.getModel().getCount()));
    }

    public void increment(Object sender) {
        int count = counterManager.getModel().getCount();
        counterManager.setCount(sender, ++count);
    }

    public void decrement(Object sender) {
        int count = counterManager.getModel().getCount();
        counterManager.setCount(sender, --count);
    }

    public void refreshIp() {
        getModel().setProgressVisible(true);

        runTask(new Task<Response<IpPayload>>() {
            @Override
            public Response<IpPayload> execute(Monitor<Response<IpPayload>> monitor) throws Exception {
                return serviceFactory.createService(IpService.class)
                        .getIp("json").execute();
            }
        }, new Task.Callback<Response<IpPayload>>() {
            @Override
            public void onSuccess(Response<IpPayload> response) {
                super.onSuccess(response);
                if (response.isSuccessful()) {
                    getModel().setIpAddress(response.body().getIp());
                } else {
                    view.showHttpError(response.code(), response.message());
                    logger.warn("Http error to get ip. error({}): {}", response.code(), response.message());
                }
            }

            @Override
            public void onException(Exception e) {
                if (e instanceof IOException) {
                    view.showNetworkError((IOException) e);
                }

                logger.warn(e.getMessage(), e);
            }

            @Override
            public void onFinally() {
                super.onFinally();
                getModel().setProgressVisible(false);
            }
        });
    }

    /**
     * Go to detail view.
     * @param sender
     */
    public void goToDetailScreen(Object sender) {
        //Navigate to CounterDetailController which is paired by CounterDetailScreen
        navigationManager.navigate(sender).to(CounterDetailController.class);
    }

    /**
     * Event subscriber: notified by counterManager
     * @param event
     */
    private void onEvent(CounterManager.Event2C.OnCounterUpdated event) {
        getModel().setCount(String.valueOf(event.getCount()));
    }

}
