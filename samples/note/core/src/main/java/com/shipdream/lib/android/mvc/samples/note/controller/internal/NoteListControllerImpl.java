package com.shipdream.lib.android.mvc.samples.note.controller.internal;

import com.shipdream.lib.android.mvc.controller.NavigationController;
import com.shipdream.lib.android.mvc.controller.internal.BaseControllerImpl;
import com.shipdream.lib.android.mvc.samples.note.LocId;
import com.shipdream.lib.android.mvc.samples.note.controller.AppController;
import com.shipdream.lib.android.mvc.samples.note.controller.NoteListController;
import com.shipdream.lib.android.mvc.samples.note.manager.NoteManager;
import com.shipdream.lib.android.mvc.samples.note.model.NoteListModel;
import com.shipdream.lib.android.mvc.samples.note.model.dto.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class NoteListControllerImpl extends BaseControllerImpl<NoteListModel>
        implements NoteListController {
    @Inject
    private AppController appController;

    @Inject
    private NavigationController navigationController;

    @Inject
    private NoteManager noteManager;

    @Override
    public Class<NoteListModel> modelType() {
        return NoteListModel.class;
    }

    @Override
    public void removeNote(Object sender) {
        if (inSelectionMode()) {
            noteManager.removeNote(getModel().getSelectedNoteIds());
            postViewEvent(new EventC2V.OnNoteRemoved(sender, new ArrayList<>(getModel().getSelectedNoteIds())));
            getModel().getSelectedNoteIds().clear();
        }
    }

    @Override
    public void toCreateNote() {
        navigationController.navigate(this).to(LocId.NEW_NOTE);
    }

    @Override
    public void clearSelections(Object sender) {
        getModel().getSelectedNoteIds().clear();
        postViewEvent(new EventC2V.OnNoteSelectionChanged(sender));
    }

    @Override
    public List<Note> getNotes() {
        return noteManager.getNotes();
    }

    @Override
    public boolean inSelectionMode() {
        return getModel().getSelectedNoteIds() != null && !getModel().getSelectedNoteIds().isEmpty();
    }

    @Override
    public void selectNote(long id) {
        if (inSelectionMode()) {
            toggleSelection(this, id);
        } else {
            noteManager.setViewingNote(id);

            if(appController.getCurrentOrientation() == AppController.Orientation.PORTRAIT) {
                //Only navigate on portrait mode
                navigationController.navigate(this).to(LocId.NOTE_HANDSET_DETAIL);
            } else {
                EventC2V.OnNoteSelected event = new EventC2V.OnNoteSelected(this, noteManager.findNote(id));
                postViewEvent(event);
            }
        }
    }

    @Override
    public void toggleSelection(Object sender, long id) {
        if (!inSelectionMode()) {
            //Not in selection mode, add the first selected item
            getModel().getSelectedNoteIds().add(id);
            postViewEvent(new EventC2V.OnEditModeBegan(sender));
        } else {
            //check state of current id
            int index = isSelected(id);
            if (index >= 0) {
                getModel().getSelectedNoteIds().remove(index);
            } else {
                getModel().getSelectedNoteIds().add(id);
            }
        }
        postViewEvent(new EventC2V.OnNoteSelectionChanged(sender));
    }

    @Override
    public int isSelected(long id) {
        if (getModel().getSelectedNoteIds() == null || getModel().getSelectedNoteIds().isEmpty()) {
            return -1;
        } else {
            List<Long> ids = getModel().getSelectedNoteIds();
            int size = ids.size();
            for (int i = 0; i < size; i++) {
                if (id == ids.get(i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public String getDisplayNoteUpdateTime(long time) {
        Date d = new Date(time);
        return format.format(d);
    }

}
