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

package com.shipdream.lib.android.mvc.samples.note.view.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.shipdream.lib.android.mvc.event.BaseEventV2V;
import com.shipdream.lib.android.mvc.samples.note.R;
import com.shipdream.lib.android.mvc.samples.note.controller.WeatherController;
import com.shipdream.lib.android.mvc.samples.note.model.WeatherModel;
import com.shipdream.lib.android.mvc.samples.note.model.dto.WeatherInfo;
import com.shipdream.lib.android.mvc.view.MvcDialogFragment;

import java.util.Map;

import javax.inject.Inject;

public class WeatherCityListFragment extends MvcDialogFragment {
    private CheckBox checkBoxBeijing;
    private CheckBox checkBoxNewYork;
    private CheckBox checkBoxLondon;
    private CheckBox checkBoxSydney;
    private View buttonOK;

    @Inject
    private WeatherController weatherController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather_city_list, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkBoxBeijing = (CheckBox) view.findViewById(R.id.fragment_weather_city_check_beijing);
        checkBoxNewYork = (CheckBox) view.findViewById(R.id.fragment_weather_city_check_nyc);
        checkBoxLondon = (CheckBox) view.findViewById(R.id.fragment_weather_city_check_london);
        checkBoxSydney = (CheckBox) view.findViewById(R.id.fragment_weather_city_check_sydney);
        buttonOK = view.findViewById(R.id.buttonOk);

        checkBoxBeijing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    weatherController.addCity(WeatherModel.City.Beijing);
                } else {
                    weatherController.removeCity(WeatherModel.City.Beijing);
                }
            }
        });

        checkBoxNewYork.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    weatherController.addCity(WeatherModel.City.NewYork);
                } else {
                    weatherController.removeCity(WeatherModel.City.NewYork);
                }
            }
        });

        checkBoxLondon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    weatherController.addCity(WeatherModel.City.London);
                } else {
                    weatherController.removeCity(WeatherModel.City.London);
                }
            }
        });

        checkBoxSydney.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    weatherController.addCity(WeatherModel.City.Sydney);
                } else {
                    weatherController.removeCity(WeatherModel.City.Sydney);
                }
            }
        });

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postEventV2V(new EventV2V.OnOkButtonClicked(v));
            }
        });

        updateList();
    }

    /**
     * Called when all saved state has been restored into the view hierarchy
     * of the fragment.  This can be used to do initialization based on saved
     * state that you are letting the view hierarchy track itself, such as
     * whether check box widgets are currently checked.  This is called
     * after {@link #onActivityCreated(Bundle)} and before
     * {@link #onStart()}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        weatherController.updateAllCities(dialog);
    }

    private void updateList() {
        Map<WeatherModel.City, WeatherInfo> cities = weatherController.getModel().getWeatherWatchlist();
        checkBoxBeijing.setChecked(cities.containsKey(WeatherModel.City.Beijing));
        checkBoxNewYork.setChecked(cities.containsKey(WeatherModel.City.NewYork));
        checkBoxLondon.setChecked(cities.containsKey(WeatherModel.City.London));
        checkBoxSydney.setChecked(cities.containsKey(WeatherModel.City.Sydney));
    }

    public static class EventV2V {
        public static class OnOkButtonClicked extends BaseEventV2V {
            protected OnOkButtonClicked(Object sender) {
                super(sender);
            }
        }
    }
}
