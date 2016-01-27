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

package com.shipdream.lib.android.mvc.samples.note.controller.internal;

import com.shipdream.lib.android.mvc.manager.NavigationManager;
import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.samples.note.LocId;
import com.shipdream.lib.android.mvc.samples.note.controller.AppController;
import com.shipdream.lib.android.mvc.samples.note.manager.NoteManager;
import com.shipdream.lib.android.mvc.samples.note.model.NoteModel;

import javax.inject.Inject;

public class AppControllerImpl extends BaseControllerImpl implements AppController {
    private Orientation currentOrientation;

    @Inject
    private NavigationManager navigationManager;

    @Inject
    private NoteManager noteManager;

    @Override
    public Class modelType() {
        return null;
    }

    @Override
    public void startApp(Object sender) {
        //Login a dump user. This should be done by a login api in real app.
        if(navigationManager.getModel().getCurrentLocation() == null) {
            navigationManager.navigate(this).to(LocId.NOTE_HANDSET_LIST);
        }
    }

    @Override
    public void notifyOrientationChanged(Orientation lastOrientation, Orientation currentOrientation) {
        if(lastOrientation != currentOrientation) {
            switch(currentOrientation) {
                case PORTRAIT:
                    showPortrait();
                    break;
                case LANDSCAPE:
                    showLandscape();
                    break;
            }
        }
        this.currentOrientation = currentOrientation;
    }

    @Override
    public Orientation getCurrentOrientation() {
        return currentOrientation;
    }

    private void onEvent(NavigationManager.Event2C.OnLocationForward event) {
        postEvent2V(new Event2V.OnForwardNavigation(event.getSender()));
    }

    private void onEvent(NavigationManager.Event2C.OnLocationBack event) {
        postEvent2V(new Event2V.OnBackNavigation(event.getSender()));
    }

    private void showPortrait() {
        NavigationManager.Model navModel = navigationManager.getModel();
        String curLocId = "";
        if (navModel.getCurrentLocation() != null) {
            curLocId = navModel.getCurrentLocation().getLocationId();
        }
        //If we are viewing note, update the navigation history and navigate to proper location
        if(curLocId.equals(LocId.NOTE_HANDSET_DETAIL)
                || curLocId.equals(LocId.NOTE_HANDSET_LIST)
                || curLocId.equals(LocId.NOTE_TABLET_LANDSCAPE)) {
            //Clear history and go to note list
            navigationManager.navigate(this).to( LocId.NOTE_HANDSET_LIST, null);

            NoteModel noteModel = noteManager.getModel();
            if(null != noteModel.getViewingNote()) {
                //Was viewing note details, stack the detail location on top of note list
                navigationManager.navigate(this).to(LocId.NOTE_HANDSET_DETAIL);
            }
        }
    }

    private void showLandscape() {
        NavigationManager.Model navModel = navigationManager.getModel();
        String curLocId = navModel.getCurrentLocation() == null ? null : navModel.getCurrentLocation().getLocationId();
        //If we are viewing note, use landscape location only and clear history locations
        if(curLocId == null
                || curLocId.equals(LocId.NOTE_HANDSET_DETAIL)
                || curLocId.equals(LocId.NOTE_HANDSET_LIST)
                || curLocId.equals(LocId.NOTE_TABLET_LANDSCAPE)) {
            //Clear history and go to note landscape location
            navigationManager.navigate(this).to( LocId.NOTE_TABLET_LANDSCAPE, null);
        }
    }
}
