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
import com.shipdream.lib.android.mvc.StateManaged;
import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.poke.util.ReflectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public abstract class MvcActivity extends AppCompatActivity {
    private DelegateFragment delegateFragment;

    String getDelegateFragmentTag() {
        return AndroidMvc.FRAGMENT_TAG_PREFIX + getDelegateFragmentClass().getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mvc_activity);
        delegateFragment = (DelegateFragment) getSupportFragmentManager().findFragmentByTag(
                getDelegateFragmentTag());

        if (delegateFragment == null) {
            //Brand new container fragment
            try {
                delegateFragment = (DelegateFragment) new ReflectUtils.newObjectByType(
                        getDelegateFragmentClass()).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate delegate fragment.", e);
            }
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.replace(R.id.android_mvc_activity_root, delegateFragment, getDelegateFragmentTag());
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
        delegateFragment.onBackButtonPressed();
    }

    /**
     * Add callback so that onViewReady will be delay to call after all instance state are restored
     * @param runnable The delayed onViewReady callbacks
     */
    void addPendingOnViewReadyActions(Runnable runnable) {
        delegateFragment.pendingOnViewReadyActions.add(runnable);
    }

    /**
     * This fragment is the container fragment as a root of the activity. When navigating by
     * {@link NavigationController}, new fragments will be created and replace the root view of this
     * fragment or pop out the stacked history fragments. {@link NavigationController} can be simply
     * injected into any fragments extending {@link MvcFragment} by fields annotated by @Inject.
     */
    public static abstract class DelegateFragment extends MvcFragment {
        private static final String MVC_STATE_BUNDLE_KEY = AndroidMvc.MVC_SATE_PREFIX + "RootBundle";
        private Logger logger = LoggerFactory.getLogger(getClass());
        //Track if the state is saved and not able to commit fragment transaction
        private boolean canCommitFragmentTransaction = false;
        private List<Runnable> pendingNavActions = new ArrayList<>();
        private List<Runnable> pendingOnViewReadyActions = new ArrayList<>();

        @Inject
        private NavigationController navigationController;

        /**
         * Hack to fix this <a href='https://code.google.com/p/android/issues/detail?id=74222'>bug</a>
         * with this <a href='http://ideaventure.blogspot.com.au/2014/10/nested-retained-fragment-lost-state.html'>solution</a>
         * FIXME: ChildFragmentManager hack - remove this method when the bug is fixed in future android support library
         */
        private FragmentManager retainedChildFragmentManager;

        /**
         * Get child fragment manager with android support lib rev20/rev21 which has a the
         * <a href='https://code.google.com/p/android/issues/detail?id=74222'>bug</a> to retain child
         * fragment manager in nested fragments. See this <a href='http://ideaventure.blogspot.com.au/2014/10/nested-retained-fragment-lost-state.html'>solution</a>
         * FIXME: ChildFragmentManager hack - remove this method when the bug is fixed in future android support library
         */
        protected FragmentManager childFragmentManager() {
            if (retainedChildFragmentManager == null) {
                retainedChildFragmentManager = getChildFragmentManager();
            }
            return retainedChildFragmentManager;
        }

        /**
         * Hack to fix this <a href='https://code.google.com/p/android/issues/detail?id=74222'>bug</a>
         * with this <a href='http://ideaventure.blogspot.com.au/2014/10/nested-retained-fragment-lost-state.html'>solution</a>
         * FIXME: ChildFragmentManager hack - remove this method when the bug is fixed in future android support library
         */
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            if (retainedChildFragmentManager != null) {
                //restore the last retained child fragment manager to the new
                //created fragment
                try {
                    Field childFMField = Fragment.class.getDeclaredField("mChildFragmentManager");
                    childFMField.setAccessible(true);
                    childFMField.set(this, retainedChildFragmentManager);
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            } else {
                //If the child fragment manager has not been retained yet, let it hold the internal
                //child fragment manager as early as possible. This can prevent child fragment
                //manager from missing to be set and then retained, which could happen when
                //OS kills activity and restarts it. In this case, the delegate fragment restored
                //but childFragmentManager() may not be called so mRetainedChildFragmentManager is
                //yet set. If the fragment is rotated, the state of child fragment manager will be
                //lost since mRetainedChildFragmentManager hasn't set to be retained by the OS.
                retainedChildFragmentManager = getChildFragmentManager();
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
            return navigationController;
        }

        @Override
        public boolean onBackButtonPressed() {
            MvcFragment topFragment = null;
            //FIXME: ChildFragmentManager hack - use getChildFragmentManager when bug is fixed
            NavLocation curLoc = navigationController.getModel().getCurrentLocation();
            if (curLoc != null && curLoc.getLocationId() != null) {
                topFragment = (MvcFragment) childFragmentManager().findFragmentByTag(
                        getFragmentTag(curLoc.getLocationId()));
            }

            boolean navigateBack = false;
            if (topFragment != null) {
                navigateBack = !topFragment.onBackButtonPressed();
            }
            if (navigateBack) {
                navigationController.navigateBack(this);
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
            MvcActivity activity = ((MvcActivity) getActivity());
            activity.delegateFragment = this;

            if (savedInstanceState != null) {
                notifyAllSubMvcFragmentsTheirStateIsManagedByMe(this, true);

                Bundle mvcOutState = savedInstanceState.getBundle(MVC_STATE_BUNDLE_KEY);
                long ts = System.currentTimeMillis();
                AndroidMvc.restoreStateOfAllControllers(mvcOutState);
                logger.trace("Restored state of all active controllers, {}ms used.", System.currentTimeMillis() - ts);
            }
        }

        private boolean firstTimeRun = false;

        @Override
        public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
            super.onViewReady(view, savedInstanceState, reason);
            if (reason == Reason.FIRST_TIME) {
                firstTimeRun = true;
            }
        }

        @Override
        public void onViewStateRestored(Bundle savedInstanceState) {
            super.onViewStateRestored(savedInstanceState);

            if (savedInstanceState != null && pendingOnViewReadyActions != null) {
                int size = pendingOnViewReadyActions.size();
                for (int i = 0; i < size; i++) {
                    pendingOnViewReadyActions.get(i).run();
                }
                pendingOnViewReadyActions.clear();
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
        public void onResume() {
            super.onResume();
            canCommitFragmentTransaction = true;
            runPendingNavigationActions();

            if (firstTimeRun) {
                //Run onStartUp() in onResume after onViewReady to make sure the extending fragments
                //views are ready before do the startup action
                onStartUp();
            }
            firstTimeRun = false;
        }

        private void runPendingNavigationActions() {
            if (!pendingNavActions.isEmpty()) {
                for (Runnable r : pendingNavActions) {
                    r.run();
                }
                pendingNavActions.clear();
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            canCommitFragmentTransaction = false;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            long ts = System.currentTimeMillis();
            Bundle mvcOutState = new Bundle();
            AndroidMvc.saveStateOfAllControllers(mvcOutState);
            outState.putBundle(MVC_STATE_BUNDLE_KEY, mvcOutState);
            logger.trace("Save state of all active controllers, {}ms used.", System.currentTimeMillis() - ts);
        }

        /**
         * Notify all sub MvcFragments theirs state is managed by this root fragment. So all
         * {@link StateManaged} objects those fragments holding will be saved into this root
         * fragment's outState bundle.
         */
        private void notifyAllSubMvcFragmentsTheirStateIsManagedByMe(MvcFragment fragment, boolean selfManaged) {
            if (fragment != null) {
                fragment.isStateManagedByRootDelegateFragment = selfManaged;

                List<Fragment> frags = fragment.getChildFragmentManager().getFragments();
                if (frags != null) {
                    int size = frags.size();
                    for (int i = 0; i < size; i++) {
                        MvcFragment frag = (MvcFragment) frags.get(i);
                        if (frag != null) {
                            if(frag.isAdded() && frag instanceof MvcFragment) {
                                frag.isStateManagedByRootDelegateFragment = selfManaged;
                            }
                            notifyAllSubMvcFragmentsTheirStateIsManagedByMe(frag, selfManaged);
                        }
                    }
                }
            }
        }

        public void onEvent(final NavigationController.EventC2V.OnLocationForward event) {
            if (!canCommitFragmentTransaction) {
                pendingNavActions.add(new Runnable() {
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

                    logger.trace("Cleared fragment back stack up to {}", tagPopTo);
                }

                transaction.replace(getContentLayoutResId(), fragment, fragmentTag);
                transaction.addToBackStack(fragmentTag);
                transaction.commit();
            }
        }

        public void onEvent(final NavigationController.EventC2V.OnLocationBack event) {
            if (!canCommitFragmentTransaction) {
                pendingNavActions.add(new Runnable() {
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
                        logger.trace("Navigation back: Fast rewind to home location {}", currentLoc.getLocationId());
                    } else {
                        String tag = getFragmentTag(currentLoc.getLocationId());
                        fm.popBackStack(tag, 0);
                        logger.trace("Navigation back: Fast rewind to given location {}", currentLoc.getLocationId());
                    }
                } else {
                    fm.popBackStack();
                    logger.trace("Navigation back: On step back to location {}", currentLoc.getLocationId());
                }
            }
        }
    }
}
