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

package com.shipdream.lib.android.mvc.samples.note.view.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.shipdream.lib.android.mvc.NavLocation;
import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.android.mvc.samples.note.LocId;
import com.shipdream.lib.android.mvc.samples.note.R;
import com.shipdream.lib.android.mvc.samples.note.controller.AppController;
import com.shipdream.lib.android.mvc.view.MvcActivity;

import javax.inject.Inject;

public class MainFragment extends MvcActivity.DelegateFragment {
    @Inject
    AppController appController;
    private DrawerLayout drawerLayout;
    private ViewGroup mainContainer;
    private ViewGroup navContainer;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private View navHome;
    private View navWeather;
    @Inject
    private NavigationController navigationController;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_main_drawer_over_toolbar;
    }

    @Override
    protected int getContentLayoutResId() {
        return R.id.main_container;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_container);
        mainContainer = (ViewGroup) view.findViewById(R.id.main_container);
        navContainer = (ViewGroup) view.findViewById(R.id.nav_container);
        drawerToggle = new ActionBarDrawerToggle(this.getActivity(), drawerLayout,
                R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(drawerToggle);

        navHome = navContainer.findViewById(R.id.nav_item_home);
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Note the 3rd parameter which indicates navigating to locations on main menu will
                 * clear all history where back key will exit the app
                 */
                navigationController.navigateTo(v, LocId.NOTE_HANDSET_LIST, null);
                drawerLayout.closeDrawers();
            }
        });

        navWeather = navContainer.findViewById(R.id.nav_item_weather);
        navContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Note the 3rd parameter which indicates navigating to locations on main menu will
                 * clear all history where back key will exit the app
                 */
                navigationController.navigateTo(v, LocId.WEATHERS, null);
                drawerLayout.closeDrawers();
            }
        });

        updateNavigationUi();

        //When app starts or restore, notify the app controller the original orientation
        switch (reason) {
            case FIRST_TIME:
                appController.navigateToInitialLocation();
                break;
            case RESTORE:
                appController.notifyOrientationChanged(
                        convertOrientation(Configuration.ORIENTATION_UNDEFINED),
                        convertOrientation(getCurrentOrientation()));
                break;
        }
    }

    @Override
    protected void onStartUp() {

    }

    @Override
    protected void onOrientationChanged(int lastOrientation, int currentOrientation) {
        super.onOrientationChanged(lastOrientation, currentOrientation);
        appController.notifyOrientationChanged(
                convertOrientation(lastOrientation), convertOrientation(currentOrientation));
    }

    private AppController.Orientation convertOrientation(int orientation) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return AppController.Orientation.PORTRAIT;
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return AppController.Orientation.LANDSCAPE;
        } else {
            return AppController.Orientation.UNSPECIFIED;
        }
    }

    @Override
    public void onEvent(NavigationController.EventC2V.OnLocationForward event) {
        super.onEvent(event);
        updateNavigationUi();
    }

    @Override
    public void onEvent(NavigationController.EventC2V.OnLocationBack event) {
        super.onEvent(event);
        updateNavigationUi();
    }

    private void updateNavigationUi() {
        NavLocation curLoc = navigationController.getModel().getCurrentLocation();
        if (curLoc != null && curLoc.getPreviousLocation() != null) {
            //Has history location,should show back nav icon
            toolbar.setNavigationIcon(R.drawable.ic_action_navigation_back_menu);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackButtonPressed();
                }
            });
        } else {
            //Show 3 bars
            toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            });
        }

    }
}
