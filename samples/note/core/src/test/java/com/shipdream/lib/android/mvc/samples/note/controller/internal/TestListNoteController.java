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

import com.shipdream.lib.android.mvc.MvcGraph;
import com.shipdream.lib.android.mvc.manager.NavigationManager;
import com.shipdream.lib.android.mvc.samples.note.LocId;
import com.shipdream.lib.android.mvc.samples.note.controller.AppController;
import com.shipdream.lib.android.mvc.samples.note.controller.NoteListController;
import com.shipdream.lib.android.mvc.samples.note.model.NoteListModel;
import com.shipdream.lib.android.mvc.samples.note.service.android.PreferenceService;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Provides;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//Test cases in this class do not cover all possible scenarios but just for showing examples
public class TestListNoteController extends TestControllerBase<NoteListController> {
    private AppController appControllerMock;
    private PreferenceService preferenceServiceMock;
    private PreferenceService.Editor editorMock;

    public static class TestComp extends Component {
        TestListNoteController testNoteController;

        @Singleton
        @Provides
        public PreferenceService providePreferenceService() {
            return testNoteController.preferenceServiceMock;
        }

        @Provides
        public AppController provideAppController() {
            return testNoteController.appControllerMock;
        }
    }

    @Override
    protected void registerDependencies(MvcGraph mvcGraph) {
        appControllerMock = mock(AppController.class);

        preferenceServiceMock = mock(PreferenceService.class);
        editorMock = mock(PreferenceService.Editor.class);
        when(editorMock.putString(anyString(), anyString())).thenReturn(editorMock);
        when(preferenceServiceMock.edit()).thenReturn(editorMock);

        TestComp testComp = new TestComp();
        testComp.testNoteController = this;
        mvcGraph.register(testComp);
    }

    @Override
    protected NoteListController createTestingController() {
        return new NoteListControllerImpl();
    }

    @Test
    public void shouldNavigateToNewNoteViewByCalling_ToCreateNewNote() {
        //arrange
        class Monitor {
            public void onEvent(NavigationManager.Event2C.OnLocationForward event) {
            }
        }

        Monitor monitor = mock(Monitor.class);
        eventBusC.register(monitor);

        //act
        controllerToTest.toCreateNote();

        //verify
        ArgumentCaptor<NavigationManager.Event2C.OnLocationForward> navEvent
                = ArgumentCaptor.forClass(NavigationManager.Event2C.OnLocationForward.class);
        //Navigation event should be raised
        verify(monitor, times(1)).onEvent(navEvent.capture());
        //It should go to new note view
        Assert.assertEquals(navEvent.getValue().getCurrentValue().getLocationId(), LocId.NEW_NOTE);
    }

    @Test
    public void shouldBeInSelectionModeOnlyWhenSelectedNoteIdListIsNullOrEmpty() {
        //pre-check
        Assert.assertFalse(controllerToTest.inSelectionMode());

        //arrange
        NoteListModel model = new NoteListModel();
        controllerToTest.bindModel(this, model);
        model.setSelectedNoteIds(null);

        //assert
        Assert.assertFalse(controllerToTest.inSelectionMode());

        //arrange
        model.setSelectedNoteIds(new ArrayList<Long>());

        //assert
        Assert.assertFalse(controllerToTest.inSelectionMode());

        //arrange
        List<Long> selectedIds = new ArrayList<>();
        selectedIds.add(1L);
        model.setSelectedNoteIds(selectedIds);

        //assert
        Assert.assertTrue(controllerToTest.inSelectionMode());
    }

    @Test
    public void shouldNavigateToNoteDetailViewWhenSelectANoteInNonSelectionModeWhenInPortraitMode() {
        //arrange
        class Monitor {
            public void onEvent(NavigationManager.Event2C.OnLocationForward event) {
            }
        }

        Monitor monitor = mock(Monitor.class);
        eventBusC.register(monitor);

        Assert.assertFalse(controllerToTest.inSelectionMode());

        when(appControllerMock.getCurrentOrientation()).thenReturn(AppController.Orientation.PORTRAIT);

        //act
        controllerToTest.selectNote(5);

        //verify
        ArgumentCaptor<NavigationManager.Event2C.OnLocationForward> navEvent
                = ArgumentCaptor.forClass(NavigationManager.Event2C.OnLocationForward.class);
        //Navigation event should be raised
        verify(monitor, times(1)).onEvent(navEvent.capture());
        //It should go to new note view
        Assert.assertEquals(navEvent.getValue().getCurrentValue().getLocationId(), LocId.NOTE_HANDSET_DETAIL);
    }

    @Test
    public void shouldNotNavigateToNoteDetailViewWhenSelectANoteInNonSelectionModeWhenInLandscapeMode() {
        //arrange
        class Monitor {
            public void onEvent(NavigationManager.Event2C.OnLocationForward event) {
            }
        }

        Monitor monitor = mock(Monitor.class);
        eventBusC.register(monitor);

        Assert.assertFalse(controllerToTest.inSelectionMode());

        when(appControllerMock.getCurrentOrientation()).thenReturn(AppController.Orientation.LANDSCAPE);

        //act
        controllerToTest.selectNote(5);

        //verify
        ArgumentCaptor<NavigationManager.Event2C.OnLocationForward> navEvent
                = ArgumentCaptor.forClass(NavigationManager.Event2C.OnLocationForward.class);
        //Navigation event should be raised
        verify(monitor, times(0)).onEvent(navEvent.capture());
    }

    @Test
    public void shouldNotNavigateToNoteDetailViewWhenItIsInSelectionMode() {
        //arrange
        class Monitor {
            public void onEvent(NavigationManager.Event2C.OnLocationForward event) {
            }
        }

        Monitor monitor = mock(Monitor.class);
        eventBusC.register(monitor);


        Assert.assertFalse(controllerToTest.inSelectionMode());

        //arrange
        NoteListModel model = new NoteListModel();
        controllerToTest.bindModel(this, model);
        List<Long> selectedIds = new ArrayList<>();
        selectedIds.add(1L);
        model.setSelectedNoteIds(selectedIds);

        //now controller is in note selection mode
        Assert.assertTrue(controllerToTest.inSelectionMode());

        //act
        controllerToTest.selectNote(1);

        //verify
        ArgumentCaptor<NavigationManager.Event2C.OnLocationForward> navEvent
                = ArgumentCaptor.forClass(NavigationManager.Event2C.OnLocationForward.class);
        //Navigation event should be raised
        verify(monitor, times(0)).onEvent(navEvent.capture());
    }
}
