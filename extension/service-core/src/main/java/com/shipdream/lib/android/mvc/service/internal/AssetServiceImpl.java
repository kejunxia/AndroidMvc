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

package com.shipdream.lib.android.mvc.service.internal;

import android.content.Context;
import com.shipdream.lib.android.mvc.service.AssetService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AssetServiceImpl implements AssetService {
    private Context context;

    public AssetServiceImpl(Context context){
        this.context = context;
    }

    @Override
    public InputStream getAsset(String path) throws IOException {
        return context.getAssets().open(path);
    }

    @Override
    public String getStringFromAssets(String assetPath) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = context.getAssets().open(assetPath);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);
            }
        } finally {
            if(bufferedReader != null){
                bufferedReader.close();
            }
        }

        return sb.toString();
    }
}
