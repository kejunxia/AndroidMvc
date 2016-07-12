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

package com.shipdream.lib.android.mvc.view.injection.service.internal;

import com.shipdream.lib.android.mvc.Bean;
import com.shipdream.lib.android.mvc.view.injection.service.StorageService;

public class StorageServiceImpl extends Bean<StorageService.Storage> implements StorageService{
    @Override
    public Class<StorageService.Storage> modelType() {
        return StorageService.Storage.class;
    }

    @Override
    public String getContent() {
        return getModel().getContent();
    }

    @Override
    public void setContent(String content) {
        getModel().setContent(content);
    }

}
