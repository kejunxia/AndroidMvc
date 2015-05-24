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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit.client.Client;

/**
 * Controller will execute http async tasks with retrofit and support switching endpoints dynamically
 */
public abstract class AbstractRetrofitMultiEndpointsController<MODEL> extends AbstractRetrofitController<MODEL> {
    @Inject
    Client mClient;

    private Map<String, RetrofitServiceFactory> mHttpServiceFactoryCache = new HashMap<>();

    /**
     * Get a service for a specific endpoint. A new service will be created if not cached yet, otherwise cached service
     * will be returned.
     * @param endpoint The string of the endpoint
     * @param serviceTypeClass The service type
     * @return The service
     */
    protected <T> T getService(String endpoint, Class<T> serviceTypeClass) {
        return getHttpServiceFactory(endpoint).createService(serviceTypeClass);
    }

    /**
     * Override to config newly created HttpRetrofitServiceFactory for specific endpoint
     * @param factory The factory to generate service
     * @param endpoint The string of the endpoint
     */
    protected void onCreateNewHttpRetrofitServiceFactory(RetrofitServiceFactory factory, String endpoint) {}

    private RetrofitServiceFactory getHttpServiceFactory(String endpoint) {
        //Cache service factory
        if(mHttpServiceFactoryCache.get(endpoint) == null) {
            RetrofitServiceFactory factory = new RetrofitServiceFactory(endpoint, mClient);
            onCreateNewHttpRetrofitServiceFactory(factory, endpoint);
            mHttpServiceFactoryCache.put(endpoint, factory);
        }
        return mHttpServiceFactoryCache.get(endpoint);
    }
}
