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

import com.shipdream.lib.android.mvc.event.BaseEventC2V;

import retrofit.RetrofitError;

/**
 * C2VEvent for Http network error. This event will be raised only if the type of the http error is caused by network issue.
 */
public class OnNetworkErrorEvent extends BaseEventC2V {
    private RetrofitError error;

    public OnNetworkErrorEvent(Object sender, RetrofitError error) {
        super(sender);
        this.error = error;
    }

    public RetrofitError getError(){
        return error;
    }
}