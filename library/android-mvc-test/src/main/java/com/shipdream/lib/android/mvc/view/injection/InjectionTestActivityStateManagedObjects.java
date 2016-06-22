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

package com.shipdream.lib.android.mvc.view.injection;

import com.shipdream.lib.android.mvc.Controller;
import com.shipdream.lib.android.mvc.Forwarder;
import com.shipdream.lib.android.mvc.FragmentController;
import com.shipdream.lib.android.mvc.MvcActivity;
import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerA;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerB;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerC;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerD;

import javax.inject.Inject;

public class InjectionTestActivityStateManagedObjects extends MvcActivity {
    @Override
    protected Class<? extends MvcFragment> mapControllerFragment(Class<? extends Controller> presenterClass) {
        if (presenterClass == ControllerA.class) {
            return FragmentA.class;
        } else if (presenterClass == ControllerB.class) {
            return FragmentB.class;
        } else if (presenterClass == ControllerC.class) {
            return FragmentC.class;
        } else if (presenterClass == ControllerD.class) {
            return FragmentD.class;
        }
        return null;
    }

    @Override
    protected Class<? extends DelegateFragment> getDelegateFragmentClass() {
        return HomeFragment.class;
    }

    public static class HomeFragment extends DelegateFragment {
        static class HomeController extends FragmentController {
            @Override
            public Class modelType() {
                return null;
            }
        }

        @Override
        protected Class<HomeFragment.HomeController> getControllerClass() {
            return HomeFragment.HomeController.class;
        }

        @Override
        public void update() {

        }

        @Inject
        private NavigationManager navigationManager;

        @Override
        protected void onStartUp() {
            navigationManager.navigate(this).to(ControllerD.class, new Forwarder().clearAll());
        }
    }

}
