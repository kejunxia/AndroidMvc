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

package com.shipdream.lib.android.mvc.samples.simple.mvp;

import com.shipdream.lib.android.mvc.Controller;
import com.shipdream.lib.android.mvc.MvcActivity;
import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.samples.simple.mvp.controller.AppDelegateController;

public class MainActivity extends MvcActivity {
    @Override
    protected Class<? extends MvcFragment> mapControllerFragment(
            Class<? extends Controller> presenterClass) {
        String presenterPkgName = presenterClass.getPackage().getName();
        String viewPkgName = presenterPkgName.substring(0, presenterPkgName.lastIndexOf(".")) + ".view";
        String fragmentClassName = viewPkgName + "."
                + presenterClass.getSimpleName().replace("Controller", "Screen");
        try {
            return (Class<? extends MvcFragment>) Class.forName(fragmentClassName);
        } catch (ClassNotFoundException e) {
            String msg = String.format("Fragment class(%s) for controller(%s) can not be found",
                    fragmentClassName, presenterClass.getName());
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * @return The class type of the delegate fragment for the activity
     */
    @Override
    protected Class<? extends MvcActivity.DelegateFragment> getDelegateFragmentClass() {
        return ContainerFragment.class;
    }

    /**
     * Container fragment extends DelegateFragment would be the root container fragments to swap
     * full screen fragments inside it on navigation.
     */
    public static class ContainerFragment extends MvcActivity.DelegateFragment<AppDelegateController> {
        /**
         * What to do when app starts for the first time
         */
        @Override
        protected void onStartUp() {
            controller.startApp(this);
        }

        @Override
        protected Class getControllerClass() {
            return AppDelegateController.class;
        }

        @Override
        public void update() {
        }
    }
}
