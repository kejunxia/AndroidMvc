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

package com.shipdream.lib.android.mvc.samples.simple.mvp.controller;

import com.shipdream.lib.android.mvc.NavLocation;
import com.shipdream.lib.android.mvc.NavigationManager;
import com.shipdream.lib.android.mvc.Reason;
import com.shipdream.lib.android.mvc.UiView;
import com.shipdream.lib.android.mvc.samples.simple.mvp.manager.AppManager;

import javax.inject.Inject;

public class AppDelegateController extends AbstractController<Void, AppDelegateController.View> {
    /**
     * Define the interface of the controller's view
     */
    public interface View extends UiView {
        void updateTitle(String title);
        void changeNavIcon(boolean showBackArrow);
    }

    @Inject
    private AppManager appManager;

    @Inject
    private NavigationManager navigationManager;

    @Override
    public Class modelType() {
        return null;
    }

    @Override
    public void onViewReady(Reason reason) {
        super.onViewReady(reason);
        view.updateTitle(appManager.getModel().getTitle());
        doUpdateNavIcon(navigationManager.getModel().getCurrentLocation());

    }

    public void startApp(Object sender) {
        navigationManager.navigate(sender).to(CounterMasterController.class);
    }

    public void navigateBack(Object sender) {
        navigationManager.navigate(sender).back();
    }

    /**
     * Subscribe to event when app manager updates current page's title
     * @param event
     */
    private void onEvent(AppManager.Event.OnTitleUpdated event) {
        view.updateTitle(event.getTitle());
    }

    /**
     * Subscribe to forward navigation
     * @param event
     */
    private void onEvent(NavigationManager.Event.OnLocationForward event) {
        updateToolbar(event.getCurrentValue());
    }

    /**
     * Subscribe to backward navigation
     * @param event
     */
    private void onEvent(NavigationManager.Event.OnLocationBack event) {
        updateToolbar(event.getCurrentValue());
    }

    private void updateToolbar(final NavLocation location) {
        uiThreadRunner.post(new Runnable() {
            @Override
            public void run() {
                doUpdateNavIcon(location);
            }
        });
    }

    private void doUpdateNavIcon(NavLocation location) {
        if (location != null) {
            view.changeNavIcon(location.getPreviousLocation() != null);
        } else {
            view.changeNavIcon(false);
        }
    }
}
