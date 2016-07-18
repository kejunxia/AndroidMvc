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
import com.shipdream.lib.android.mvc.UiView;
import com.shipdream.lib.android.mvc.samples.simple.mvp.manager.CounterManager;

import javax.inject.Inject;

public class CounterServiceController extends AbstractController<Void, CounterServiceController.View> {
    @Override
    public Class modelType() {
        return null;
    }

    public interface View extends UiView{
        void counterFinished();
    }

    private class AutoCounter implements Runnable {
        private int count = 0;
        private boolean canceled = false;

        @Override
        public void run() {
            if (!canceled) {
                if (count ++ <= AUTO_FINISH_COUNT) {
                    counterManager.setCount(this, counterManager.getModel().getCount() + 1);

                    uiThreadRunner.postDelayed(this, 1000);
                } else {
                    view.counterFinished();
                }
            }
        }

        private void cancel() {
            canceled = true;
        }
    }

    private static final int AUTO_FINISH_COUNT = 10;
    private AutoCounter autoCounter;

    @Inject
    private NavigationManager navigationManager;

    @Inject
    private CounterManager counterManager;

    private void onEvent(CounterManager.Event2C.OnCounterUpdated event) {
        view.update();
    }

    public int getCount() {
        return counterManager.getModel().getCount();
    }

    public void startAutoIncrement() {
        stopAutoIncrement();
        autoCounter = new AutoCounter();
        autoCounter.run();
    }

    public void stopAutoIncrement() {
        if (autoCounter != null) {
            autoCounter.cancel();
        }
    }
}
