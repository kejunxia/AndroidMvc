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

import com.shipdream.lib.android.mvc.Forwarder;
import com.shipdream.lib.android.mvc.FragmentController;
import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.TestActivity;
import com.shipdream.lib.android.mvc.view.viewpager.controller.FirstFragmentController;
import com.shipdream.lib.android.mvc.view.viewpager.controller.HomeController;
import com.shipdream.lib.android.mvc.view.viewpager.controller.SecondFragmentController;

import javax.inject.Inject;

public class ViewPagerTestActivity extends TestActivity {
    @Override
    protected Class<? extends MvcFragment> mapFragmentRouting(Class<? extends FragmentController> controllerClass) {
        if (controllerClass == FirstFragmentController.class) {
            return ViewPagerHomeFragment.class;
        } else if (controllerClass == SecondFragmentController.class) {
            return SecondFragment.class;
        }
        return ViewPagerHomeFragment.class;
    }

    @Override
    protected Class<? extends DelegateFragment> getDelegateFragmentClass() {
        return HomeFragment.class;
    }

    public static class HomeFragment extends DelegateFragment<HomeController> {
        @Inject
        private NavigationManager navigationManager;

        @Override
        protected void onStartUp() {
            navigationManager.navigate(this).to(FirstFragmentController.class, new Forwarder().clearAll());
        }

        @Override
        protected Class<HomeController> getControllerClass() {
            return HomeController.class;
        }

        @Override
        public void update() {

        }
    }

    Intent launchAnotherActivity() {
        Intent intent = new Intent(this, ViewPagerTestActivityTop.class);
        return intent;
    }
}
