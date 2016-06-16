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

import android.content.Intent;

import com.shipdream.lib.android.mvc.Controller;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.Forwarder;
import com.shipdream.lib.android.mvc.MvcActivity;
import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.view.viewpager.presenter.HomeController;
import com.shipdream.lib.android.mvc.view.viewpager.presenter.SubViewController;

import javax.inject.Inject;

public class ViewPagerTestActivity extends MvcActivity {
    @Override
    protected Class<? extends MvcFragment> mapControllerFragment(Class<? extends Controller> presenterClass) {
        if (presenterClass == HomeController.class) {
            return ViewPagerHomeFragment.class;
        } else if (presenterClass == SubViewController.class) {
            return SubFragment.class;
        }
        return ViewPagerHomeFragment.class;
    }

    @Override
    protected Class<? extends DelegateFragment> getDelegateFragmentClass() {
        return HomeFragment.class;
    }

    public static class HomeFragment extends DelegateFragment {
        @Inject
        private NavigationManager navigationManager;

        @Override
        protected void onStartUp() {
            navigationManager.navigate(this).to(HomeController.class, new Forwarder().clearAll());
        }
    }

    void launchAnotherActivity() {
        Intent intent = new Intent(this, ViewPagerTestActivityTop.class);
        startActivity(intent);
    }
}
