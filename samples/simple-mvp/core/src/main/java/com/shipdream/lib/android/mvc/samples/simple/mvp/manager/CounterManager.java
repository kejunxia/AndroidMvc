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

package com.shipdream.lib.android.mvc.samples.simple.mvp.manager;

import com.shipdream.lib.android.mvc.Manager;

public class CounterManager extends Manager<CounterManager.Model> {
    /**
     * Namespace the events for this controller by nested interface so that all its events would
     * be referenced as CounterController.EventC2V.BlaBlaEvent.
     */
    public interface Event2C {
        /**
         * Event2C to notify views counter has been updated
         */
        class OnCounterUpdated {
            private final int count;
            public OnCounterUpdated(int count) {
                this.count = count;
            }

            public int getCount() {
                return count;
            }
        }
    }

    public static class Model {
        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    /**
     * Just return the class type of the model managed by this controller
     */
    @Override
    public Class<Model> modelType() {
        return Model.class;
    }

    public void setCount(Object sender, int count) {
        getModel().setCount(count);
        postEvent2C(new Event2C.OnCounterUpdated(count));
    }

    public String convertNumberToEnglish(int number) {
        if (number < -3) {
            return "Less than negative three";
        } else  if (number == -3) {
            return "Negative three";
        } else  if (number == -2) {
            return "Negative two";
        } else  if (number == -1) {
            return "Negative one";
        } else if (number == 0) {
            return "Zero";
        } else if (number == 1) {
            return "One";
        } else if (number == 2) {
            return "Two";
        } else if (number == 3) {
            return "Three";
        } else {
            return "Greater than three";
        }
    }

}
