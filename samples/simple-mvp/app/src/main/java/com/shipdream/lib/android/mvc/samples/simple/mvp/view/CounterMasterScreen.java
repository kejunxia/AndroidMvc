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

package com.shipdream.lib.android.mvc.samples.simple.mvp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.samples.simple.mvp.R;
import com.shipdream.lib.android.mvc.samples.simple.mvp.controller.CounterMasterController;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class CounterMasterScreen extends AbstractFragment<CounterMasterController>
        implements CounterMasterController.View{

    @Inject
    private NavigationManager navigationManager;

    @BindView(R.id.screen_master_counterDisplay)
    TextView display;
    @BindView(R.id.fragment_master_ipValue)
    TextView ipValue;
    @BindView(R.id.fragment_master_ipProgress)
    ProgressBar ipProgress;

    @Override
    protected Class<CounterMasterController> getControllerClass() {
        return CounterMasterController.class;
    }

    /**
     * @return Layout id used to inflate the view of this MvcFragment.
     */
    @Override
    protected int getLayoutResId() {
        return R.layout.screen_master;
    }

    @OnClick(R.id.screen_master_buttonIncrement)
    void increment(View v) {
        controller.increment(v);
    }

    @OnClick(R.id.screen_master_buttonDecrement)
    void decrement(View v) {
        controller.decrement(v);
    }

    @OnClick(R.id.fragment_master_buttonShowDetailScreen)
    void goToDetailPage(View v) {
        controller.goToDetailScreen(v);
    }

    @OnClick(R.id.fragment_master_ipRefresh)
    void refreshIp(){
        controller.refreshIp();
    }

    /**
     * Lifecycle similar to onViewCreated by with more granular control with an extra argument to
     * indicate why this view is created: 1. first time created, or 2. rotated or 3. restored
     * @param view The root view of the fragment
     * @param savedInstanceState The savedInstanceState when the fragment is being recreated after
     *                           its enclosing activity is killed by OS, otherwise null including on
     *                           rotation
     * @param reason Indicates the {@link Reason} why the onViewReady is called.
     */
    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        if (reason.isFirstTime()) {
            CounterMasterInsideView f = new CounterMasterInsideView();

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.screen_master_anotherFragmentContainer, f).commit();
        }

    }

    @Override
    public void update() {
        display.setText(controller.getModel().getCount());
    }

    @Override
    public void showProgress() {
        ipProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        ipProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void updateIpValue(String ip) {
        ipValue.setVisibility(View.VISIBLE);
        ipValue.setText(ip);
    }

    @Override
    public void showHttpError(int statusCode, String message) {
        Toast.makeText(getContext(), String.format("Http error(%d): %s", statusCode, message),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNetworkError(IOException e) {
        Toast.makeText(getContext(), R.string.network_error_to_get_ip
                , Toast.LENGTH_SHORT).show();
    }

}
