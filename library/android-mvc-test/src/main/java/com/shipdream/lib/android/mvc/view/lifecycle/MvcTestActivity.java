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

package com.shipdream.lib.android.mvc.view.lifecycle;

import com.shipdream.lib.android.mvc.view.MvcActivity;
import com.shipdream.lib.android.mvc.view.MvcFragment;

public class MvcTestActivity extends MvcActivity {

    @Override
    protected Class<? extends MvcFragment> mapNavigationFragment(String locationId) {
        return MvcTestFragment.class;
    }

    @Override
    protected Class<? extends DelegateFragment> getDelegateFragmentClass() {
        return HomeFragment.class;
    }

    public static class HomeFragment extends DelegateFragment {
        @Override
        protected void onStartUp() {
            getNavigationController().navigate(this).to("TestFragment", null);
        }
    }

}
