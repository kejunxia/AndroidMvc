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

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.shipdream.lib.android.mvc.event.BaseEventV2V;

/**
 * This dialog fragment can either use {@link AlertDialog.Builder} to build a alert dialog or use
 * {@link MvcFragment} as an nested fragment for custom view dialog, as a result the underlying
 * doesn't need to be designed with awareness how it is going to be used and can be reused as a
 * normal fragment as well. This class is FINAL and don't extend this class to custom your dialog.
 * <p/>
 */
public class MvcDialogFragment extends DialogFragment {
    private EventRegister eventRegister;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getParentFragment() == null) {
            setRetainInstance(true);
        }

        AndroidMvc.graph().inject(this);

        eventRegister = new EventRegister(this);
        eventRegister.registerEventBuses();
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
        eventRegister.unregisterEventBuses();
        AndroidMvc.graph().release(this);
    }

    /**
     * Post an event from this view to other views
     * @param event The event
     */
    protected void postEventV2V(BaseEventV2V event) {
        AndroidMvc.getEventBusC2V().post(event);
    }

}
