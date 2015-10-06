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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shipdream.lib.android.mvc.event.BaseEventV2V;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

/**
 * Fragment to help utilize MVC pattern. {@link #setRetainInstance(boolean)} will be set true by
 * default. Don't set it false which will result unexpected behaviour and life cycles. Controllers
 * and other dependencies can be injected by fields annotated by @{@link Inject}. See
 * {@link AndroidMvc} to check out how to do the injection.
 *
 * <p>
 * This fragment uses life cycles slightly different from original Android Fragment.
 * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} is sealed to be overridden which will
 * use the layout provided by {@link #getLayoutResId()} to inflate the view of the fragment.
 * {@link #onViewCreated(View, Bundle)} is sealed too. Instead use {@link #onViewReady(View, Bundle, Reason)}
 * with an extra flag to indicate the {@link Reason}  why the onViewReady is called. Override
 * {@link #onViewReady(View, Bundle, Reason)} to setup views and bind data where all dependencies
 * and restored state will be guaranteed ready.
 * </p>
 *
 * <p>
 * If some actions need to be delayed to invoke after the fragment is ready, use {@link #registerOnViewReadyListener(Runnable)}
 * For example, when the fragment is just instantiated before it's inflated and added to view
 * hierarchy, the fragment is not in ready state to be interacted. In this case, register to run the
 * action after the first {@link #onViewReady(View, Bundle, Reason)} lifecycle of the fragment
 * </p>
 */
public abstract class MvcFragment extends Fragment {
    /**
     * Reason of creating the view of the fragment
     */
    public enum Reason {
        /**
         * The view of the fragment is newly created for the first time
         */
        FIRST_TIME,
        /**
         * The view of the fragment is recreated due to rotation
         */
        ROTATE,
        /**
         * The view of the fragment is recreated on restoration after the activity of the fragment
         * is killed and recreated by the OS. Note that even there is an orientation change along
         * with the restoration only the reason will still be RESTORE.
         */
        RESTORE
    }

    private final static String STATE_LAST_ORIENTATION = AndroidMvc.MVC_SATE_PREFIX + "LastOrientation--__";
    private EventRegister eventRegister;
    private CopyOnWriteArrayList<Runnable> onViewReadyListeners;
    private boolean fragmentComesBackFromBackground = false;
    private int lastOrientation;
    private boolean dependenciesInjected = false;

    boolean isStateManagedByRootDelegateFragment = false;

    /**
     * @return orientation before last orientation change.
     */
    protected int getLastOrientation() {
        return lastOrientation;
    }

    /**
     * @return current orientation
     */
    protected int getCurrentOrientation() {
        return getResources().getConfiguration().orientation;
    }

    /**
     * Assign the layout xml of the <strong>Root View</strong> for this fragment. The layout will be
     * inflated in {@link #onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}..
     * <p><strong>Also see</strong> {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * </p>
     *
     * @return The id of the fragment layout
     */
    protected abstract int getLayoutResId();

    void injectDependencies() {
        if (!dependenciesInjected) {
            AndroidMvc.graph().inject(this);
            dependenciesInjected = true;
        }
    }

    void releaseDependencies() {
        if (dependenciesInjected) {
            AndroidMvc.graph().release(this);
            dependenciesInjected = false;
        }
    }

