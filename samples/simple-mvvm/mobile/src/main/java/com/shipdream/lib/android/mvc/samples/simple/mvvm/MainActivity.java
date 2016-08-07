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

package com.shipdream.lib.android.mvc.samples.simple.mvvm;

import com.shipdream.lib.android.mvc.FragmentController;
import com.shipdream.lib.android.mvc.MvcActivity;
import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.view.AppDelegateFragment;

public class MainActivity extends MvcActivity {
        //Manually map MvcFragment and FragmentController
//    @Override
//    protected Class<? extends MvcFragment> mapFragmentRouting(
//            Class<? extends Controller> controllerClass) {
//        if (controllerClass == CounterMasterController.class) {
//            return CounterMasterScreen.class;
//        } else if (controllerClass == CounterDetailController.class) {
//            return CounterDetailScreen.class;
//        } else {
//            return null;
//        }
//    }

    @Override
    protected Class<? extends MvcFragment> mapFragmentRouting(
            Class<? extends FragmentController> controllerClass) {
        String controllerPackage = controllerClass.getPackage().getName();

        //Find the classes of fragment under package .view and named in form of xxxScreen
        //For example

        //a.b.c.CounterMasterController -> a.b.c.view.CounterMasterScreen

        String viewPkgName = controllerPackage.substring(0, controllerPackage.lastIndexOf(".")) + ".view";
        String fragmentClassName = viewPkgName + "."
                + controllerClass.getSimpleName().replace("Controller", "Screen");

        try {
            return (Class<? extends MvcFragment>) Class.forName(fragmentClassName);
        } catch (ClassNotFoundException e) {
            String msg = String.format("Fragment class(%s) for controller(%s) can not be found",
                    fragmentClassName, controllerClass.getName());
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * @return The class type of the delegate fragment for the activity
     */
    @Override
    protected Class<? extends MvcActivity.DelegateFragment> getDelegateFragmentClass() {
        return AppDelegateFragment.class;
    }

}
