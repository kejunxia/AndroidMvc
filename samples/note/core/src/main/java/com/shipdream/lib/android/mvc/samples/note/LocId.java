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

package com.shipdream.lib.android.mvc.samples.note;

public interface LocId {
    /**
     * Page to show note list on handset(portrait + landscape) and tablet portrait. From here app
     * will drill down to {@link #NOTE_HANDSET_DETAIL}
     */
    String NOTE_HANDSET_LIST = "MyHandsetNotes";
    /**
     * Page to show note detail on handset(portrait + landscape) and tablet portrait. Will be on top
     * of {@link #NOTE_HANDSET_LIST}
     */
    String NOTE_HANDSET_DETAIL = "NoteHandsetDetail";
    /**
     * Page to show note list and detail with two panes on the same page for tablet landscape mode.
     */
    String NOTE_TABLET_LANDSCAPE = "NoteTabletLandScape";
    String NEW_NOTE = "NewNote";
    String WEATHERS = "Weathers";
}