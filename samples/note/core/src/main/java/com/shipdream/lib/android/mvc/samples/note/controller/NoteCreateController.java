package com.shipdream.lib.android.mvc.samples.note.controller;

import com.shipdream.lib.android.mvc.controller.BaseController;
import com.shipdream.lib.android.mvc.event.BaseEventV;
import com.shipdream.lib.android.mvc.samples.note.model.NoteCreateModel;

public interface NoteCreateController extends BaseController<NoteCreateModel> {
    /**
     * Add a new note and will raise {@link EventC2V.OnNoteCreated}
     * @param title The title
     * @param content The content
     */
    void addNote(String title, String content);

    interface EventC2V {
        /**
         * Raises when a new note is created
         */
        class OnNoteCreated extends BaseEventV {
            public OnNoteCreated(Object sender) {
                super(sender);
            }
        }
    }
}
