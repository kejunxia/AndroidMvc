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

package com.shipdream.lib.android.mvc.samples.simple.view;

import com.shipdream.lib.android.mvc.samples.simple.controller.AppController;
import com.shipdream.lib.android.mvc.view.MvcActivity;
import com.shipdream.lib.android.mvc.view.MvcFragment;

import javax.inject.Inject;

/**
 * Single activity for the app
 */
public class MainActivity extends MvcActivity {
    /**
     * Define how to map navigation location id to full screen fragments
     * @param locationId The location id in string
     * @return The class of the fragment representing the navigation locations
     */
    @Override
    protected Class<? extends MvcFragment> mapNavigationFragment(String locationId) {
        switch (locationId) {
            case "LocationA":
                return FragmentA.class;
            case "LocationB":
                return FragmentB.class;
            default:
                return null;
        }
    }

    /**
     * @return The class type of the delegate fragment for the activity
     */
    @Override
    protected Class<? extends DelegateFragment> getDelegateFragmentClass() {
        return ContainerFragment.class;
    }

    /**
     * Container fragment extends DelegateFragment would be the root container fragments to swap
     * full screen fragments inside it on navigation.
     */
    public static class ContainerFragment extends DelegateFragment {
        @Inject
        private AppController appController;

        /**
         * What to do when app starts for the first time
         */
        @Override
        protected void onStartUp() {
            appController.startApp(this);
        }
    }
}
