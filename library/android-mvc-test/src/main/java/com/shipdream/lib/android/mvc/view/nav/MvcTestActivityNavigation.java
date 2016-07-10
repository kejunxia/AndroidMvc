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

package com.shipdream.lib.android.mvc.view.nav;

import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.shipdream.lib.android.mvc.Controller;
import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.Navigator;
import com.shipdream.lib.android.mvc.view.TestActivity;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerA;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerB;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerC;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerD;

import javax.inject.Inject;

public class MvcTestActivityNavigation extends TestActivity {
    int fragAInjectCount = 0;
    int fragBInjectCount = 0;
    int fragCInjectCount = 0;
    int fragDInjectCount = 0;
    int fragAReleaseCount = 0;
    int fragBReleaseCount = 0;
    int fragCReleaseCount = 0;
    int fragDReleaseCount = 0;

    /**
     * Map a presenter class type to fragment class type. This is used for navigation. When the
     * {@link Navigator} navigates to a presenter, in view layer, it loads the mapped fragment.
     * <p/>
     * <p>
     * To make the mapping generic, consider to use {@link Class#forName(String)}.
     * </p>
     *
     * @param presenterClass The presenter class type
     * @return The class type of the {@link MvcFragment} mapped to the presenter
     */
    @Override
    protected Class<? extends MvcFragment> mapControllerFragment(Class<? extends Controller> presenterClass) {
        if (presenterClass == ControllerA.class) {
            return NavFragmentA.class;
        } else if (presenterClass == ControllerB.class) {
            return NavFragmentB.class;
        } else if (presenterClass == ControllerC.class) {
            return NavFragmentC.class;
        } else if (presenterClass == ControllerD.class) {
            return NavFragmentD.class;
        } else if (presenterClass == ControllerE.class) {
            return NavFragmentE.class;
        } else if (presenterClass == ControllerF.class) {
            return NavFragmentF.class;
        } else if (presenterClass == ControllerG.class) {
            return NavFragmentG.class;
        }
        return null;
    }

    @Override
    protected Class<? extends DelegateFragment> getDelegateFragmentClass() {
        return HomeFragment.class;
    }

    FragmentManager getRootFragmentManager() {
        return getSupportFragmentManager().getFragments().get(0).getChildFragmentManager();
    }

    public static class HomeFragment extends DelegateFragment {
        @Inject
        private NavigationManager navigationManager;

        @Override
        protected void onStartUp() {
            Log.d("MvcTesting", "navigate");
//            navigationManager.navigate(this).to(ControllerA.class);
        }

        @Override
        protected Class getControllerClass() {
            return null;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Override
        public void update() {

        }
    }

}
