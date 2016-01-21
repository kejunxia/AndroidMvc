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

package com.shipdream.lib.android.mvc.samples.simple.controller;

import com.shipdream.lib.android.mvc.controller.BaseController;
import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.android.mvc.event.BaseEventV;
import com.shipdream.lib.android.mvc.samples.simple.model.CounterModel;

/**
 * Define controller contract and its events. And specify which model it manages by binding the
 * model type.
 */
public interface CounterController extends BaseController<CounterModel> {
    /**
     * Increment count and will raise {@link EventC2V.OnCounterUpdated}
     * @param sender Who requests this action
     */
    void increment(Object sender);

    /**
     * Decrement count and will raise {@link EventC2V.OnCounterUpdated}
     * @param sender Who requests this action
     */
    void decrement(Object sender);

    /**
     * Method to convert number to english
     */
    String convertNumberToEnglish(int number);

    /**
     * Navigate to LocationB by {@link NavigationController}to show advance view that can update
     * count continuously by holding buttons.
     * @param sender
     */
    void goToAdvancedView(Object sender);

    /**
     * Navigate back to LocationA by {@link NavigationController}to show basic view from LocationB
     * @param sender
     */
    void goBackToBasicView(Object sender);

    /**
     * Namespace the events for this controller by nested interface so that all its events would
     * be referenced as CounterController.EventC2V.BlaBlaEvent.
     */
    interface EventC2V {
        /**
         * Event to notify views counter has been updated
         */
        class OnCounterUpdated extends BaseEventV {
            private final int count;
            private final String countInEnglish;
            public OnCounterUpdated(Object sender, int count, String countInEnglish) {
                super(sender);
                this.count = count;
                this.countInEnglish = countInEnglish;
            }

            public int getCount() {
                return count;
            }

            public String getCountInEnglish() {
                return countInEnglish;
            }
        }
    }
}
