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

package com.shipdream.lib.android.mvc.service.internal;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.shipdream.lib.android.mvc.service.NetworkService;

/**
 *
 */
public class NetworkServiceImpl implements NetworkService {
    private Context context;

    public NetworkServiceImpl(Context context){
        this.context = context;
    }

    @Override
    public NetworkStatus getCurrentNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return NetworkStatus.WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return NetworkStatus.MOBILE;
        }
        return NetworkStatus.NOT_CONNECTED;
    }

}
