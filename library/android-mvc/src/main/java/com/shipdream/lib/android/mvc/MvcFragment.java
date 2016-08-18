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

package com.shipdream.lib.android.mvc;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shipdream.lib.poke.Graph;
import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

/**
 * Fragment to help utilize Mvc pattern. {@link #setRetainInstance(boolean)} will be set true by
 * default. Don't set it false which will result unexpected behaviour and life cycles. Controllers
 * and other dependencies can be injected by fields annotated by @{@link Inject}.
 * <p>
 * <p>
 * This fragment uses life cycles slightly different from original Android Fragment.
 * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} is sealed to be overridden which will
 * use the layout provided by {@link #getLayoutResId()} to inflate the view of the fragment.
 * {@link #onViewCreated(View, Bundle)} is sealed too. Instead use {@link #onViewReady(View, Bundle, Reason)}
 * with an extra flag to indicate the {@link Reason}  why the onViewReady is called. Override
 * {@link #onViewReady(View, Bundle, Reason)} to setup views and bind data where all dependencies
 * and restored state will be guaranteed ready.
 * </p>
 * <p>
 * <p>
 * If some actions need to be delayed to invoke after the fragment is ready, use {@link #registerOnViewReadyListener(Runnable)}
 * For example, when the fragment is just instantiated before it's inflated and added to view
 * hierarchy, the fragment is not in ready state to be interacted. In this case, register to run the
 * action after the first {@link #onViewReady(View, Bundle, Reason)} lifecycle of the fragment
 * </p>
 */
public abstract class MvcFragment<CONTROLLER extends FragmentController> extends Fragment implements UiView {
    private final static String STATE_LAST_ORIENTATION = MvcActivity.STATE_PREFIX + "LastOrientation--__";
    private EventRegister eventRegister;
    private CopyOnWriteArrayList<Runnable> onViewReadyListeners;
    private boolean fragmentComesBackFromBackground = false;
    private int lastOrientation;
    private boolean dependenciesInjected = false;
    private Object newInstanceChecker;
    boolean isStateManagedByRootDelegateFragment;
    protected CONTROLLER controller;
    private Graph.Monitor graphMonitor;

