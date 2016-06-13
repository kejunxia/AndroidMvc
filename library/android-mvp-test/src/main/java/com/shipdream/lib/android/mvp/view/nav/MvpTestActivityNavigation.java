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

package com.shipdream.lib.android.mvp.view.nav;

import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.shipdream.lib.android.mvp.AbstractPresenter;
import com.shipdream.lib.android.mvp.MvpActivity;
import com.shipdream.lib.android.mvp.MvpFragment;
import com.shipdream.lib.android.mvp.NavigationManager;
import com.shipdream.lib.android.mvp.Navigator;
import com.shipdream.lib.android.mvp.view.injection.presenter.PresenterA;
import com.shipdream.lib.android.mvp.view.injection.presenter.PresenterB;
import com.shipdream.lib.android.mvp.view.injection.presenter.PresenterC;
import com.shipdream.lib.android.mvp.view.injection.presenter.PresenterD;

import javax.inject.Inject;

public class MvpTestActivityNavigation extends MvpActivity {
    /**
     * Map a presenter class type to fragment class type. This is used for navigation. When the
     * {@link Navigator} navigates to a presenter, in view layer, it loads the mapped fragment.
     * <p/>
     * <p>
     * To make the mapping generic, consider to use {@link Class#forName(String)}.
     * </p>
     *
     * @param presenterClass The presenter class type
     * @return The class type of the {@link MvpFragment} mapped to the presenter
     */
    @Override
    protected Class<? extends MvpFragment> mapPresenterFragment(Class<? extends AbstractPresenter> presenterClass) {
        if (presenterClass == PresenterA.class) {
            return NavFragmentA.class;
        } else if (presenterClass == PresenterB.class) {
            return NavFragmentB.class;
        } else if (presenterClass == PresenterC.class) {
            return NavFragmentC.class;
        } else if (presenterClass == PresenterD.class) {
            return NavFragmentD.class;
        } else if (presenterClass == PresenterE.class) {
            return NavFragmentE.class;
        } else if (presenterClass == PresenterF.class) {
            return NavFragmentF.class;
        } else if (presenterClass == PresenterG.class) {
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
            navigationManager.navigate(this).to(PresenterA.class);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }
    }

}
