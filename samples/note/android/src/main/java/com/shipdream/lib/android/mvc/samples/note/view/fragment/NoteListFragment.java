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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.samples.note.R;
import com.shipdream.lib.android.mvc.samples.note.controller.NoteController;
import com.shipdream.lib.android.mvc.samples.note.model.dto.Note;

import javax.inject.Inject;

public class NoteListFragment extends BaseFragment {
    private Button buttonAddNote;
    private View emptyView;
    private RecyclerView listView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ActionMode actionMode;

    @Inject
    private NoteController noteController;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_note_list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionMode = null;
    }

    @Override
    public void onViewReady(View view, Bundle savedInstanceState, Reason reason) {
        super.onViewReady(view, savedInstanceState, reason);

        emptyView = view.findViewById(R.id.fragment_note_list_listViewEmpty);

        buttonAddNote = (Button) view.findViewById(R.id.fragment_note_list_buttonAdd);
        buttonAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteController.toCreateNote();
            }
        });

        listView = (RecyclerView) view.findViewById(R.id.fragment_note_list_listView);
        layoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(layoutManager);

        if (!reason.isRotated()) {
            updateList();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if(getCurrentOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            getToolBar().setTitle("My Notes");
        }
        //otherwise let Tablet Fragment show its title
    }

    @Override
    public boolean onBackButtonPressed() {
        if(actionMode == null) {
            return super.onBackButtonPressed();
        } else {
            actionMode.finish();
            return true;
        }
    }

    public void onEvent(NoteController.EventC2V.OnNoteSelectionChanged event) {
        updateList();
    }

    public void onEvent(NoteController.EventC2V.OnNoteRemoved event) {
        updateList();
    }

    public void onEvent(NoteController.EventC2V.OnEditModeBegan event) {
        showActionMode();
    }

    private void showActionMode() {
        actionMode = getToolBar().startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_edit_notes, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_delete:
                        noteController.removeNote(item);
                        mode.finish();
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                noteController.clearSelections(mode);
                actionMode = null;
            }
        });
    }

    private void updateList() {
        adapter = new NoteAdapter(this);
        listView.setAdapter(adapter);

        if (adapter.getItemCount() > 0) {
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }

        if(actionMode != null && !noteController.inSelectionMode()) {
            actionMode.finish();
        } else if(noteController.inSelectionMode() && actionMode == null) {
            showActionMode();
        }
    }

    static class NoteAdapter extends RecyclerView.Adapter<NoteItemHolder> {
        private NoteListFragment noteListFragment;

        NoteAdapter(NoteListFragment noteListFragment) {
            this.noteListFragment = noteListFragment;
        }

        @Override
        public NoteItemHolder onCreateViewHolder(ViewGroup parent, int i) {
            View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_note_list_item, parent, false);
            NoteItemHolder holder = new NoteItemHolder(convertView);
            holder.root = convertView.findViewById(R.id.note_item_root);
            holder.title = (TextView) convertView.findViewById(R.id.fragment_note_list_item_title);
            holder.updateTime = (TextView) convertView.findViewById(R.id.fragment_note_list_item_updateTime);
            return holder;
        }

        @Override
        public void onBindViewHolder(NoteItemHolder holder, final int i) {
            final Note note = noteListFragment.noteController.getNotes().get(i);
            holder.title.setText(note.getTitle());
            holder.updateTime.setText(noteListFragment.noteController.getDisplayNoteUpdateTime(note.getUpdateTime()));
            holder.root.setSelected(noteListFragment.noteController.isSelected(note.getId()) >= 0);
            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    noteListFragment.noteController.selectNote(note.getId());
                }
            });
            holder.root.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    noteListFragment.noteController.toggleSelection(v, note.getId());
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return noteListFragment.noteController.getNotes().size();
        }
    }

    static class NoteItemHolder extends RecyclerView.ViewHolder {
        View root;
        TextView title;
        TextView updateTime;

        public NoteItemHolder(View itemView) {
            super(itemView);
        }
    }
}
