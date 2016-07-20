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

package com.shipdream.lib.android.mvc.samples.simple.mvp;

import android.app.Application;
import android.content.Context;

import com.shipdream.lib.android.mvc.Mvc;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Mvc.graph().getRootComponent().register(new Object(){
                @Provides
                @AppContext
                public Context context() {
                    return getApplicationContext();
                }
            });
        } catch (ProvideException e) {
            e.printStackTrace();
        } catch (ProviderConflictException e) {
            e.printStackTrace();
        }
    }
}
