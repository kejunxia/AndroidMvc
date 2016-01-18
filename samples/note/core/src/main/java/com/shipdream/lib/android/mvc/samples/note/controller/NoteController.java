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

package com.shipdream.lib.android.mvc.samples.note.controller;

import com.shipdream.lib.android.mvc.controller.BaseController;
import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.android.mvc.event.BaseEventV;
import com.shipdream.lib.android.mvc.samples.note.model.NoteModel;
import com.shipdream.lib.android.mvc.samples.note.model.dto.Note;

import java.util.List;

public interface NoteController extends BaseController<NoteModel> {
    /**
     * Gets saved notes.
     * @return Empty array when there is no notes otherwise the list of notes.
     */
    List<Note> getNotes();

    /**
     * Navigate to create note view
     */
    void toCreateNote();

    /**
     * Indicates whether a note with given id is selected
     * @param id note id
     * @return -1 if nothing selected otherwise
     */
    int isSelected(long id);

    /**
     * Selects note with given id. If it's already in {@link #inSelectionMode()} add the note to the
     * selected list otherwise navigate to note detail page where there should be an event
     * {@link NavigationController.EventC2V.OnLocationForward} raised.
     * @param id The id
     */
    void selectNote(long id);

    /**
     * Add a new note and will raise {@link EventC2V.OnNoteCreated}
     * @param title The title
     * @param content The content
     */
    void addNote(String title, String content);

    /**
     * Updates the note being viewed if there is one. Will raise {@link EventC2V.OnNoteUpdated} if
     * the viewing note is updated.
     * @param title The title
     * @param content The content
     */
    void updateViewingNote(Object sender, String title, String content);

    /**
     * Removes all selected notes and raises {@link EventC2V.OnNoteRemoved}
     * @param sender who wants to remove note
     */
    void removeNote(Object sender);

    /**
     * Indicates whether in note selection mode
     * @return whether in selection mode
     */
    boolean inSelectionMode();

    /**
     * Toggles selection of the note with given id and raises {@link EventC2V.OnNoteSelectionChanged}.
     * If {@link #inSelectionMode()} is false, it also turns on selection mode and raises
     * {@link EventC2V.OnEditModeBegan}
     * @param sender Who initiates the command
     * @param id The id
     */
    void toggleSelection(Object sender, long id);

    /**
     * Clear all selections and turns off selection mode and raises {@link EventC2V.OnNoteSelectionChanged}.
     * @param sender Who initiates the command
     */
    void clearSelections(Object sender);

    /**
     * Converts time to string for note updated time
     * @param time The time in date
     * @return  The time in string
     */
    String getDisplayNoteUpdateTime(long time);

    /**
     * Events from {@link NoteController} to views.
     */
    class EventC2V {
        /**
         * Raises when a new note is created
         */
        public static class OnNoteSelected extends BaseEventV {
            private final Note note;

            public OnNoteSelected(Object sender, Note note) {
                super(sender);
                this.note = note;
            }

            public Note getNote() {
                return note;
            }
        }

        /**
         * Raises when a new note is created
         */
        public static class OnNoteCreated extends BaseEventV {
            public OnNoteCreated(Object sender) {
                super(sender);
            }
        }

        /**
         * Raises when a new note is updated
         */
        public static class OnNoteUpdated extends BaseEventV {
            public OnNoteUpdated(Object sender) {
                super(sender);
            }
        }

        /**
         * Raises when edit mode has began
         */
        public static class OnEditModeBegan extends BaseEventV {
            public OnEditModeBegan(Object sender) {
                super(sender);
            }
        }

        /**
         * Raises when any selection of notes changed
         */
        public static class OnNoteSelectionChanged extends BaseEventV {
            public OnNoteSelectionChanged(Object sender) {
                super(sender);
            }
        }

        /**
         * Raises when a new note is removed
         */
        public static class OnNoteRemoved extends BaseEventV {
            final private List<Long> deletedNoteIds;
            public OnNoteRemoved(Object sender, List<Long> deletedNoteIds) {
                super(sender);
                this.deletedNoteIds = deletedNoteIds;
            }

            public List<Long> getDeletedNoteIds() {
                return deletedNoteIds;
            }
        }
    }
}
