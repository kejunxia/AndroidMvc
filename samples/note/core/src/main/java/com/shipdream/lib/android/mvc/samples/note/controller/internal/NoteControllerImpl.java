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

package com.shipdream.lib.android.mvc.samples.note.controller.internal;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.samples.note.LocId;
import com.shipdream.lib.android.mvc.samples.note.controller.AppController;
import com.shipdream.lib.android.mvc.samples.note.controller.NoteController;
import com.shipdream.lib.android.mvc.samples.note.model.NoteModel;
import com.shipdream.lib.android.mvc.samples.note.model.dto.Note;
import com.shipdream.lib.android.mvc.samples.note.service.android.PreferenceService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

public class NoteControllerImpl extends BaseControllerImpl<NoteModel> implements NoteController {
    private static final String PREF_KEY_NOTES = "PrefKey:Notes";
    private Gson gson = new Gson();

    @Inject
    private AppController appController;

    @Inject
    private PreferenceService preferenceService;

    //Inject depending controller - navigationController
    @Inject
    private NavigationController navigationController;

    @Override
    public void onConstruct() {
        super.onConstruct();

        String json = preferenceService.getString(PREF_KEY_NOTES, null);
        List<Note> notes;
        if (json != null) {
            notes = gson.fromJson(json, new TypeToken<List<Note>>() {}.getType());
        } else {
            notes = new ArrayList<>();
        }
        getModel().setNotes(notes);
        getModel().setSelectedNoteIds(new ArrayList<Long>());
    }

    private List<Note> notes() {
        return getModel().getNotes();
    }

    private Note findNote(long id) {
        for (Note n : notes()) {
            if (n.getId() == id) {
                return n;
            }
        }
        return null;
    }

    @Override
    public List<Note> getNotes() {
        return notes();
    }

    @Override
    public void toCreateNote() {
        navigationController.navigateTo(this, LocId.NEW_NOTE);
    }

    @Override
    public void selectNote(long id) {
        if (inSelectionMode()) {
            toggleSelection(this, id);
        } else {
            Note note = findNote(id);
            getModel().setViewingNote(findNote(id));

            if(appController.getCurrentOrientation() == AppController.Orientation.PORTRAIT) {
                //Only navigate on portrait mode
                navigationController.navigateTo(this, LocId.NOTE_HANDSET_DETAIL);
            } else {
                EventC2V.OnNoteSelected event = new EventC2V.OnNoteSelected(this, note);
                postToViews(event);
            }
        }
    }

    @Override
    public void addNote(String title, String content) {
        if(title != null && !title.isEmpty()) {
            Note note = new Note();
            notes().add(note);
            note.setId(System.currentTimeMillis());
            note.setTitle(title);
            note.setContent(content);
            note.setUpdateTime(System.currentTimeMillis());

            persistNotes();
            postToViews(new EventC2V.OnNoteCreated(this));
        }
        navigationController.navigateBack(this);
    }

    @Override
    public void updateViewingNote(Object sender, String title, String content) {
        Note note = getModel().getViewingNote();
        if(note != null) {
            note.setTitle(title);
            note.setContent(content);
            note.setUpdateTime(System.currentTimeMillis());
            persistNotes();
            postToViews(new EventC2V.OnNoteUpdated(sender));
        }
    }

    @Override
    public void removeNote(Object sender) {
        if (inSelectionMode()) {
            Iterator<Note> iterator = notes().iterator();
            while (iterator.hasNext()) {
                Note note = iterator.next();
                if (getModel().getSelectedNoteIds().contains(note.getId())) {
                    iterator.remove();
                }
            }

            persistNotes();

            postToViews(new EventC2V.OnNoteRemoved(sender, new ArrayList<>(getModel().getSelectedNoteIds())));
            getModel().getSelectedNoteIds().clear();
        }
    }

    private void persistNotes() {
        preferenceService.edit().putString(PREF_KEY_NOTES, gson.toJson(getModel().getNotes())).commit();
    }

    @Override
    public boolean inSelectionMode() {
        return getModel().getSelectedNoteIds() != null && !getModel().getSelectedNoteIds().isEmpty();
    }

    @Override
    public void toggleSelection(Object sender, long id) {
        if (!inSelectionMode()) {
            //Not in selection mode, add the first selected item
            getModel().getSelectedNoteIds().add(id);
            postToViews(new EventC2V.OnEditModeBegan(sender));
        } else {
            //check state of current id
            int index = isSelected(id);
            if (index >= 0) {
                getModel().getSelectedNoteIds().remove(index);
            } else {
                getModel().getSelectedNoteIds().add(id);
            }
        }
        postToViews(new EventC2V.OnNoteSelectionChanged(sender));
    }

    public int isSelected(long id) {
        if (getModel().getSelectedNoteIds() == null || getModel().getSelectedNoteIds().isEmpty()) {
            return -1;
        } else {
            List<Long> ids = getModel().getSelectedNoteIds();
            int size = ids.size();
            for (int i = 0; i < size; i++) {
                if (id == ids.get(i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void clearSelections(Object sender) {
        getModel().getSelectedNoteIds().clear();
        postToViews(new EventC2V.OnNoteSelectionChanged(sender));
    }

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public String getDisplayNoteUpdateTime(long time) {
        Date d = new Date(time);
        return format.format(d);
    }

    @Override
    public Class<NoteModel> getModelClassType() {
        return NoteModel.class;
    }
}
