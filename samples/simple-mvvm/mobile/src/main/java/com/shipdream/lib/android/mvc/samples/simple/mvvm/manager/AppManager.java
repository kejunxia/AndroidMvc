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

package com.shipdream.lib.android.mvc.samples.simple.mvvm.manager;

import com.shipdream.lib.android.mvc.Manager;

public class AppManager extends Manager<AppManager.Model>{
    public interface Event {
        class OnTitleUpdated{
            private final String title;

            public OnTitleUpdated(String title) {
                this.title = title;
            }

            public String getTitle() {
                return title;
            }
        }
    }

    public static class Model {
        private String title;

        public String getTitle() {
            return title;
        }
    }

    @Override
    public Class<Model> modelType() {
        return Model.class;
    }

    public void setTitle(String title) {
        getModel().title = title;
        postEvent2C(new Event.OnTitleUpdated(title));
    }
}
