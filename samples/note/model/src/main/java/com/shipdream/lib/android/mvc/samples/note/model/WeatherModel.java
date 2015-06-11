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

package com.shipdream.lib.android.mvc.samples.note.model;

import com.shipdream.lib.android.mvc.samples.note.model.dto.WeatherInfo;

import org.parceler.Parcel;

import java.util.Map;

@Parcel
public class WeatherModel {
    /**
     * Example cities can be added for this example
     */
    public enum City {
        Beijing,
        NewYork,
        London,
        Sydney;

        /**
         * @return Id of the city on <a href="http://openweathermap.org">openweathermap</a>
         */
        public int id() {
            switch (this) {
                case Beijing:
                    return 1816670;
                case NewYork:
                    return 5128638;
                case London:
                    return 2643743;
                case Sydney:
                    return 2147714;
                default:
                    return 0;
            }
        }
    }

    Map<City, WeatherInfo> weatherWatchlist;

    public Map<City, WeatherInfo> getWeatherWatchlist() {
        return weatherWatchlist;
    }

    public void setWeatherWatchlist(Map<City, WeatherInfo> weatherWatchlist) {
        this.weatherWatchlist = weatherWatchlist;
    }
}