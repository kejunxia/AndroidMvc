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

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.shipdream.lib.android.mvc.samples.note.R;
import com.shipdream.lib.android.mvc.samples.note.controller.NoteCreateController;

import javax.inject.Inject;

public class NoteCreateFragment extends BaseFragment {
    @Inject
    private NoteCreateController noteCreateController;

    private EditText title;
    private EditText content;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_new_note;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);
        title = (EditText) view.findViewById(R.id.fragment_new_note_title);
        content = (EditText) view.findViewById(R.id.fragment_new_note_content);
    }

    @Override
    public void onResume() {
        super.onResume();
        getToolBar().setTitle("Create A New Note");
    }

    @Override
    public boolean onBackButtonPressed() {
        noteCreateController.addNote(title.getText().toString(), content.getText().toString());
        return true;
    }

    public void onEvent(NoteCreateController.EventC2V.OnNoteCreated event) {
        Toast.makeText(getActivity(), "Note created.", Toast.LENGTH_SHORT).show();
    }
}
