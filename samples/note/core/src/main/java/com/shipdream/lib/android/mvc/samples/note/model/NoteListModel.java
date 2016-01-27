package com.shipdream.lib.android.mvc.samples.note.model;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class NoteListModel {
    private List<Long> selectedNoteIds = new ArrayList<>();

    public List<Long> getSelectedNoteIds() {
        return selectedNoteIds;
    }

    public void setSelectedNoteIds(List<Long> selectedNoteIds) {
        this.selectedNoteIds = selectedNoteIds;
    }
}