    /**
     * Called when the fragment is about to create. Fields annotated by {@link Inject} will be
     * injected in this method.
     * </p>
     * Event bus will be registered in this method
     * <p/>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            lastOrientation = getResources().getConfiguration().orientation;
        } else {
            lastOrientation = savedInstanceState.getInt(STATE_LAST_ORIENTATION);
        }

        if (getParentFragment() == null) {
            setRetainInstance(true);
        }
        injectDependencies();

        if (savedInstanceState != null && !isStateManagedByRootDelegateFragment) {
            AndroidMvc.restoreControllerStateByTheirOwn(savedInstanceState, this);
        }
    }

    /**
     * This Android lifecycle callback is sealed. {@link MvcFragment} will always use the
     * layout returned by {@link #getLayoutResId()} to inflate the view. Instead, do actions to
     * prepare views in {@link #onViewReady(View, Bundle, Reason)} where all injected dependencies
     * and all restored state will be ready to use.
     *
     * @param inflater The inflater
     * @param container The container
     * @param savedInstanceState The savedInstanceState
     * @return The view for the fragment
     */
    @Override
    final public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        injectDependencies();
        return inflater.inflate(getLayoutResId(), container, false);
    }

    /**
     * This Android lifecycle callback is sealed. Use {@link #onViewReady(View, Bundle, Reason)}
     * instead which provides a flag to indicate if the creation of the view is caused by rotation.
     *
     * @param view View of this fragment
     * @param savedInstanceState The savedInstanceState: Null when the view is newly created,
     *                           otherwise the state to restore and recreate the view
     */
    @Override
    final public void onViewCreated(final View view, final Bundle savedInstanceState) {
        fragmentComesBackFromBackground = false;
        eventRegister = new EventRegister(this);
        eventRegister.registerEventBuses();

        final boolean restoring = savedInstanceState != null;
        if (restoring && isStateManagedByRootDelegateFragment) {
            ((MvcActivity)getActivity()).addPendingOnViewReadyActions(new Runnable() {
                @Override
                public void run() {
                    doOnViewCreatedCallBack(view, savedInstanceState, restoring);
                    isStateManagedByRootDelegateFragment = false;
                }
            });
        } else {
            doOnViewCreatedCallBack(view, savedInstanceState, restoring);
        }
    }

    private void doOnViewCreatedCallBack(View view, Bundle savedInstanceState, boolean restoring) {
        int currentOrientation = getResources().getConfiguration().orientation;
        boolean orientationChanged = currentOrientation != lastOrientation;
        Reason reason;
        if (restoring) {
            reason = Reason.RESTORE;
        } else {
            if (orientationChanged) {
                reason = Reason.ROTATE;
            } else {
                reason = Reason.FIRST_TIME;
            }
        }

        onViewReady(view, savedInstanceState, reason);
        if (onViewReadyListeners != null) {
            for (Runnable r : onViewReadyListeners) {
                r.run();
            }
        }

        if (orientationChanged) {
            onOrientationChanged(lastOrientation, getResources().getConfiguration().orientation);
        }

        lastOrientation = currentOrientation;
    }

    /**
     * Called when the view of the fragment is ready to use. This also replaces Android lifecycle -
     * {@link #onViewCreated(View, Bundle)} and provide an extra flag to indicate the {@link Reason}
     * why this callback is invoked.
     *
     * <p>Note: When savedInstanceState is non-null, causedByRotation will <b>always be false</b> as the
     * recreation is not just caused by rotation but isStateManagedByRootDelegateFragment the view killed by OS, since the
     * fragment retains instance.</p>
     * @param view The root view of the fragment
     * @param savedInstanceState The savedInstanceState when the fragment is being recreated after
     *                           its enclosing activity is killed by OS, otherwise null including on
     *                           rotation
     * @param reason Indicates the {@link Reason} why the onViewReady is called.
     */
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
    }

    @Override
    public void onResume() {
        super.onResume();
        checkWhetherReturnFromForeground();
    }

    private void checkWhetherReturnFromForeground() {
        if(fragmentComesBackFromBackground) {
            onReturnForeground();
        }
        fragmentComesBackFromBackground = false;
    }

    /**
     * Called when the fragment resumes without a new view being created. For example, press home
     * button and then bring the app back foreground without rotation or being killed by OS.
     * This method is called after {@link #onResume}
     */
    protected void onReturnForeground() {
    }

    /**
     * Called before this fragment will be replaced by new fragment and being pushed to fragment
     * back stack.
     */
    protected void onPushingToBackStack() {
    }

    /**
     * Called when this fragment is popped out from fragment back stack. This callback will be
     * invoked after {@link #onViewReady(View, Bundle, Reason)}.
     */
    protected void onPoppedOutToFront() {
    }

    /**
     * Called after {@link #onViewReady(View, Bundle, Reason)} when orientation changed.
     *
     * @param lastOrientation Orientation before rotation
     * @param currentOrientation Current orientation
     */
    protected void onOrientationChanged(int lastOrientation, int currentOrientation) {
    }

    @Override
    public void onPause() {
        super.onPause();
        fragmentComesBackFromBackground = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        eventRegister.unregisterEventBuses();
        eventRegister = null;
    }

    /**
     * Called when the fragment is no longer in use. This is called after onStop() and before onDetach().
     * Event bus will be unregistered in the method.
     *
     * <p><b>Note that, when a new fragment to create and pushes this fragment to back stack,
     * onDestroy of this fragment will NOT be called. This method will be called until this fragment
     * is removed completely.</b></p>
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseDependencies();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_LAST_ORIENTATION, lastOrientation);

        if (!isStateManagedByRootDelegateFragment) {
            AndroidMvc.saveControllerStateOfTheirOwn(outState, this);
        }
    }

    /**
     * Register a callback after {@link #onViewReady(View, Bundle, Reason)} is called. This is useful when an action that
     * can't be executed after the fragment is instantiated but before the fragment has gone through
     * life cycles and gets created and ready to use. If this is one time action, use
     * {@link #unregisterOnViewReadyListener(Runnable)} (Runnable)}  unregister itself in the given onCreateViewAction.
     *
     * @param onCreateViewAction The action to register
     */
    public void registerOnViewReadyListener(Runnable onCreateViewAction) {
        if (onViewReadyListeners == null) {
            onViewReadyListeners = new CopyOnWriteArrayList<>();
        }
        onViewReadyListeners.add(onCreateViewAction);
    }

    /**
     * Unregister the callback that to be called in {@link #onViewCreated(View, Bundle)}
     *
     * @param onResumeAction The action to run in onResume callback
     */
    public void unregisterOnViewReadyListener(Runnable onResumeAction) {
        if (onViewReadyListeners != null) {
            onViewReadyListeners.remove(onResumeAction);
        }
    }

    /**
     * Unregister callbacks that to be called after {@link #onViewReady(View, Bundle, Reason)}
     */
    public void clearOnViewReadyListener() {
        if (onViewReadyListeners != null) {
            onViewReadyListeners.clear();
        }
    }

    /**
     * Overrides this method when this fragment needs to handle its own business on back button
     * pressed. If this fragment wants to intercept and swallow the back button pressed event, return
     * true, otherwise return false. In other words, when false is returned, the fragment will be
     * dismissed, otherwise only the logic in this method will be executed but the fragment remains
     * in the front.
     *
     * @return True to consume the back button pressed event, otherwise returns false which will
     * forward the back button pressed event to other views
     */
    public boolean onBackButtonPressed() {
        return false;
    }

    /**
     * Post an event from this view to other views
     * @param event The view to view event
     */
    protected void postEventV2V(BaseEventV2V event) {
        AndroidMvc.getEventBusV2V().post(event);
    }
}
