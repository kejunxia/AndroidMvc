package com.shipdream.lib.android.mvc.samples.note.controller.internal;

import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.samples.note.controller.NoteDetailController;
import com.shipdream.lib.android.mvc.samples.note.manager.NoteManager;
import com.shipdream.lib.android.mvc.samples.note.model.NoteDetailModel;
import com.shipdream.lib.android.mvc.samples.note.model.dto.Note;

import javax.inject.Inject;

public class NoteDetailControllerImpl extends BaseControllerImpl<NoteDetailModel>
        implements NoteDetailController {
    @Inject
    private NoteManager noteManager;

    @Override
    public Class<NoteDetailModel> modelType() {
        return NoteDetailModel.class;
    }

    @Override
    public void updateViewingNote(Object sender, String title, String content) {
        noteManager.updateViewingNote(title, content);
        postViewEvent(new EventC2V.OnNoteUpdated(sender));
    }

    @Override
    public Note getViewingNote() {
        return noteManager.getModel().getViewingNote();
    }

}
