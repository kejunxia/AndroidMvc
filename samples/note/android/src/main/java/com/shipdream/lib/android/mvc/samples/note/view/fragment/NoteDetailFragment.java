/*
 * Copyright 2016 Kejun Xia
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

package com.shipdream.lib.android.mvc.samples.note.view.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.shipdream.lib.android.mvc.samples.note.R;
import com.shipdream.lib.android.mvc.samples.note.controller.NoteController;
import com.shipdream.lib.android.mvc.samples.note.model.dto.Note;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class NoteDetailFragment extends BaseFragment {
    private TextView title;
    private TextView content;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private NoteController noteController;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_note_detail;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        title = (TextView) view.findViewById(R.id.fragment_note_detail_title);
        content = (TextView) view.findViewById(R.id.fragment_note_detail_content);

        Note note = noteController.getModel().getViewingNote();
        displayNote(note);
    }

    private void displayNote(Note note) {
        title.setText(note == null ? "NO SELECTED NOTE" : note.getTitle());
        content.setText(note == null ? "" : note.getContent());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCurrentOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            getToolBar().setTitle("Note Detail");
        }
        //otherwise let Tablet Fragment show its title
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isVisible()) {
            noteController.updateViewingNote(this, title.getText().toString(), content.getText().toString());
        }
    }

    private void onEvent(NoteController.EventC2V.OnNoteSelected event) {
        displayNote(event.getNote());
    }

    public void onEvent(NoteController.EventC2V.OnNoteUpdated event) {
        Toast.makeText(getActivity(), "Note updated.", Toast.LENGTH_SHORT).show();
        logger.debug("Note updated by {}", event.getSender().hashCode());
    }
}
