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

package com.shipdream.lib.android.mvc.view.viewpager;

import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.view.test.R;
import com.shipdream.lib.android.mvc.view.viewpager.controller.SecondFragmentController;

public class SecondFragment extends MvcFragment<SecondFragmentController> {
    @Override
    protected Class<SecondFragmentController> getControllerClass() {
        return SecondFragmentController.class;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_view_pager_sub;
    }

    @Override
    public void update() {

    }
}
