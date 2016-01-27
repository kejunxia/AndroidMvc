package com.shipdream.lib.android.mvc.samples.note.manager;

import com.shipdream.lib.android.mvc.manager.BaseManager;
import com.shipdream.lib.android.mvc.samples.note.model.NoteModel;
import com.shipdream.lib.android.mvc.samples.note.model.dto.Note;

import java.util.List;

public interface NoteManager extends BaseManager<NoteModel> {
    void setViewingNote(long noteId);
    void addNote(String title, String content);
    void updateViewingNote(String title, String content);
    void removeNote(List<Long> noteIds);
    void persistNotes();
    Note findNote(long noteId);

    List<Note> getNotes();
}
