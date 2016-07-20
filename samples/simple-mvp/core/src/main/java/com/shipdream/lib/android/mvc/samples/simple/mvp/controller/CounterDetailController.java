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

package com.shipdream.lib.android.mvc.samples.simple.mvp.controller;

import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.samples.simple.mvp.manager.CounterManager;

import javax.inject.Inject;

public class CounterDetailController extends AbstractScreenController {
    private class ContinuousCounter implements Runnable {
        private final boolean incrementing;
        private boolean canceled = false;
        private static final long INTERVAL = 200;

        public ContinuousCounter(boolean incrementing) {
            this.incrementing = incrementing;
        }

        @Override
        public void run() {
            if (!canceled) {
                if (incrementing) {
                    increment(this);
                } else {
                    decrement(this);
                }

                uiThreadRunner.postDelayed(this, INTERVAL);
            }
        }

        private void cancel() {
            canceled = true;
        }
    }

    @Override
    public Class modelType() {
        return null;
    }

    @Inject
    private NavigationManager navigationManager;

    @Inject
    private CounterManager counterManager;

    private ContinuousCounter incrementer;
    private ContinuousCounter decrementer;

    @Override
    public void onViewReady(Reason reason) {
        super.onViewReady(reason);
        view.update();
    }

    public void startContinuousIncrement() {
        stopContinuousIncrement();
        incrementer = new ContinuousCounter(true);
        incrementer.run();
    }

    public void stopContinuousIncrement() {
        if (incrementer != null) {
            incrementer.cancel();
        }
    }

    public void startContinuousDecrement() {
        stopContinuousDecrement();
        decrementer = new ContinuousCounter(false);
        decrementer.run();
    }

    public void stopContinuousDecrement() {
        if (decrementer != null) {
            decrementer.cancel();
        }
    }

    public String getCount() {
        return String.valueOf(counterManager.getModel().getCount());
    }

    public void increment(Object sender) {
        int count = counterManager.getModel().getCount();
        counterManager.setCount(sender, ++count);
    }

    public void decrement(Object sender) {
        int count = counterManager.getModel().getCount();
        counterManager.setCount(sender, --count);
    }

    public void goBackToBasicView(Object sender) {
        navigationManager.navigate(sender).back();
    }

    private void onEvent(CounterManager.Event2C.OnCounterUpdated event) {
        view.update();
    }
}
