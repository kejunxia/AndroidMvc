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

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatDialogFragment;

import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusV;
import com.shipdream.lib.poke.Graph;
import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.util.ReflectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Abstract class for dialogs. Don't use it as a normal fragment but only for Dialog. To share logic
 * for similar dialog and fragment use shared controller.
 * @param <CONTROLLER> The class type for the controller of this dialog
 */
public abstract class MvcDialog<CONTROLLER extends Controller> extends AppCompatDialogFragment
        implements UiView {
    @Inject
    @EventBusV
    private EventBus eventBusV;

    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected CONTROLLER controller;
    protected abstract Class<CONTROLLER> getControllerClass();
    private Graph.Monitor graphMonitor;

    /**
     * Show dialog.
     * @param fragmentManager The fragment manager. Usually it's the child fragment manager of the
     *                        fragment on which the dialog will show
     * @param dialogClass The class type of the dialog extending {@link MvcDialog}
     */
    public static void show(FragmentManager fragmentManager, Class<? extends MvcDialog> dialogClass) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        MvcDialog dialogFragment = (MvcDialog) fragmentManager.findFragmentByTag(dialogClass.getName());
        if (dialogFragment == null) {
            try {
                dialogFragment = new ReflectUtils.newObjectByType<>(dialogClass).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ft.addToBackStack(null);
        dialogFragment.show(ft, dialogClass.getName());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        graphMonitor = new Graph.Monitor() {
            @Override
            public void onInject(Object target) {
                if (controller != null && target == MvcDialog.this) {
                    controller.view = MvcDialog.this;
                }
            }

            @Override
            public void onRelease(Object target) {
            }
        };
        Mvc.graph().registerMonitor(graphMonitor);

        if (getParentFragment() == null) {
            setRetainInstance(true);
        }

        try {
            controller = Mvc.graph().reference(getControllerClass(), null);
        } catch (PokeException e) {
            logger.error(e.getMessage(), e);
        }
        Mvc.graph().inject(this);

        eventBusV.register(this);
    }

    @Override
    public void onDestroyView() {
        //======================================
        //workaround of this bug
        //https://code.google.com/p/android/issues/detail?id=17423
        if (getDialog() != null && (getParentFragment() != null || getRetainInstance())) {
            getDialog().setDismissMessage(null);
        }
        //============================================
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Mvc.graph().unregisterMonitor(graphMonitor);
        eventBusV.register(this);
        Mvc.graph().release(this);
        try {
            Mvc.graph().dereference(controller, getControllerClass(), null);
        } catch (PokeException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Handy method to post an event to other views directly. However, when possible, it's
     * recommended to post events from controllers to views to keep views' logic simple.
     * @param event
     */
    protected void postEvent2V(Object event) {
        eventBusV.post(event);
    }

}