    /**
     *
     * Specify the class type of the {@link FragmentController} for this fragment. It's recommended
     * every fragment has a paired controller to deal with the fragment's model and business logic.
     * If the fragment doesn't need a controller simply returns null and {@link MvcFragment#controller}
     * will be null. So it this method returns null, be cautious not to use {@link MvcFragment#controller}.
     *
     * <p/>
     * The fragment will instantiate the {@link FragmentController} by this class type. The
     * instantiated controller will get its fragment lifecycle called automatically by this fragment.
     * @return The class type of the controller
     *
     */
    protected abstract Class<CONTROLLER> getControllerClass();

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
     * inflated in {@link #onCreateView(android.view.LayoutInflater, ViewGroup, android.os.Bundle)}..
     * <p><strong>Also see</strong> {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * </p>
     *
     * @return The id of the fragment layout
     */
    protected abstract int getLayoutResId();

    private void injectDependencies() {
        if (!dependenciesInjected) {
            if (getControllerClass() != null) {
                try {
                    controller = Mvc.graph().reference(getControllerClass(), null);
                } catch (PokeException e) {
                    throw new IllegalStateException("Unable to inject "
                            + getControllerClass().getName() + ".\n" + e.getMessage(), e);
                }
            }

            Mvc.graph().inject(this);
            dependenciesInjected = true;
        }
    }

    private void releaseDependencies() {
        if (dependenciesInjected) {
            if (getControllerClass() != null) {
                try {
                    Mvc.graph().dereference(controller, getControllerClass(), null);
                } catch (ProviderMissingException e) {
                    //should never happen
                    Logger logger = LoggerFactory.getLogger(getClass());
                    logger.warn("Failed to dereference controller " + getControllerClass().getName(), e);
                }
            }

            Mvc.graph().release(this);
            dependenciesInjected = false;
        }
    }

    /**
     * Called when the fragment is about to create. Fields annotated by {@link Inject} will be
     * injected in this method.
     * </p>
     * Event2C bus will be registered in this method
     * <p/>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        graphMonitor = new Graph.Monitor() {
            @Override
            public void onInject(Object target) {
                if (controller != null && target == MvcFragment.this) {
                    controller.view = MvcFragment.this;
                }
            }

            @Override
            public void onRelease(Object target) {
            }
        };
        Mvc.graph().registerMonitor(graphMonitor);

        eventRegister = new EventRegister(this);

        if (savedInstanceState == null) {
            lastOrientation = getResources().getConfiguration().orientation;
        } else {
            lastOrientation = savedInstanceState.getInt(STATE_LAST_ORIENTATION);
        }

        if (getParentFragment() == null) {
            setRetainInstance(true);
        }

        injectDependencies();
    }

    /**
     * This Android lifecycle callback is sealed. {@link MvcFragment} will always use the
     * layout returned by {@link #getLayoutResId()} to inflate the view. Instead, do actions to
     * prepare views in {@link #onViewReady(View, Bundle, Reason)} where all injected dependencies
     * and all restored state will be ready to use.
     *
     * @param inflater           The inflater
     * @param container          The container
     * @param savedInstanceState The savedInstanceState
     * @return The view for the fragment
     */
    @Override
    final public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        injectDependencies();
        return inflater.inflate(getLayoutResId(), container, false);
    }

    /**
     * Called when view is created by before {@link #onViewReady(View, Bundle, Reason)} is called
     */
    void onPreViewReady(View view, Bundle savedInstanceState) {
    }

    /**
     * This Android lifecycle callback is sealed. Use {@link #onViewReady(View, Bundle, Reason)}
     * instead, which provides a flag to indicate why the view is created.
     *
     * @param view               View of this fragment
     * @param savedInstanceState The savedInstanceState: Null when the view is newly created,
     *                           otherwise the state to restore and recreate the view
     */
    @Override
    final public void onViewCreated(final View view, final Bundle savedInstanceState) {
        fragmentComesBackFromBackground = false;
        eventRegister.registerEventBuses();

        onPreViewReady(view, savedInstanceState);

        final boolean restoring = savedInstanceState != null;
        if (restoring && isStateManagedByRootDelegateFragment) {
            ((MvcActivity) getActivity()).addPendingOnViewReadyActions(new Runnable() {
                @Override
                public void run() {
                    doOnViewCreatedCallBack(view, savedInstanceState, restoring);
                }
            });
        } else {
            doOnViewCreatedCallBack(view, savedInstanceState, restoring);
        }
    }

    private void doOnViewCreatedCallBack(View view, Bundle savedInstanceState, boolean restoring) {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (controller != null) {
            controller.orientation = parseOrientation(currentOrientation);
        }

        boolean orientationChanged = currentOrientation != lastOrientation;
        Reason reason = new Reason();

        if (newInstanceChecker == null) {
            newInstanceChecker = new Object();
            reason.isNewInstance = true;
        } else {
            reason.isNewInstance = false;
        }

        if (orientationChanged) {
            reason.isRotated = true;
        }

        if (restoring) {
            reason.isRestored = !reason.isRotated;
        } else if (!orientationChanged && !aboutToPopOut) {
            //When the view is created not by orientation change nor popping out from back stack
            reason.isFirstTime = !reason.isRotated;
        }

        if (aboutToPopOut) {
            reason.isPoppedOut = true;
            aboutToPopOut = false;
        }

        onViewReady(view, savedInstanceState, reason);

        if (reason.isPoppedOut()) {
            onPoppedOutToFront();
        }

        if (orientationChanged) {
            onOrientationChanged(lastOrientation, getResources().getConfiguration().orientation);
        }

        lastOrientation = currentOrientation;

        if (onViewReadyListeners != null) {
            for (Runnable r : onViewReadyListeners) {
                r.run();
            }
        }

        if (controller != null) {
            controller.onViewReady(reason);
        }

        update();
    }

    /**
     * Called when the view of the fragment is ready to use. This also replaces Android lifecycle -
     * {@link #onViewCreated(View, Bundle)} and provide an extra flag to indicate the {@link Reason}
     * why this callback is invoked. This callback also ensure all injected instanced are fully
     * injected which means their own injectable fields are injected as well.
     *
     * @param view               The root view of the fragment
     * @param savedInstanceState The savedInstanceState when the fragment is being recreated after
     *                           its enclosing activity is killed by OS, otherwise null including on
     *                           rotation
     * @param reason             Indicates the {@link Reason} why the onViewReady is called.
     */
    protected void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
    }

    @Override
    public void onResume() {
        super.onResume();
        checkWhetherReturnFromForeground();
        if (controller != null) {
            controller.onResume();
        }
    }

