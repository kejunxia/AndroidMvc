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

package com.shipdream.lib.android.mvc.samples.note.service.http.internal;

import com.google.gson.Gson;
import com.shipdream.lib.android.mvc.samples.note.model.dto.WeatherListResponse;
import com.shipdream.lib.android.mvc.samples.note.service.http.WeatherService;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class WeatherServiceImpl implements WeatherService{
    private final static String APPKEY = "e28e969eb96b3d4929abd12d42a9513d";
    private OkHttpClient httpClient;
    private Gson gson;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public WeatherServiceImpl() {
        httpClient = new OkHttpClient();
        gson = new Gson();
    }

    @Override
    public WeatherListResponse getWeathers(List<Integer> ids) throws IOException {
        String idsStr = "";
        for (Integer id : ids) {
            if (!idsStr.isEmpty()) {
                idsStr += ", ";
            }
            idsStr += String.valueOf(id);
        }
        String url = String.format("http://api.openweathermap.org/data/2.5/group?id=%s&appId=%s",
                URLEncoder.encode(idsStr, "UTF-8"), APPKEY);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response resp = httpClient.newCall(request).execute();
        String responseStr = resp.body().string();
        logger.debug("Weather Service Response: {}", responseStr);
        return gson.fromJson(responseStr, WeatherListResponse.class);
    }
}
