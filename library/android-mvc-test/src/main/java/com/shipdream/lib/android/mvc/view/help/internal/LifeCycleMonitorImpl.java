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

package com.shipdream.lib.android.mvc.view.help.internal;

import android.os.Bundle;
import android.view.View;

import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.view.help.LifeCycleMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifeCycleMonitorImpl implements LifeCycleMonitor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        logger.info("Lifecycle method invoked: onCreated");
    }

    @Override
    public void onCreateView(View rootView, Bundle savedInstanceState) {
        logger.info("Lifecycle method invoked: onCreateView");
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        logger.info("Lifecycle method invoked: onViewCreated");
    }

    @Override
    public void onViewReady(View rootView, Bundle savedInstanceState, Reason reason) {
        logger.info("Lifecycle method invoked: onViewReady, reason?: " + reason.toString());
    }

    @Override
    public void onResume() {
        logger.info("Lifecycle method invoked: onResume");
    }

    @Override
    public void onReturnForeground() {
        logger.info("Lifecycle method invoked: onReturnForeground");
    }

    @Override
    public void onPushToBackStack() {
        logger.info("Lifecycle method invoked: onPushToBackStack");
    }

    @Override
    public void onPopAway() {
        logger.info("Lifecycle method invoked: onPopAway");
    }

    @Override
    public void onPoppedOutToFront() {
        logger.info("Lifecycle method invoked: onPoppedOutToFront");
    }

    @Override
    public void onOrientationChanged(int lastOrientation, int currentOrientation) {
        logger.info("Lifecycle method invoked: onOrientationChanged");
    }

    @Override
    public void onDestroyView() {
        logger.info("Lifecycle method invoked: onDestroyView");
    }

    @Override
    public void onDestroy() {
        logger.info("Lifecycle method invoked: onDestroy");
    }
}
