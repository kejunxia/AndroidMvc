package com.shipdream.lib.android.mvc.samples.note.controller;

import com.shipdream.lib.android.mvc.controller.BaseController;
import com.shipdream.lib.android.mvc.event.BaseEventV;
import com.shipdream.lib.android.mvc.samples.note.model.NoteDetailModel;
import com.shipdream.lib.android.mvc.samples.note.model.dto.Note;

public interface NoteDetailController extends BaseController<NoteDetailModel> {
    /**
     * Updates the note being viewed if there is one. Will raise {@link EventC2V.OnNoteUpdated} if
     * the viewing note is updated.
     * @param title The title
     * @param content The content
     */
    void updateViewingNote(Object sender, String title, String content);

    /**
     * Get the note being viewed.
     * @return
     */
    Note getViewingNote();

    interface EventC2V {
        /**
         * Raises when a new note is updated
         */
        class OnNoteUpdated extends BaseEventV {
            public OnNoteUpdated(Object sender) {
                super(sender);
            }
        }
    }

}
