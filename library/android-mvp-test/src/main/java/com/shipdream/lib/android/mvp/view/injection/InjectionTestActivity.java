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

import com.shipdream.lib.android.mvp.manager.NavigationManager;
import com.shipdream.lib.android.mvp.manager.internal.Forwarder;
import com.shipdream.lib.android.mvp.view.MvpActivity;
import com.shipdream.lib.android.mvp.view.MvpFragment;
import com.shipdream.lib.android.mvp.view.nav.MvpTestActivityNavigation;

import javax.inject.Inject;

public class InjectionTestActivity extends MvpActivity {
    @Override
    protected Class<? extends MvpFragment> mapNavigationFragment(String locationId) {
        switch (locationId) {
            case MvpTestActivityNavigation.Loc.A:
                return FragmentA.class;
            case MvpTestActivityNavigation.Loc.B:
                return FragmentB.class;
            case MvpTestActivityNavigation.Loc.C:
                return FragmentC.class;
            case MvpTestActivityNavigation.Loc.D:
                return FragmentD.class;
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

        @Override
        protected void onStartUp() {
            navigationManager.navigate(this).to(MvpTestActivityNavigation.Loc.A, new Forwarder().clearAll());
        }
    }

}
