package com.shipdream.lib.android.mvc.samples.note.manager.internal;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shipdream.lib.android.mvc.manager.internal.BaseManagerImpl;
import com.shipdream.lib.android.mvc.samples.note.manager.NoteManager;
import com.shipdream.lib.android.mvc.samples.note.model.NoteModel;
import com.shipdream.lib.android.mvc.samples.note.model.dto.Note;
import com.shipdream.lib.android.mvc.samples.note.service.android.PreferenceService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

public class NoteManagerImpl extends BaseManagerImpl<NoteModel> implements NoteManager {
    private static final String PREF_KEY_NOTES = "PrefKey:Notes";
    private Gson gson = new Gson();

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
    }

    @Inject
    private PreferenceService preferenceService;

    @Override
    public Class<NoteModel> modelType() {
        return NoteModel.class;
    }

    @Override
    public void setViewingNote(long noteId) {
        getModel().setViewingNote(findNote(noteId));
    }

    @Override
    public void addNote(String title, String content) {
        if(title != null && !title.isEmpty()) {
            Note note = new Note();
            getNotes().add(note);
            note.setId(System.currentTimeMillis());
            note.setTitle(title);
            note.setContent(content);
            note.setUpdateTime(System.currentTimeMillis());

            persistNotes();
        }
    }

    @Override
    public void updateViewingNote(String title, String content) {
        Note note = getModel().getViewingNote();
        if(note != null) {
            note.setTitle(title);
            note.setContent(content);
            note.setUpdateTime(System.currentTimeMillis());
            persistNotes();
        }
    }

    @Override
    public void removeNote(List<Long> noteIds) {
        Iterator<Note> iterator = getNotes().iterator();
        while (iterator.hasNext()) {
            Note note = iterator.next();
            if (noteIds.contains(note.getId())) {
                iterator.remove();
            }
        }
        persistNotes();
    }

    @Override
    public void persistNotes() {
        preferenceService.edit().putString(PREF_KEY_NOTES, gson.toJson(getModel().getNotes())).commit();
    }

    @Override
    public List<Note> getNotes() {
        return getModel().getNotes();
    }

    @Override
    public Note findNote(long id) {
        for (Note n : getNotes()) {
            if (n.getId() == id) {
                return n;
            }
        }
        return null;
    }

}
