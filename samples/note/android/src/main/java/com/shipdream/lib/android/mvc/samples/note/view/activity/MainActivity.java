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

package com.shipdream.lib.android.mvc.samples.note.view.activity;

import com.shipdream.lib.android.mvc.samples.note.LocId;
import com.shipdream.lib.android.mvc.samples.note.view.fragment.MainFragment;
import com.shipdream.lib.android.mvc.samples.note.view.fragment.NoteCreateFragment;
import com.shipdream.lib.android.mvc.samples.note.view.fragment.NoteDetailFragment;
import com.shipdream.lib.android.mvc.samples.note.view.fragment.NoteListFragment;
import com.shipdream.lib.android.mvc.samples.note.view.fragment.NoteTabletLandscape;
import com.shipdream.lib.android.mvc.samples.note.view.fragment.WeatherListFragment;
import com.shipdream.lib.android.mvc.view.MvcActivity;
import com.shipdream.lib.android.mvc.view.MvcFragment;

public class MainActivity extends MvcActivity {
    @Override
    protected Class<? extends MvcFragment> mapNavigationFragment(String locationId) {
        switch (locationId) {
            case LocId.NOTE_HANDSET_LIST:
                return NoteListFragment.class;
            case LocId.NOTE_HANDSET_DETAIL:
                return NoteDetailFragment.class;
            case LocId.NOTE_TABLET_LANDSCAPE:
                return NoteTabletLandscape.class;
            case LocId.NEW_NOTE:
                return NoteCreateFragment.class;
            case LocId.WEATHERS:
                return WeatherListFragment.class;
            default:
                return null;
        }
    }

    @Override
    protected Class<? extends DelegateFragment> getDelegateFragmentClass() {
        return MainFragment.class;
    }
}
