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

package com.shipdream.lib.android.mvc.view.event2v;

import android.os.Bundle;
import android.view.View;

import com.shipdream.lib.android.mvc.Forwarder;
import com.shipdream.lib.android.mvc.FragmentController;
import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.Navigator;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.TestActivity;
import com.shipdream.lib.android.mvc.view.event2v.controller.V2VHomeController;
import com.shipdream.lib.android.mvc.view.event2v.controller.V2VTestController;

import javax.inject.Inject;

public class EventBusV2VActivity extends TestActivity {

    /**
     * Map a presenter class type to fragment class type. This is used for navigation. When the
     * {@link Navigator} navigates to a presenter, in view layer, it loads the mapped fragment.
     * <p/>
     * <p>
     * To make the mapping generic, consider to use {@link Class#forName(String)}.
     * </p>
     *
     * @param controllerClass The controller class type
     * @return The class type of the {@link MvcFragment} mapped to the presenter
     */
    @Override
    protected Class<? extends MvcFragment> mapFragmentRouting(
            Class<? extends FragmentController> controllerClass) {
        return EventBusV2VFragment.class;
    }

    @Override
    protected Class<? extends DelegateFragment> getDelegateFragmentClass() {
        return HomeFragment.class;
    }

    public static class HomeFragment extends DelegateFragment<V2VHomeController> {
        @Inject
        private NavigationManager navigationManager;

        private boolean onViewStateRestoredCalled = false;

        @Override
        protected void onStartUp() {
            navigationManager.navigate(this).to(V2VTestController.class, new Forwarder().clearAll());
        }

        @Override
        protected Class<V2VHomeController> getControllerClass() {
            return V2VHomeController.class;
        }

        @Override
        public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
            super.onViewReady(view, savedInstanceState, reason);

            if (reason.isRestored()) {
                if (!onViewStateRestoredCalled) {
                    throw new IllegalStateException("When activity is restoring, onViewReady must be called after onViewStateRestored to guarantee all state of this fragment is ready to use.");
                }
            }
        }

        @Override
        public void onViewStateRestored(Bundle savedInstanceState) {
            onViewStateRestoredCalled = true;
            super.onViewStateRestored(savedInstanceState);
        }

        @Override
        public void update() {

        }
    }

}
