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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class WeatherServiceImpl implements WeatherService{
    private HttpClient httpClient;
    private Gson gson;

    public WeatherServiceImpl() {
        httpClient = new DefaultHttpClient();
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
        String url = String.format("http://api.openweathermap.org/data/2.5/group?id=%s&units=metric",
                URLEncoder.encode(idsStr, "UTF-8"));
        HttpGet get = new HttpGet(url);
        HttpResponse resp = httpClient.execute(get);
        String responseStr = EntityUtils.toString(resp.getEntity());
        return gson.fromJson(responseStr, WeatherListResponse.class);
    }
}
