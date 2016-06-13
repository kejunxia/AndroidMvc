package com.shipdream.lib.android.mvc.samples.simple.mvp;

import com.shipdream.lib.android.mvc.samples.simple.mvp.presenter.EntryPresenter;
import com.shipdream.lib.android.mvp.Presenter;
import com.shipdream.lib.android.mvp.MvpActivity;
import com.shipdream.lib.android.mvp.MvpFragment;

import javax.inject.Inject;

public class MainActivity extends MvpActivity {

    @Override
    protected Class<? extends MvpFragment> mapPresenterFragment(
            Class<? extends Presenter> presenterClass) {
        String presenterPkgName = presenterClass.getPackage().getName();
        String viewPkgName = presenterPkgName.substring(0, presenterPkgName.lastIndexOf(".")) + ".view";
        String fragmentClassName = viewPkgName + "."
                + presenterClass.getSimpleName().replace("Presenter", "View");
        try {
            return (Class<? extends MvpFragment>) Class.forName(fragmentClassName);
        } catch (ClassNotFoundException e) {
            String msg = String.format("Fragment class(%s) for presenter(%s) can not be found",
                    fragmentClassName, presenterClass.getName());
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * @return The class type of the delegate fragment for the activity
     */
    @Override
    protected Class<? extends MvpActivity.DelegateFragment> getDelegateFragmentClass() {
        return ContainerFragment.class;
    }

    /**
     * Container fragment extends DelegateFragment would be the root container fragments to swap
     * full screen fragments inside it on navigation.
     */
    public static class ContainerFragment extends MvpActivity.DelegateFragment {
        @Inject
        private EntryPresenter presenter;

        /**
         * What to do when app starts for the first time
         */
        @Override
        protected void onStartUp() {
            presenter.startApp(this);
        }
    }
}
