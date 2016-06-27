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

package com.shipdream.lib.android.mvc.view.injection.controller.internal;

import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerD;
import com.shipdream.lib.android.mvc.view.injection.manager.AccountManager;

import javax.inject.Inject;

public class ControllerDImpl extends BaseControllerImpl<ControllerD.Model>
        implements ControllerD {
    @Inject
    private AccountManager accountManager;

    @Override
    public Class<ControllerD.Model> modelType() {
        return ControllerD.Model.class;
    }

    @Override
    public AccountManager getAccountManager() {
        return accountManager;
    }

    @Override
    public void setUserId(long userId) {
        accountManager.setUserId(userId);
    }

    @Override
    public void setStorage(String content) {
        accountManager.setContent(content);
    }

    @Override
    public void bindModel(Object sender, Model model) {

    }
}
