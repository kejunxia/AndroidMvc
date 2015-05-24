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

package com.shipdream.lib.android.mvc.samples.note.view;

import android.app.Application;
import android.content.Context;
import android.os.Parcelable;

import com.shipdream.lib.android.mvc.samples.note.service.android.PreferenceService;
import com.shipdream.lib.android.mvc.samples.note.view.internal.PreferenceServiceImpl;
import com.shipdream.lib.android.mvc.view.AndroidMvc;
import com.shipdream.lib.android.mvc.view.AndroidStateKeeper;
import com.shipdream.lib.poke.Component;
import com.shipdream.lib.poke.Provides;
import com.shipdream.lib.poke.ScopeCache;

import org.parceler.Parcels;

/**
 * Note app. It uses custom {@link AndroidStateKeeper} to save and restore models in conjunction
 * of a library <a href="https://github.com/johncarl81/parceler">Parceler</a> to help parcel
 * objects without boilerplate code.
 */
public class NoteApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AndroidMvc.setCustomStateKeeper(new AndroidStateKeeper() {
            @SuppressWarnings("unchecked")
            @Override
            public Parcelable saveState(Object state, Class type) {
                /**
                 * Use parcelable to save all states.
                 */
                return Parcels.wrap(state);
                //type of the state can be used as a filter to handle some state specially
                //if (type == BlaBlaType) {
                //    special logic to save state
                //}
            }

            @SuppressWarnings("unchecked")
            @Override
            public Object getState(Parcelable parceledState, Class type) {
                /**
                 * Use parcelable to restore all states.
                 */
                return Parcels.unwrap(parceledState);

                //type of the state can be used as a filter to handle some state specially
                //if (type == BlaBlaType) {
                //    special logic to restore state
                //}
            }
        });

        NoteComponent component = new NoteComponent(new ScopeCache(), getApplicationContext());
        AndroidMvc.graph().register(component);
    }

    static class NoteComponent extends Component {
        private Context context;

        public NoteComponent(ScopeCache scopeCache, Context context) {
            super(scopeCache);
            this.context = context;
        }

        @Provides
        public PreferenceService providePreferenceService() {
            return new PreferenceServiceImpl(context, "SampleNote:PreferenceDefault", MODE_PRIVATE);
        }
    }
}