    private void checkWhetherReturnFromForeground() {
        if (fragmentComesBackFromBackground) {
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
        if (controller != null) {
            controller.onReturnForeground();
        }
    }

    /**
     * Called before this fragment is about to be replaced by new fragment and being pushed to fragment
     * back stack.
     */
    protected void onPushToBackStack() {
        if (controller != null) {
            controller.onPushToBackStack();
        }
    }

    /**
     * <p>
     * Called when this fragment is popped out from fragment back stack and will become the top most
     * fragment and present to user. This callback will be invoked after {@link #onViewReady(View, Bundle, Reason)}.
     * </p>
     *
     * <p>
     * For example, current navigation history is A->B->C, when navigate back. The C will be popped
     * out. At this moment,
     * <ul>
     *     <li>C.onPopAway() will be called</li>
     *     <li>B.onPoppedOutToFront will be called</li>
     * </ul>
     * </p>
     */
    protected void onPoppedOutToFront() {
        if (controller != null) {
            controller.onPoppedOutToFront();
        }
    }

    /**
     *
     * <p>
     * Called when the fragment was the top most presenting fragment and will be removed from the
     * fragment back stack and replaced by the fragment under it from the back stack.
     * </p>
     *
     * <p>
     * For example, current navigation history is A->B->C, when navigate back. The C will be popped
     * out. At this moment,
     * <ul>
     *     <li>C.onPopAway() will be called</li>
     *     <li>B.onPoppedOutToFront will be called</li>
     * </ul>
     * </p>
     */
    protected void onPopAway() {
        if (controller != null) {
            controller.onPopAway();
        }
    }

    /**
     * Called when {@link NavigationManager#navigate(Object)} is invoked and this fragment is acting
     * as a navigable page. This fragment will be replaced by the nextFragment. This method is called
     * right before the transaction is committed. This is the ideal place to
     * {@link FragmentTransaction#addSharedElement(View, String)}.
     *
     * @param transaction  The transaction being committing
     * @param nextFragment Next fragment is going to
     */
    protected void onPreNavigationTransaction(FragmentTransaction transaction, MvcFragment nextFragment) {
    }

    boolean aboutToPopOut = false;

    /**
     * Called after {@link #onViewReady(View, Bundle, Reason)} when orientation changed.
     *
     * @param lastOrientation    Orientation before rotation
     * @param currentOrientation Current orientation
     */
    protected void onOrientationChanged(int lastOrientation, int currentOrientation) {
        if (controller != null) {
            controller.onOrientationChanged(
                    parseOrientation(lastOrientation),
                    parseOrientation(currentOrientation));
        }
    }

    private static Orientation parseOrientation(int androidOrientation) {
        if (androidOrientation == Configuration.ORIENTATION_PORTRAIT) {
            return Orientation.PORTRAIT;
        } else if (androidOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            return Orientation.LANDSCAPE;
        } else {
            return Orientation.UNSPECIFIED;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        fragmentComesBackFromBackground = true;
        if (controller != null) {
            controller.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        eventRegister.unregisterEventBuses();
    }

    /**
     * Called when the fragment is no longer in use. This is called after onStop() and before onDetach().
     * Event2C bus will be unregistered in the method.
     * <p>
     * <p><b>Note that, when a new fragment to create and pushes this fragment to back stack,
     * onDestroy of this fragment will NOT be called. This method will be called until this fragment
     * is removed completely.</b></p>
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        releaseDependencies();

        Mvc.graph().unregisterMonitor(graphMonitor);
        eventRegister = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_LAST_ORIENTATION, lastOrientation);
    }

    /**
     * Register a callback after {@link #onViewReady(View, Bundle, Reason)} is called. This is useful when an action that
     * can't be executed after the fragment is instantiated but before the fragment has gone through
     * life cycles and gets created and ready to use. If this is one time action, use
     * {@link #unregisterOnViewReadyListener(Runnable)} (Runnable)}  unregister itself in the given onCreateViewAction.
     *
     * @param action The action to registered to be run after view is ready
     */
    public void registerOnViewReadyListener(Runnable action) {
        if (onViewReadyListeners == null) {
            onViewReadyListeners = new CopyOnWriteArrayList<>();
        }
        onViewReadyListeners.add(action);
    }

    /**
     * Unregister the callback that to be called in {@link #onViewReady(View, Bundle, Reason)}
     *
     * @param action The action to unregistered
     */
    public void unregisterOnViewReadyListener(Runnable action) {
        if (onViewReadyListeners != null) {
            onViewReadyListeners.remove(action);
        }
    }

    /**
     * Unregister all actions to be called after {@link #onViewReady(View, Bundle, Reason)}
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
     * <p>By default, if this fragment has a corresponding controller it delegates the call to
     * {@link FragmentController#onBackButtonPressed()} otherwise returns false.</p>
     *
     * @return True to consume the back button pressed event, otherwise returns false which will
     * forward the back button pressed event to other views
     */
    public boolean onBackButtonPressed() {
        if (controller != null) {
            return controller.onBackButtonPressed();
        } else {
            return false;
        }
    }

    /**
     * Handy method to post an event to other views directly. However, when possible, it's
     * recommended to post events from controllers to views.
     *
     * @param event The event send to other views
     */
    protected void postEvent2V(Object event) {
        eventRegister.postEvent2V(event);
    }

}
