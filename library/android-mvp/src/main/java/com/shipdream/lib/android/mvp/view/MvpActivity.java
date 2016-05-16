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

package com.shipdream.lib.android.mvp.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentHostCallback;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.shipdream.lib.android.mvc.view.R;
import com.shipdream.lib.android.mvp.Injector;
import com.shipdream.lib.android.mvp.MvpBean;
import com.shipdream.lib.android.mvp.NavLocation;
import com.shipdream.lib.android.mvp.__MvpGraphHelper;
import com.shipdream.lib.android.mvp.event.BaseEventV;
import com.shipdream.lib.android.mvp.manager.NavigationManager;
import com.shipdream.lib.android.mvp.manager.internal.__MvpManagerHelper;
import com.shipdream.lib.android.mvp.presenter.internal.BaseControllerImpl;
import com.shipdream.lib.poke.util.ReflectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public abstract class MvpActivity extends AppCompatActivity {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static final String FRAGMENT_TAG_PREFIX = "__--AndroidMvp:Fragment:";
    private DelegateFragment delegateFragment;
    boolean toPrintAppExitMessage = false;

    String getDelegateFragmentTag() {
        return FRAGMENT_TAG_PREFIX + getDelegateFragmentClass().getName();
    }

    private EventRegister eventRegister;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eventRegister = new EventRegister(this);
        eventRegister.onCreate();

        eventRegister.registerEventBuses();

        setContentView(R.layout.mvp_activity);
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
            trans.replace(R.id.android_mvp_activity_root, delegateFragment, getDelegateFragmentTag());
            trans.commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        eventRegister.unregisterEventBuses();
        eventRegister.onDestroy();

        if (toPrintAppExitMessage && logger.isTraceEnabled()) {
            logger.trace("App Exits(UI): {} injected beans are still cached.",
                    __MvpGraphHelper.getAllCachedInstances(Injector.getGraph()).size());
            toPrintAppExitMessage = false;
        }
    }

    void performSuperBackKeyPressed() {
        super.onBackPressed();
    }

    /**
     * Provides class types of fragments to present navigation location of given location id.
     *
     * @param locationId The location id in string
     * @return The class type of the {@link MvpFragment}
     */
    protected abstract Class<? extends MvpFragment> mapNavigationFragment(String locationId);

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
     * Post an event from this view to other views. Events sent to views should be managed by controllers.
     * <p>However, it's handy in some scenarios. For example, when routing intent received by Activities to
     * Fragments, EventBusV is a handy solution. Note that the AndroidMvp framework is a single
     * Activity design and it manages views on fragment level and fragments don't have
     * onNewIntent(Intent intent) method. When a fragment needs to handle an intent, use eventBusV
     * to route the intent to fragments from the main activity.</p>
     *
     * @param event The event to views
     */
    protected void postEvent2V(BaseEventV event) {
        eventRegister.postEvent2V(event);
    }
    /**
     * Add callback so that onViewReady will be delay to call after all instance state are restored
     *
     * @param runnable The delayed onViewReady callbacks
     */
    void addPendingOnViewReadyActions(Runnable runnable) {
        delegateFragment.pendingOnViewReadyActions.add(runnable);
    }

    private static class DelegateFragmentController extends BaseControllerImpl {
        private Handler handler = new Handler(Looper.getMainLooper());

        @Inject
        private NavigationManager navigationManager;

        private DelegateFragment delegateFragment;

        private void onEvent(final NavigationManager.Event2C.OnLocationForward event) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    delegateFragment.handleForwardNavigation(event);
                }
            });
        }

        private void onEvent(final NavigationManager.Event2C.OnLocationBack event) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    delegateFragment.handleBackNavigation(event);
                }
            });
        }

        @Override
        public Class modelType() {
            return null;
        }

        private void navigateBack(Object sender) {
            navigationManager.navigate(sender).back();
        }

        private NavLocation getCurrentLocation() {
            return navigationManager.getModel().getCurrentLocation();
        }
    }

    /**
     * This fragment is the container fragment as a root of the activity. When navigating by
     * {@link NavigationManager}, new fragments will be created and replace the root view of this
     * fragment or pop out the stacked history fragments. {@link NavigationManager} can be simply
     * injected into any fragments extending {@link MvpFragment} by fields annotated by @Inject.
     */
    public static abstract class DelegateFragment extends MvpFragment {
        private static final String MVP_STATE_BUNDLE_KEY = DefaultModelKeeper.MVP_SATE_PREFIX + "RootBundle";
        private Logger logger = LoggerFactory.getLogger(getClass());
        //Track if the state is saved and not able to commit fragment transaction
        private boolean canCommitFragmentTransaction = false;
        private List<Runnable> pendingNavActions = new ArrayList<>();
        private List<Runnable> pendingOnViewReadyActions = new ArrayList<>();

        @Inject
        private DelegateFragmentController delegateFragmentController;

        /**
         * Hack to fix this <a href='https://code.google.com/p/android/issues/detail?id=74222'>bug</a>
         * with this <a href='http://ideaventure.blogspot.com.au/2014/10/nested-retained-fragment-lost-state.html'>solution</a>
         * FIXME: ChildFragmentManager hack - remove this method when the bug is fixed in future android support library
         */
        private FragmentManager retainedChildFragmentManager;
        private FragmentHostCallback currentHost;
        private Class fragmentImplClass;
        private Field mHostField;

        {
            try {
                fragmentImplClass = Class.forName("android.support.v4.app.FragmentManagerImpl");
                mHostField = fragmentImplClass.getDeclaredField("mHost");
                mHostField.setAccessible(true);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("FragmentManagerImpl is renamed due to the " +
                        "change of Android SDK, this workaround doesn't work any more. " +
                        "See the issue at " +
                        "https://code.google.com/p/android/issues/detail?id=74222", e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("FragmentManagerImpl.mHost is found due to the " +
                        "change of Android SDK, this workaround doesn't work any more. " +
                        "See the issue at " +
                        "https://code.google.com/p/android/issues/detail?id=74222", e);
            }
        }

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
        public void onAttach(Context context) {
            super.onAttach(context);
            if (retainedChildFragmentManager != null) {
                //restore the last retained child fragment manager to the new
                //created fragment
                try {
                    //Copy the mHost(Activity) to retainedChildFragmentManager
                    currentHost = (FragmentHostCallback) mHostField.get(getFragmentManager());

                    Field childFMField = Fragment.class.getDeclaredField("mChildFragmentManager");
                    childFMField.setAccessible(true);
                    childFMField.set(this, retainedChildFragmentManager);

                    refreshHosts(getFragmentManager());
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
                //Refresh children fragment's hosts
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

        private void refreshHosts(FragmentManager fragmentManager) throws IllegalAccessException {
            if (fragmentManager != null) {
                replaceFragmentManagerHost(fragmentManager);
            }

            List<Fragment> frags = fragmentManager.getFragments();
            if (frags != null) {
                for (Fragment f : frags) {
                    if (f != null) {
                        try {
                            //Copy the mHost(Activity) to retainedChildFragmentManager
                            Field mHostField = Fragment.class.getDeclaredField("mHost");
                            mHostField.setAccessible(true);
                            mHostField.set(f, currentHost);
                        } catch (Exception e) {
                            logger.warn(e.getMessage(), e);
                        }
                        if (f.getChildFragmentManager() != null) {
                            refreshHosts(f.getChildFragmentManager());
                        }
                    }
                }
            }
        }

        private void replaceFragmentManagerHost(FragmentManager fragmentManager) throws IllegalAccessException {
            if (currentHost != null) {
                mHostField.set(fragmentManager, currentHost);
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
            return R.layout.android_mvp_delegate_fragment;
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
            if (getLayoutResId() != R.layout.android_mvp_delegate_fragment) {
                String msg = String.format("%s.getContentLayoutResId() must be overridden to " +
                                "provide the layout that is used to hold navigating fragments.",
                        getClass().getName());
                throw new IllegalStateException(msg);
            }
            return R.id.android_mvp_delegate_fragment_content;
        }

        @Override
        public boolean onBackButtonPressed() {
            MvpFragment topFragment = null;
            //FIXME: ChildFragmentManager hack - use getChildFragmentManager when bug is fixed
            NavLocation curLoc = delegateFragmentController.getCurrentLocation();
            if (curLoc != null && curLoc.getLocationId() != null) {
                topFragment = (MvpFragment) childFragmentManager().findFragmentByTag(
                        getFragmentTag(curLoc.getLocationId()));
            }

            boolean navigateBack = false;
            if (topFragment != null) {
                navigateBack = !topFragment.onBackButtonPressed();
            }
            if (navigateBack) {
                delegateFragmentController.navigateBack(this);
            }
            return true;
        }

        private String getFragmentTag(String locationId) {
            return FRAGMENT_TAG_PREFIX + locationId;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            MvpActivity activity = ((MvpActivity) getActivity());
            activity.delegateFragment = this;
        }

        void onPreViewReady(final View view, final Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                notifyAllSubMvpFragmentsTheirStateIsManagedByMe(this, true);
            }

            delegateFragmentController.delegateFragment = this;
        }

        private boolean firstTimeRun = false;

        @Override
        public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
            super.onViewReady(view, savedInstanceState, reason);
            canCommitFragmentTransaction = true;
            if (reason.isFirstTime()) {
                firstTimeRun = true;
            }
        }

        @Override
        public void onViewStateRestored(Bundle savedInstanceState) {
            super.onViewStateRestored(savedInstanceState);

            if (savedInstanceState != null) {
                Bundle mvpOutState = savedInstanceState.getBundle(MVP_STATE_BUNDLE_KEY);
                long ts = System.currentTimeMillis();
                DefaultStateKeeperHolder.restoreStateOfAllControllers(mvpOutState);
                logger.trace("Restored state of all active controllers, {}ms used.", System.currentTimeMillis() - ts);

                notifyAllSubMvpFragmentsTheirStateIsManagedByMe(this, false);

                if (pendingOnViewReadyActions != null) {
                    int size = pendingOnViewReadyActions.size();
                    for (int i = 0; i < size; i++) {
                        pendingOnViewReadyActions.get(i).run();
                    }
                    pendingOnViewReadyActions.clear();
                }
            }
        }

        /**
         * Called when the app starts up for the first time. Use {@link NavigationManager} to
         * navigate to the initial fragment in this callback. {@link NavigationManager} can be
         * obtained by inject {@link NavigationManager} to the view's controller. This callback is
         * equivalent to override {@link #onViewReady(View, Bundle, Reason)} and perform action when
         * reason of view ready of this {@link DelegateFragment} is {@link Reason#isFirstTime()}.
         * <p/>
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
            Bundle mvpOutState = new Bundle();
            DefaultStateKeeperHolder.saveStateOfAllControllers(mvpOutState);
            outState.putBundle(MVP_STATE_BUNDLE_KEY, mvpOutState);
            logger.trace("Save state of all active controllers, {}ms used.", System.currentTimeMillis() - ts);

            notifyAllSubMvpFragmentsTheirStateIsManagedByMe(this, true);
        }

        /**
         * Notify all sub MvpFragments theirs state is managed by this root fragment. So all
         * {@link MvpBean} objects those fragments holding will be saved into this root
         * fragment's outState bundle.
         */
        private void notifyAllSubMvpFragmentsTheirStateIsManagedByMe(MvpFragment fragment, final boolean selfManaged) {
            traverseFragmentAndSubFragments(fragment, new FragmentManipulator() {
                @Override
                public void manipulate(Fragment fragment) {
                    if (fragment != null && fragment.isAdded() && fragment instanceof MvpFragment) {
                        ((MvpFragment)fragment).isStateManagedByRootDelegateFragment = selfManaged;
                    }
                }
            });
        }

        /**
         * Handle the forward navigation event call back
         *
         * @param event The forward navigation event
         */
        private void handleForwardNavigation(final NavigationManager.Event2C.OnLocationForward event) {
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

        private void traverseFragmentAndSubFragments(Fragment fragment, FragmentManipulator manipulator) {
            if (fragment != null) {
                manipulator.manipulate(fragment);

                List<Fragment> frags = fragment.getChildFragmentManager().getFragments();
                if (frags != null) {
                    int size = frags.size();
                    for (int i = 0; i < size; i++) {
                        MvpFragment frag = (MvpFragment) frags.get(i);
                        if (frag != null) {
                            manipulator.manipulate(frag);
                        }
                    }
                }
            }
        }

        interface FragmentManipulator {
            void manipulate(Fragment fragment);
        }

        @SuppressWarnings("unchecked")
        private void performForwardNav(final NavigationManager.Event2C.OnLocationForward event) {
            //FIXME: ChildFragmentManager hack - use getChildFragmentManager when bug is fixed
            FragmentManager fm = childFragmentManager();

            MvpActivity activity = ((MvpActivity) getActivity());

            Class<? extends MvpFragment> fragmentClass = activity.mapNavigationFragment(event.getCurrentValue().getLocationId());
            if (fragmentClass == null) {
                throw new RuntimeException("Cannot find fragment class mapped in MvpActivity.mapNavigationFragment(location) for location: "
                        + event.getCurrentValue().getLocationId());
            } else {
                MvpFragment lastFragment = null;
                if (event.getLastValue() != null && event.getLastValue().getLocationId() != null) {
                    lastFragment = (MvpFragment) fm.findFragmentByTag(
                            getFragmentTag(event.getLastValue().getLocationId()));
                }

                final MvpFragment currentFragment;
                try {
                    currentFragment = (MvpFragment) new ReflectUtils.newObjectByType(fragmentClass).newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate fragment: " + fragmentClass.getName(), e);
                }

                if (event.isClearHistory()) {
                    NavLocation clearTopToLocation = event.getLocationWhereHistoryClearedUpTo();
                    String tagPopTo = clearTopToLocation == null ? null : getFragmentTag(clearTopToLocation.getLocationId());

                    //clear back stack fragments
                    if (tagPopTo == null) {
                        //Clear all, must use flag FragmentManager.POP_BACK_STACK_INCLUSIVE
                        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    } else {
                        //Clear to specific fragment. Use 0 to leave the given fragment in the stack as
                        //the last one.
                        fm.popBackStack(tagPopTo, 0);
                    }

                    logger.trace("Cleared fragment back stack up to {}", tagPopTo);
                }

                final FragmentTransaction transaction = fm.beginTransaction();
                currentFragment.registerOnViewReadyListener(new Runnable() {
                    @Override
                    public void run() {
                        if (event.getNavigator() != null) {
                            __MvpManagerHelper.destroyNavigator(event.getNavigator());
                        }

                        logger.trace("Fragment ready: " + currentFragment.getClass().getSimpleName());

                        currentFragment.unregisterOnViewReadyListener(this);
                    }
                });

                String fragmentTag = getFragmentTag(event.getCurrentValue().getLocationId());
                transaction.replace(getContentLayoutResId(), currentFragment, fragmentTag);

                boolean interim = false;
                NavLocation lastLocation = event.getLastValue();
                if (lastLocation != null && lastLocation.isInterim()) {
                    interim = true;
                }
                if (!interim) {
                    transaction.addToBackStack(fragmentTag);
                    traverseFragmentAndSubFragments(lastFragment, new FragmentManipulator() {
                        @Override
                        public void manipulate(Fragment fragment) {
                            if (fragment != null && fragment instanceof MvpFragment) {
                                ((MvpFragment)fragment).onPushingToBackStack();
                            }
                        }
                    });
                }

                if (lastFragment != null) {
                    //Invoke OnPreTransactionCommit for fragment and its child fragments recursively
                    traverseFragmentAndSubFragments(lastFragment, new FragmentManipulator() {
                        @Override
                        public void manipulate(Fragment fragment) {
                            if (fragment != null && fragment instanceof MvpFragment) {
                                ((MvpFragment)fragment).onPreNavigationTransaction(transaction, currentFragment);
                            }
                        }
                    });
                }
                transaction.commit();
            }
        }

        /**
         * Handle the backward navigation event call back
         *
         * @param event The backward navigation event
         */
        private void handleBackNavigation(final NavigationManager.Event2C.OnLocationBack event) {
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

        private void performBackNav(final NavigationManager.Event2C.OnLocationBack event) {
            NavLocation currentLoc = event.getCurrentValue();
            if (currentLoc == null) {
                if (event.getNavigator() != null) {
                    __MvpManagerHelper.destroyNavigator(event.getNavigator());
                }

                MvpActivity mvpActivity = ((MvpActivity) getActivity());
                //Back to null which should finish the current activity
                mvpActivity.performSuperBackKeyPressed();
                mvpActivity.toPrintAppExitMessage = true;
            } else {
                //FIXME: ChildFragmentManager hack - use getChildFragmentManager when bug is fixed
                FragmentManager fm = childFragmentManager();

                String currentFragTag = getFragmentTag(currentLoc.getLocationId());
                final MvpFragment currentFrag = (MvpFragment) fm.findFragmentByTag(currentFragTag);
                if (currentFrag != null) {
                    traverseFragmentAndSubFragments(currentFrag, new FragmentManipulator() {
                        @Override
                        public void manipulate(Fragment fragment) {
                            if (fragment != null && fragment instanceof MvpFragment) {
                                final MvpFragment frag = ((MvpFragment)fragment);
                                frag.aboutToPopOut = true;
                                frag.registerOnViewReadyListener(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (event.getNavigator() != null) {
                                            __MvpManagerHelper.destroyNavigator(event.getNavigator());
                                        }
                                        frag.unregisterOnViewReadyListener(this);
                                    }
                                });
                            }
                        }
                    });


                }

                if (event.isFastRewind()) {
                    if (currentLoc.getPreviousLocation() == null) {
                        if (fm.getBackStackEntryCount() <= 1) {
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
                    logger.trace("Navigation back: On step back from {} to location {}",
                            event.getLastValue() != null ? event.getLastValue().getLocationId() : null,
                            currentLoc.getLocationId());
                }
            }
        }
    }
}
