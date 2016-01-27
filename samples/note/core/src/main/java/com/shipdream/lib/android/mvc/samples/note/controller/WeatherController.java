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

package com.shipdream.lib.android.mvc.samples.note.controller;

import com.shipdream.lib.android.mvc.controller.BaseController;
import com.shipdream.lib.android.mvc.event.BaseEventV;
import com.shipdream.lib.android.mvc.samples.note.model.WeatherModel;

public interface WeatherController extends BaseController<WeatherModel> {
    /**
     * Add a city from weather watch list which will raise {@link EventC2V.OnWeatherWatchlistChanged}.
     * @param city The city
     */
    void addCity(WeatherModel.City city);

    /**
     * Remove a city from weather watch list which will raise {@link EventC2V.OnWeatherWatchlistChanged}.
     * @param city The city
     */
    void removeCity(WeatherModel.City city);

    /**
     * Update all cities being watched. Will raise {@link EventC2V.OnWeathersUpdateBegan},
     * {@link EventC2V.OnWeathersUpdated} or {@link EventC2V.OnWeathersUpdateFailed}.
     * @param sender Who initiated the command
     */
    void updateAllCities(Object sender);

    /**
     * Events from {@link WeatherController} to views.
     */
    interface EventC2V {
        /**
         * Raised when weathers are updated successfully
         */
        class OnWeatherWatchlistChanged extends BaseEventV {
            public OnWeatherWatchlistChanged(Object sender) {
                super(sender);
            }
        }

        /**
         * Raised when it began to update weathers
         */
        class OnWeathersUpdateBegan extends BaseEventV {
            public OnWeathersUpdateBegan(Object sender) {
                super(sender);
            }
        }

        /**
         * Raised when weathers are updated successfully
         */
        class OnWeathersUpdated extends BaseEventV {
            public OnWeathersUpdated(Object sender) {
                super(sender);
            }
        }

        /**
         * Raised when failed to update weathers
         */
        class OnWeathersUpdateFailed extends BaseEventV {
            private final Exception exception;
            public OnWeathersUpdateFailed(Object sender, Exception exception) {
                super(sender);
                this.exception = exception;
            }

            public Exception getException() {
                return exception;
            }
        }
    }
}
