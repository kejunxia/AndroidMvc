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

package com.shipdream.lib.android.mvp.view.injection;

import android.os.Bundle;
import android.view.View;

import com.shipdream.lib.android.mvp.manager.NavigationManager;
import com.shipdream.lib.android.mvp.manager.internal.Forwarder;
import com.shipdream.lib.android.mvp.view.MvpActivity;
import com.shipdream.lib.android.mvp.view.MvpFragment;
import com.shipdream.lib.android.mvp.view.injection.controller.ControllerA;
import com.shipdream.lib.android.mvp.view.nav.MvpTestActivityNavigation;

import javax.inject.Inject;

public class InjectionTestActivityTestRootFragment extends MvpActivity {
    @Override
    protected Class<? extends MvpFragment> mapNavigationFragment(String locationId) {
        switch (locationId) {
            case MvpTestActivityNavigation.Loc.A:
                return FragmentA.class;
            default:
                return null;
        }
    }

    @Override
    protected Class<? extends DelegateFragment> getDelegateFragmentClass() {
        return HomeFragment.class;
    }

    public static class HomeFragment extends DelegateFragment {
        @Inject
        private NavigationManager navigationManager;

        @Inject
        private ControllerA controllerA;

        @Override
        protected void onStartUp() {
            navigationManager.navigate(this).to(MvpTestActivityNavigation.Loc.A, new Forwarder().clearAll());
        }

        @Override
        public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
            super.onViewReady(view, savedInstanceState, reason);

            if (savedInstanceState != null) {
                controllerA.addTag("OK");
            }
        }

        @Override
        public void onViewStateRestored(Bundle savedInstanceState) {
            super.onViewStateRestored(savedInstanceState);

            if (savedInstanceState != null) {
                System.out.print("");
            }
        }
    }

}
