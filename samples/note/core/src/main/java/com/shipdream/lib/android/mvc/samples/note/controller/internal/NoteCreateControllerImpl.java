package com.shipdream.lib.android.mvc.samples.note.controller.internal;

import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.samples.note.controller.NoteCreateController;
import com.shipdream.lib.android.mvc.samples.note.manager.NoteManager;
import com.shipdream.lib.android.mvc.samples.note.model.NoteCreateModel;

import javax.inject.Inject;

public class NoteCreateControllerImpl extends BaseControllerImpl<NoteCreateModel>
        implements NoteCreateController {
    @Inject
    private NavigationController navigationController;

    @Inject
    private NoteManager noteManager;

    @Override
    public Class<NoteCreateModel> modelType() {
        return NoteCreateModel.class;
    }

    @Override
    public void addNote(String title, String content) {
        if(title != null && !title.isEmpty()) {
            noteManager.addNote(title, content);
            postViewEvent(new EventC2V.OnNoteCreated(this));
        }
        navigationController.navigate(this).back();
    }
}
