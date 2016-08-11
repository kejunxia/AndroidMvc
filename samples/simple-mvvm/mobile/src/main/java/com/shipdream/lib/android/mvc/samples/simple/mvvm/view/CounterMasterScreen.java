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

package com.shipdream.lib.android.mvc.samples.simple.mvvm.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.R;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.controller.CounterMasterController;
import com.shipdream.lib.android.mvc.samples.simple.mvvm.databinding.FragmentCounterMasterBinding;

import javax.inject.Inject;

public class CounterMasterScreen extends AbstractFragment<CounterMasterController> {

    @Inject
    private NavigationManager navigationManager;

    private TextView display;
    private TextView ipValue;
    private ProgressBar ipProgress;
    private ImageView ipRefresh;
    private Button increment;
    private Button decrement;
    private Button buttonGoToDetailScreen;

    @Override
    protected Class<CounterMasterController> getControllerClass() {
        return CounterMasterController.class;
    }

    /**
     * @return Layout id used to inflate the view of this MvcFragment.
     */
    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_counter_master;
    }

    private FragmentCounterMasterBinding binding;
    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        binding = DataBindingUtil.bind(view);
        binding.setController(controller);
        binding.setModel(controller.getModel());

        if (reason.isFirstTime()) {
            CounterMasterInsideView f = new CounterMasterInsideView();

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_a_anotherFragmentContainer, f).commit();
        }
    }


    @Override
    public void update() {
    }

    private void onEvent(CounterMasterController.Event.OnHttpError event) {
        Toast.makeText(getContext(), String.format("Http error(%d): %s", event.getStatusCode(), event.getMessage()),
                Toast.LENGTH_SHORT).show();
    }

    private void onEvent(CounterMasterController.Event.OnNetworkError event) {
        Toast.makeText(getContext(), String.format("Network error: %s", event.getIoException().getMessage())
                , Toast.LENGTH_SHORT).show();
    }

}
