/*
 * Copyright 2015 Kejun Xia
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

package com.shipdream.lib.android.mvc.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.shipdream.lib.android.mvc.NavLocation;
import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.poke.util.ReflectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public abstract class MvcActivity extends AppCompatActivity {
    private DelegateFragment mDelegateFragment;

    String getDelegateFragmentTag() {
        return AndroidMvc.FRAGMENT_TAG_PREFIX + getDelegateFragmentClass().getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mvc_activity);
        mDelegateFragment = (DelegateFragment) getSupportFragmentManager().findFragmentByTag(
                getDelegateFragmentTag());

        if (mDelegateFragment == null) {
            //Brand new container fragment
            try {
                mDelegateFragment = (DelegateFragment) new ReflectUtils.newObjectByType(
                        getDelegateFragmentClass()).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate delegate fragment.", e);
            }
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.replace(R.id.android_mvc_activity_root, mDelegateFragment, getDelegateFragmentTag());
            trans.commit();
        }
    }

    void performSuperBackKeyPressed () {
        super.onBackPressed();
    }

    /**
     * Provides class types of fragments to present navigation location of given location id.
     *
     * @param locationId The location id in string
     * @return The class type of the {@link MvcFragment}
     */
    protected abstract Class<? extends MvcFragment> mapNavigationFragment(String locationId);

    /**
     * Provides the class type of the delegate fragment which is the root fragment holding fragments
     * during navigation. By default, {@link DelegateFragment}.class is provided. Overrides this
     * method to provide custom delegate fragment.
     *
     * @return The class type of the delegate fragment
     */
    protected abstract Class<? extends DelegateFragment> getDelegateFragmentClass();

    @Override
    public void onBackPressed() {
        mDelegateFragment.onBackButtonPressed();
    }

    /**
     * Add callback so that onViewReady will be delay to call after all instance state are restored
     * @param runnable The delayed onViewReady callbacks
     */
    void addPendingOnViewReadyActions(Runnable runnable) {
        mDelegateFragment.mPendingOnViewReadyActions.add(runnable);
    }

    /**
     * This fragment is the container fragment as a root of the activity. When navigating by
     * {@link NavigationController}, new fragments will be created and replace the root view of this
     * fragment or pop out the stacked history fragments. {@link NavigationController} can be simply
     * injected into any fragments extending {@link MvcFragment} by fields annotated by @Inject.
     */
    public static abstract class DelegateFragment extends MvcFragment {
        private static final String MVC_STATE_BUNDLE_KEY = AndroidMvc.MVC_SATE_PREFIX + "RootBundle";
        private Logger mLogger = LoggerFactory.getLogger(getClass());
        //Mimic the behavior of Android framework to manage the state save state
        private boolean mStateSaved = false;
        private List<Runnable> mPendingNavActions = new ArrayList<>();
        private List<Runnable> mPendingOnViewReadyActions = new ArrayList<>();

        @Inject
        private NavigationController mNavigationController;

        /**
         * Hack to fix this <a href='https://code.google.com/p/android/issues/detail?id=74222'>bug</a>
         * with this <a href='http://ideaventure.blogspot.com.au/2014/10/nested-retained-fragment-lost-state.html'>solution</a>
         * FIXME: ChildFragmentManager hack - remove this method when the bug is fixed in future android support library
         */
        private FragmentManager mRetainedChildFragmentManager;

        /**
         * Get child fragment manager with android support lib rev20/rev21 which has a the
         * <a href='https://code.google.com/p/android/issues/detail?id=74222'>bug</a> to retain child
         * fragment manager in nested fragments. See this <a href='http://ideaventure.blogspot.com.au/2014/10/nested-retained-fragment-lost-state.html'>solution</a>
         * FIXME: ChildFragmentManager hack - remove this method when the bug is fixed in future android support library
         */
        protected FragmentManager childFragmentManager() {
            if (mRetainedChildFragmentManager == null) {
                mRetainedChildFragmentManager = getChildFragmentManager();
            }
            return mRetainedChildFragmentManager;
        }

        /**
         * Hack to fix this <a href='https://code.google.com/p/android/issues/detail?id=74222'>bug</a>
         * with this <a href='http://ideaventure.blogspot.com.au/2014/10/nested-retained-fragment-lost-state.html'>solution</a>
         * FIXME: ChildFragmentManager hack - remove this method when the bug is fixed in future android support library
         */
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            if (mRetainedChildFragmentManager != null) {
                //restore the last retained child fragment manager to the new
                //created fragment
                try {
                    Field childFMField = Fragment.class.getDeclaredField("mChildFragmentManager");
                    childFMField.setAccessible(true);
                    childFMField.set(this, mRetainedChildFragmentManager);
                } catch (Exception e) {
                    mLogger.warn(e.getMessage(), e);
                }
            } else {
                //If the child fragment manager has not been retained yet, let it hold the internal
                //child fragment manager as early as possible. This can prevent child fragment
                //manager from missing to be set and then retained, which could happen when
                //OS kills activity and restarts it. In this case, the delegate fragment restored
                //but childFragmentManager() may not be called so mRetainedChildFragmentManager is
                //yet set. If the fragment is rotated, the state of child fragment manager will be
                //lost since mRetainedChildFragmentManager hasn't set to be retained by the OS.
                mRetainedChildFragmentManager = getChildFragmentManager();
            }
        }

        /**
         * Gets the id of activity layout resource. By default it's a single
         * {@link android.widget.FrameLayout} into which new fragment will be injected into during
         * navigation. Eg. During navigation, FragmentA, FragmentB and etc will replace the current
         * containing fragment inside this {@link android.widget.FrameLayout}.
         * <p>
         * Overrides this method to provide custom layout if complex layout is required. For
         * example, a {@link android.support.v4.widget.DrawerLayout} maybe needed in this fragment.
         * In this case, create a custom layout with the {@link android.support.v4.widget.DrawerLayout}
         * and corresponding components. <br><br>
         * <b>
         * Note that, once this methods is overridden to provide a custom view,
         * {@link #getContentLayoutResId()} MUST be overridden as well to provide the
         * id of the layout in the custom layout that will be used to place navigating fragments.
         * </b>
         * </p>
         *
         * @return The resource id of the root layout of the activity
         */
        @Override
        protected int getLayoutResId() {
            return R.layout.android_mvc_delegate_fragment;
        }

        /**
         * Provides the id of the layout that will be used to hold navigating fragments. Note that,
         * when {@link #getLayoutResId()} is overridden, this method MUST be overridden as well.
         *
         * @return The content layout resource id
         * @throws IllegalStateException when {@link #getLayoutResId()} is overridden but this
         *                               method is not.
         */
        protected int getContentLayoutResId() {
            if (getLayoutResId() != R.layout.android_mvc_delegate_fragment) {
                String msg = String.format("%s.getContentLayoutResId() must be overridden to " +
                                "provide the layout that is used to hold navigating fragments.",
                        getClass().getName());
                throw new IllegalStateException(msg);
            }
            return R.id.android_mvc_delegate_fragment_content;
        }

        /**
         * @return Navigation controller. Alternatively, {@link NavigationController} can be injected
         * by @Inject in any other fragments which will share the same instance as this one, since
         * all injected controllers would be singleton across the whole application.
         */
        public NavigationController getNavigationController() {
            return mNavigationController;
        }

        @Override
        public boolean onBackButtonPressed() {
            MvcFragment topFragment = null;
            //FIXME: ChildFragmentManager hack - use getChildFragmentManager when bug is fixed
            NavLocation curLoc = mNavigationController.getModel().getCurrentLocation();
            if (curLoc != null && curLoc.getLocationId() != null) {
                topFragment = (MvcFragment) childFragmentManager().findFragmentByTag(
                        getFragmentTag(curLoc.getLocationId()));
            }

            boolean navigateBack = false;
            if (topFragment != null) {
                navigateBack = !topFragment.onBackButtonPressed();
            }
            if (navigateBack) {
                mNavigationController.navigateBack(this);
            }
            return true;
        }

        private String getFragmentTag(String locationId) {
            return AndroidMvc.FRAGMENT_TAG_PREFIX + locationId;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            //make sure the base activity has the reference to this container fragment before restoring other fragments..
            //When the activity gets killed by the OS and recreated then all fragments contained by the activity will be
            //recreated. All these will happen before BaseActivity.super.onCreate() finishes when findFragmentById
            //has not been ready to work thus the mContainerFragment would be null for BaseActivity and
            //mContainerFragment.getControllersInjector() will return null.This will crash other fragments to inject
            //their controllers
            MvcActivity activity = ((MvcActivity) getActivity());
            activity.mDelegateFragment = this;
        }

        @Override
        void preInvokeCallbackOnViewCreated(final View view, final Bundle savedInstanceState) {
            super.preInvokeCallbackOnViewCreated(view, savedInstanceState);
            if (savedInstanceState != null) {
                delayOnViewReady = true;

                //When the delegate fragment is restoring it should notify all visible fragments
                //to hold to call their onViewReady callback until state is restored. Because Android
                //calls onViewStateRestored of DelegateFragment after all nested fragments call
                //their onViewCreated life cycle, but onViewReady inside onViewCreated should
                //guarantee when onViewReady of nested fragments get called, all state of them should
                //be restored which is done this onViewStateRestored of this DelegateFragment. So
                //we need to call onViewReady of nested fragments when onViewStateRestored of this
                //DelegateFragment finishes.
                List<Fragment> frags = childFragmentManager().getFragments();
                int size = frags.size();
                for (int i = 0; i < size; i++) {
                    Fragment frag = frags.get(i);
                    if (frag != null && frag.isAdded() && frag instanceof MvcFragment) {
                        ((MvcFragment) frag).delayOnViewReady = true;
                    }
                }
            }
        }

        @Override
        public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
            super.onViewReady(view, savedInstanceState, reason);
            if (reason == Reason.FIRST_TIME) {
                onStartUp();
            }
        }

        @Override
        public void onViewStateRestored(Bundle savedInstanceState) {
            super.onViewStateRestored(savedInstanceState);

            if (savedInstanceState != null) {
                Bundle mvcOutState = savedInstanceState.getBundle(MVC_STATE_BUNDLE_KEY);
                long ts = System.currentTimeMillis();
                AndroidMvc.restoreStateOfControllers(mvcOutState);
                mLogger.trace("Restored state of all active controllers, {}ms used.", System.currentTimeMillis() - ts);

                if (mPendingOnViewReadyActions != null) {
                    int size = mPendingOnViewReadyActions.size();
                    for (int i = 0; i < size; i++) {
                        mPendingOnViewReadyActions.get(i).run();
                    }
                    mPendingOnViewReadyActions.clear();
                }
            }
        }

        /**
         * Called when the app starts up for the first time. Use {@link NavigationController} to
         * navigate to the initial fragment in this callback. {@link NavigationController} can be
         * obtained by {@link #getNavigationController()} or even be injected again. This callback is
         * equivalent to override {@link #onViewReady(View, Bundle, Reason)} and perform action when
         * reason of view ready of this {@link DelegateFragment} is {@link Reason#FIRST_TIME}.
         *
         * <p>
         * Note this callback will NOT be invoked on restoration after the app is killed by the OS from background.
         * </p>
         */
        protected abstract void onStartUp();

        @Override
        public void onStart() {
            super.onStart();
            mStateSaved = false;
            runPendingNavigationActions();
        }

        private void runPendingNavigationActions() {
            if (!mPendingNavActions.isEmpty()) {
                for (Runnable r : mPendingNavActions) {
                    r.run();
                }
                mPendingNavActions.clear();
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            mStateSaved = true;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            long ts = System.currentTimeMillis();
            Bundle mvcOutState = new Bundle();
            AndroidMvc.saveStateOfControllers(mvcOutState);
            outState.putBundle(MVC_STATE_BUNDLE_KEY, mvcOutState);
            mLogger.trace("Save state of all active controllers, {}ms used.", System.currentTimeMillis() - ts);
        }

        public void onEvent(final NavigationController.EventC2V.OnLocationForward event) {
            if (mStateSaved) {
                //TODO: seems will have potential problem when the app is killed. Should
                //respond back to nav controller to issue the back action action when the app restarts.
                mPendingNavActions.add(new Runnable() {
                    @Override
                    public void run() {
                        performForwardNav(event);
                    }
                });
            } else {
                performForwardNav(event);
            }
        }

        @SuppressWarnings("unchecked")
        private void performForwardNav(NavigationController.EventC2V.OnLocationForward event) {
            //FIXME: ChildFragmentManager hack - use getChildFragmentManager when bug is fixed
            FragmentManager fm = childFragmentManager();

            MvcActivity activity = ((MvcActivity) getActivity());

            Class<? extends MvcFragment> fragmentClass = activity.mapNavigationFragment(event.getCurrentValue().getLocationId());
            if (fragmentClass == null) {
                throw new RuntimeException("Must provide the class type of fragment for location: "
                        + event.getCurrentValue().getLocationId());
            } else {
                FragmentTransaction transaction = fm.beginTransaction();
                String fragmentTag = getFragmentTag(event.getCurrentValue().getLocationId());

                MvcFragment fragment;
                try {
                    fragment = (MvcFragment) new ReflectUtils.newObjectByType(fragmentClass).newInstance();
                    fragment.injectDependencies();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate fragment: " + fragmentClass.getName(), e);
                }

                if (!event.isClearHistory()) {
                    MvcFragment lastFrag = null;
                    if (event.getLastValue() != null && event.getLastValue().getLocationId() != null) {
                        lastFrag = (MvcFragment) fm.findFragmentByTag(
                                getFragmentTag(event.getLastValue().getLocationId()));
                    }
                    if (lastFrag != null) {
                        lastFrag.onPushingToBackStack();
                        lastFrag.releaseDependencies();
                    }
                } else {
                    NavLocation clearTopToLocation = event.getLocationWhereHistoryClearedUpTo();
                    String tagPopTo = clearTopToLocation == null ? null : getFragmentTag(clearTopToLocation.getLocationId());

                    //clear back stack fragments
                    if(tagPopTo == null) {
                        //Clear all, must use flag FragmentManager.POP_BACK_STACK_INCLUSIVE
                        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    } else {
                        //Clear to specific fragment. Use 0 to leave the given fragment in the stack as
                        //the last one.
                        fm.popBackStack(tagPopTo, 0);
                    }

                    mLogger.trace("Cleared fragment back stack up to {}", tagPopTo);
                }

                transaction.replace(getContentLayoutResId(), fragment, fragmentTag);
                transaction.addToBackStack(fragmentTag);
                transaction.commit();
            }
        }

        public void onEvent(final NavigationController.EventC2V.OnLocationBack event) {
            if (mStateSaved) {
                mPendingNavActions.add(new Runnable() {
                    @Override
                    public void run() {
                        performBackNav(event);
                    }
                });
            } else {
                performBackNav(event);
            }
        }

        private void performBackNav(NavigationController.EventC2V.OnLocationBack event) {
            NavLocation currentLoc = event.getCurrentValue();
            if (currentLoc == null) {
                //Back to null which should finish the current activity
                ((MvcActivity)getActivity()).performSuperBackKeyPressed();
            } else {
                //FIXME: ChildFragmentManager hack - use getChildFragmentManager when bug is fixed
                FragmentManager fm = childFragmentManager();

                String currentFragTag = getFragmentTag(currentLoc.getLocationId());
                final MvcFragment currentFrag = (MvcFragment) fm.findFragmentByTag(currentFragTag);
                if (currentFrag != null) {
                    currentFrag.injectDependencies();
                    currentFrag.registerOnViewReadyListener(new Runnable() {
                        @Override
                        public void run() {
                            currentFrag.onPoppedOutToFront();
                            unregisterOnViewReadyListener(this);
                        }
                    });
                }

                if (event.isFastRewind()) {
                    if (currentLoc.getPreviousLocation() == null) {
                        if(fm.getBackStackEntryCount() <= 1) {
                            //Has reached bottom. Does nothing in this case
                            return;
                        }

                        //Pop fragments to the last
                        int stackCount = fm.getBackStackEntryCount();
                        int timesNeedToPop = 0;
                        for (int i = 0; i < stackCount; i++) {
                            if (currentFragTag.equals(fm.getBackStackEntryAt(i).getName())) {
                                timesNeedToPop++;
                            }
                        }

                        if (timesNeedToPop > 1) {
                            for (int i = 0; i < stackCount - 1; i++) {
                                fm.popBackStack();
                            }
                            fm.executePendingTransactions();
                        } else {
                            fm.popBackStack(currentFragTag, 0);
                        }
                        mLogger.trace("Navigation back: Fast rewind to home location {}", currentLoc.getLocationId());
                    } else {
                        String tag = getFragmentTag(currentLoc.getLocationId());
                        fm.popBackStack(tag, 0);
                        mLogger.trace("Navigation back: Fast rewind to given location {}", currentLoc.getLocationId());
                    }
                } else {
                    fm.popBackStack();
                    mLogger.trace("Navigation back: On step back to location {}", currentLoc.getLocationId());
                }
            }
        }
    }
}
