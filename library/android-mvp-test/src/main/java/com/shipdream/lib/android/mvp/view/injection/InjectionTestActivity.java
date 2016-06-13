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

import com.shipdream.lib.android.mvp.Presenter;
import com.shipdream.lib.android.mvp.Forwarder;
import com.shipdream.lib.android.mvp.MvpActivity;
import com.shipdream.lib.android.mvp.MvpFragment;
import com.shipdream.lib.android.mvp.NavigationManager;
import com.shipdream.lib.android.mvp.view.injection.presenter.PresenterA;
import com.shipdream.lib.android.mvp.view.injection.presenter.PresenterB;
import com.shipdream.lib.android.mvp.view.injection.presenter.PresenterC;
import com.shipdream.lib.android.mvp.view.injection.presenter.PresenterD;

import javax.inject.Inject;

public class InjectionTestActivity extends MvpActivity {
    @Override
    protected Class<? extends MvpFragment> mapPresenterFragment(Class<? extends Presenter> presenterClass) {
        if (presenterClass == PresenterA.class) {
            return FragmentA.class;
        } else if (presenterClass == PresenterB.class) {
            return FragmentB.class;
        } else if (presenterClass == PresenterC.class) {
            return FragmentC.class;
        } else if (presenterClass == PresenterD.class) {
            return FragmentD.class;
        }
        return null;
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
            navigationManager.navigate(this).to(PresenterA.class, new Forwarder().clearAll());
        }
    }

}
