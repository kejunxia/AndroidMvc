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

package com.shipdream.lib.android.mvc.service;

public interface NetworkService {
    /**
     * Network status without depending on Android framework which can be easier to test on JVM
     */
    enum NetworkStatus {
        /**
         * On mobile network
         */
        MOBILE,
        /**
         * On WIFI
         */
        WIFI,
        status, /**
         * Not connected
         */
        NOT_CONNECTED
    }

    /**
     * Get the current network status of the mobile device
     * @return The network status
     */
    NetworkStatus getCurrentNetworkStatus();
}
