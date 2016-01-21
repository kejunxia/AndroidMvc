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

package com.shipdream.lib.android.mvc.service;

/**
 * Interface for preference service
 */
public interface PreferenceService {
    /**
     * Indicate if a key already exists
     * @param key The key
     * @return whether the key exists
     */
    boolean contains(String key);

    int getInt(String key, int defaultValue);
    long getLong(String key, long defaultValue);
    float getFloat(String key, float defaultValue);
    boolean getBoolean(String key, boolean defaultValue);
    String getString(String key, String defaultValue);

    Editor edit();

    interface Editor{
        Editor putInt(String key, int value);
        Editor putLong(String key, long value);
        Editor putFloat(String key, float value);
        Editor putBoolean(String key, boolean value);
        Editor putString(String key, String value);
        Editor remove(String key);
        Editor clear();

        /**
         * Synchronously commit changes and return whether or not the changes are applied successfully.
         * @return True if the new values were successfully written to persistent storage.
         */
        boolean commit();

        /**
         * Asynchronously applies changes.
         */
        void apply();
    }
}
