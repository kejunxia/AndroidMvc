package com.shipdream.lib.android.mvc.samples.simple;

import com.shipdream.lib.android.mvc.Controller;
import com.shipdream.lib.android.mvc.MvcActivity;
import com.shipdream.lib.android.mvc.MvcFragment;
import com.shipdream.lib.android.mvc.samples.simple.controller.AppDelegateController;

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
