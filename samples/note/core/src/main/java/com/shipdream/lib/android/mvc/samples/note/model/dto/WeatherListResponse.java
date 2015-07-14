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

package com.shipdream.lib.android.mvc.samples.note.model.dto;

import java.util.List;

/**
 * The response from api.openweathermap.org
 */
public class WeatherListResponse {
    /**
     * Count
     */
    private int cnt;
    /**
     * List
     */
    private List<WeatherInfo> list;

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public List<WeatherInfo> getList() {
        return list;
    }

    public void setList(List<WeatherInfo> list) {
        this.list = list;
    }
}
