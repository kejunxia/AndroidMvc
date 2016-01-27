package com.shipdream.lib.android.mvc.samples.note.controller;

import com.shipdream.lib.android.mvc.controller.BaseController;
import com.shipdream.lib.android.mvc.manager.NavigationManager;
import com.shipdream.lib.android.mvc.event.BaseEventV;
import com.shipdream.lib.android.mvc.samples.note.model.NoteListModel;
import com.shipdream.lib.android.mvc.samples.note.model.dto.Note;

import java.util.List;

public interface NoteListController extends BaseController<NoteListModel> {
    /**
     * Indicates whether a note with given id is selected
     * @param id note id
     * @return -1 if nothing selected otherwise
     */
    int isSelected(long id);

    /**
     * Removes all selected notes and raises {@link EventC2V.OnNoteRemoved}
     * @param sender who wants to remove note
     */
    void removeNote(Object sender);

    /**
     * Navigate to create note view
     */
    void toCreateNote();

    /**
     * Clear all selections and turns off selection mode and raises {@link EventC2V.OnNoteSelectionChanged}.
     * @param sender Who initiates the command
     */
    void clearSelections(Object sender);

    List<Note> getNotes();

    /**
     * Toggles selection of the note with given id and raises {@link EventC2V.OnNoteSelectionChanged}.
     * If {@link #inSelectionMode()} is false, it also turns on selection mode and raises
     * {@link EventC2V.OnEditModeBegan}
     * @param sender Who initiates the command
     * @param id The id
     */
    void toggleSelection(Object sender, long id);

    /**
     * Indicates whether in note selection mode
     * @return whether in selection mode
     */
    boolean inSelectionMode();

    /**
     * Converts time to string for note updated time
     * @param time The time in date
     * @return  The time in string
     */
    String getDisplayNoteUpdateTime(long time);

    /**
     * Selects note with given id. If it's already in {@link #inSelectionMode()} add the note to the
     * selected list otherwise navigate to note detail page where there should be an event
     * {@link NavigationManager.EventC2V.OnLocationForward} raised.
     * @param id The id
     */
    void selectNote(long id);

    interface EventC2V {
        /**
         * Raises when a new note is created
         */
        class OnNoteSelected extends BaseEventV {
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
        class OnNoteRemoved extends BaseEventV {
            private final List<Long> deletedNoteIds;
            public OnNoteRemoved(Object sender, List<Long> deletedNoteIds) {
                super(sender);
                this.deletedNoteIds = deletedNoteIds;
            }

            public List<Long> getDeletedNoteIds() {
                return deletedNoteIds;
            }
        }

        /**
         * Raises when any selection of notes changed
         */
        class OnNoteSelectionChanged extends BaseEventV {
            public OnNoteSelectionChanged(Object sender) {
                super(sender);
            }
        }

        /**
         * Raises when edit mode has began
         */
        class OnEditModeBegan extends BaseEventV {
            public OnEditModeBegan(Object sender) {
                super(sender);
            }
        }

    }
}
