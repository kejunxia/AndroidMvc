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

package com.shipdream.lib.android.mvc.samples.note.model;

import com.shipdream.lib.android.mvc.samples.note.model.dto.Note;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class NoteModel {
    Note viewingNote;
    List<Note> notes;
    List<Long> selectedNoteIds;

    public Note getViewingNote() {
        return viewingNote;
    }

    public void setViewingNote(Note viewingNote) {
        this.viewingNote = viewingNote;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public List<Long> getSelectedNoteIds() {
        return selectedNoteIds;
    }

    public void setSelectedNoteIds(List<Long> selectedNoteIds) {
        this.selectedNoteIds = selectedNoteIds;
    }

}
