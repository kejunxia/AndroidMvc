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

package com.shipdream.lib.android.mvc.samples.note.view.internal;


import android.content.Context;
import android.content.SharedPreferences;

import com.shipdream.lib.android.mvc.samples.note.service.android.PreferenceService;

public class PreferenceServiceImpl implements PreferenceService {
    private SharedPreferences mPrefs;


    public PreferenceServiceImpl(Context context, String preferenceName, int mode){
        mPrefs = context.getSharedPreferences(preferenceName, mode);
    }

    @Override
    public boolean contains(String key) {
        return mPrefs.contains(key);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return mPrefs.getInt(key, defaultValue);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return mPrefs.getLong(key, defaultValue);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return mPrefs.getFloat(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return mPrefs.getBoolean(key, defaultValue);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return mPrefs.getString(key, defaultValue);
    }

    @Override
    public Editor edit() {
        return new AndroidPreferenceEditor(mPrefs.edit());
    }

    private static class AndroidPreferenceEditor implements Editor{
        private SharedPreferences.Editor mEditor;
        AndroidPreferenceEditor(SharedPreferences.Editor andEditor){
            mEditor = andEditor;
        }

        @Override
        public Editor putInt(String key, int value) {
            return new AndroidPreferenceEditor(mEditor.putInt(key, value));
        }

        @Override
        public Editor putLong(String key, long value) {
            return new AndroidPreferenceEditor(mEditor.putLong(key, value));
        }

        @Override
        public Editor putFloat(String key, float value) {
            return new AndroidPreferenceEditor(mEditor.putFloat(key, value));
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            return new AndroidPreferenceEditor(mEditor.putBoolean(key, value));
        }

        @Override
        public Editor putString(String key, String value) {
            return new AndroidPreferenceEditor(mEditor.putString(key, value));
        }

        @Override
        public Editor clear() {
            return new AndroidPreferenceEditor(mEditor.clear());
        }

        @Override
        public boolean commit() {
            return mEditor.commit();
        }

        @Override
        public void apply() {
            mEditor.apply();
        }
    }
}
