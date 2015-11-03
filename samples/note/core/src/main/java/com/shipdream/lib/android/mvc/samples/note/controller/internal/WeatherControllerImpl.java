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

package com.shipdream.lib.android.mvc.samples.note.controller.internal;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shipdream.lib.android.mvc.controller.internal.AsyncExceptionHandler;
import com.shipdream.lib.android.mvc.controller.internal.AsyncTask;
import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.samples.note.controller.WeatherController;
import com.shipdream.lib.android.mvc.samples.note.model.WeatherModel;
import com.shipdream.lib.android.mvc.samples.note.model.dto.WeatherInfo;
import com.shipdream.lib.android.mvc.samples.note.service.android.PreferenceService;
import com.shipdream.lib.android.mvc.samples.note.service.http.WeatherService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class WeatherControllerImpl extends BaseControllerImpl <WeatherModel> implements
        WeatherController{
    static final String PREF_KEY_WEATHER_CITIES = "PrefKey:Weather:Cities";
    private Gson gson = new Gson();

    @Inject
    private WeatherService weatherService;

    @Inject
    private PreferenceService preferenceService;

    @Override
    protected Class<WeatherModel> getModelClassType() {
        return WeatherModel.class;
    }

    @Override
    public void onConstruct() {
        super.onConstruct();
        Map<WeatherModel.City, WeatherInfo> weatherWatchlist;
        String savedCities = preferenceService.getString(PREF_KEY_WEATHER_CITIES, null);
        if(savedCities != null) {
            Type typeOfHashMap = new TypeToken<Map<WeatherModel.City, WeatherInfo>>() { }.getType();
            weatherWatchlist = gson.fromJson(savedCities, typeOfHashMap);
        } else {
            weatherWatchlist = new LinkedHashMap<>();
        }
        getModel().setWeatherWatchlist(weatherWatchlist);
    }

    @Override
    public void addCity(WeatherModel.City city) {
        getModel().getWeatherWatchlist().put(city, null);
    }

    @Override
    public void removeCity(WeatherModel.City city) {
        getModel().getWeatherWatchlist().remove(city);
    }

    @Override
    public void updateAllCities(final Object sender) {
        if(getModel().getWeatherWatchlist().size() == 0) {
            String cities = gson.toJson(getModel().getWeatherWatchlist());
            preferenceService.edit().putString(PREF_KEY_WEATHER_CITIES, cities).apply();
            postC2VEvent(new EventC2V.OnWeathersUpdated(sender));
        } else {
            postC2VEvent(new EventC2V.OnWeathersUpdateBegan(sender));

            runAsyncTask(sender, new AsyncTask() {
                @Override
                public void execute() throws Exception {
                    List<Integer> ids = new ArrayList<>();
                    for(WeatherModel.City city : getModel().getWeatherWatchlist().keySet()) {
                        ids.add(city.id());
                    }
                    for (WeatherInfo weatherInfo : weatherService.getWeathers(ids).getList()) {
                        getModel().getWeatherWatchlist().put(findCityById(weatherInfo.getId()), weatherInfo);
                    }

                    String cities = gson.toJson(getModel().getWeatherWatchlist());
                    preferenceService.edit().putString(PREF_KEY_WEATHER_CITIES, cities).apply();
                    //Weather updated, post successful event
                    postC2VEvent(new EventC2V.OnWeathersUpdated(sender));
                }
            }, new AsyncExceptionHandler() {
                @Override
                public void handleException(Exception exception) {
                    //Weather failed, post error event
                    postC2VEvent(new EventC2V.OnWeathersUpdateFailed(sender, exception));
                }
            });
        }
    }

    private WeatherModel.City findCityById(int id) {
        for(int i = 0; i < WeatherModel.City.values().length; i++) {
            if (WeatherModel.City.values()[i].id() == id) {
                return WeatherModel.City.values()[i];
            }
        }
        return null;
    }
}
