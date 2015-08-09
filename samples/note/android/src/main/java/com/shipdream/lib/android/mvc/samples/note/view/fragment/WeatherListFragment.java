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

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.shipdream.lib.android.mvc.samples.note.R;
import com.shipdream.lib.android.mvc.samples.note.controller.WeatherController;
import com.shipdream.lib.android.mvc.samples.note.model.WeatherModel;
import com.shipdream.lib.android.mvc.samples.note.model.dto.WeatherInfo;

import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

public class WeatherListFragment extends BaseFragment {
    private Button buttonAddWeather;
    private Button buttonRefresh;
    private View emptyView;
    private RecyclerView listView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressDialog progressDialog;

    @Inject
    private WeatherController weatherController;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_weather_list;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        emptyView = view.findViewById(R.id.fragment_weather_list_listViewEmpty);

        buttonRefresh = (Button) view.findViewById(R.id.fragment_weather_list_buttonRefresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                weatherController.updateAllCities(view);
            }
        });

        buttonAddWeather = (Button) view.findViewById(R.id.fragment_weather_list_buttonAdd);
        buttonAddWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final WeatherCityListFragment cityDialog = new WeatherCityListFragment();
                cityDialog.show(getFragmentManager(), "WeatherCityDialog");
            }
        });

        listView = (RecyclerView) view.findViewById(R.id.fragment_weather_list_listView);
        layoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(layoutManager);

        updateList();

        //Automatically update weathers of all cities on first creation.
        if (reason == Reason.FIRST_TIME) {
            weatherController.updateAllCities(this);
        }
    }

    public void onEvent(WeatherCityListFragment.EventV2V.OnOkButtonClicked event) {
        WeatherCityListFragment dialog = (WeatherCityListFragment) getFragmentManager().findFragmentByTag("WeatherCityDialog");
        if(dialog != null && dialog.getDialog() != null) {
            dialog.getDialog().dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getToolBar().setTitle("Weathers");
    }

    public void onEvent(WeatherController.EventC2V.OnWeathersUpdateBegan event) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Update weathers");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    public void onEvent(WeatherController.EventC2V.OnWeathersUpdated event) {
        updateList();
        progressDialog.dismiss();
    }

    public void onEvent(WeatherController.EventC2V.OnWeathersUpdateFailed event) {
        progressDialog.dismiss();
        Toast.makeText(getActivity(), "Error:" + event.getException().getMessage(),
                Toast.LENGTH_SHORT).show();
    }

    private void updateList() {
        adapter = new WeatherAdapter(this);
        listView.setAdapter(adapter);

        if (adapter.getItemCount() > 0) {
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
    }

    static class WeatherAdapter extends RecyclerView.Adapter<WeatherItemHolder> {
        private WeatherListFragment weatherListFragment;

        WeatherAdapter(WeatherListFragment weatherListFragment) {
            this.weatherListFragment = weatherListFragment;
        }

        @Override
        public WeatherItemHolder onCreateViewHolder(ViewGroup parent, int i) {
            View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_weather_list_item, parent, false);
            WeatherItemHolder holder = new WeatherItemHolder(convertView);
            holder.root = convertView.findViewById(R.id.weather_item_root);
            holder.title = (TextView) convertView.findViewById(R.id.fragment_weather_list_item_title);
            holder.weather = (TextView) convertView.findViewById(R.id.fragment_weather_list_item_weather);
            holder.temp = (TextView) convertView.findViewById(R.id.fragment_weather_list_item_temp);
            return holder;
        }

        @Override
        public void onBindViewHolder(WeatherItemHolder holder, final int position) {
            Map<WeatherModel.City, WeatherInfo> cities = weatherListFragment.weatherController.getModel().getWeatherWatchlist();
            Iterator<WeatherModel.City> iterator = cities.keySet().iterator();
            WeatherInfo weatherInfo = null;
            if (iterator.hasNext()) {
                weatherInfo = cities.get(iterator.next());
            }

            for (int i = 0; i < position && iterator.hasNext(); i++) {
                weatherInfo = cities.get(iterator.next());
            }

            if (weatherInfo != null) {
                holder.title.setText("City: " + weatherInfo.getName());
                if (weatherInfo.getWeather().size() > 0) {
                    holder.weather.setText("Weather: " + weatherInfo.getWeather().get(0).getMain());
                }
                holder.temp.setText("Temperature: " + weatherInfo.getMain().getTemp());
            }
        }

        @Override
        public int getItemCount() {
            Map<WeatherModel.City, WeatherInfo> list = weatherListFragment.weatherController.getModel().getWeatherWatchlist();
            return list.size();
        }
    }

    static class WeatherItemHolder extends RecyclerView.ViewHolder {
        View root;
        TextView title;
        TextView weather;
        TextView temp;

        public WeatherItemHolder(View itemView) {
            super(itemView);
        }
    }
}
