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

package com.shipdream.lib.android.mvc.view.injection.controller;

import com.shipdream.lib.android.mvc.FragmentController;
import com.shipdream.lib.android.mvc.UiView;
import com.shipdream.lib.android.mvc.view.injection.manager.AccountManager;

import java.util.List;

import javax.inject.Inject;

public class ControllerD extends FragmentController<ControllerD.Model, UiView> {
    public static class Model {
        private List<String> tags;

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags){
            this.tags = tags;
        }
    }

    @Inject
    private AccountManager accountManager;

    @Override
    public Class<ControllerD.Model> modelType() {
        return Model.class;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setUserId(long userId) {
        accountManager.setUserId(userId);
    }

    public void setStorage(String content) {
        accountManager.setContent(content);
    }
}
