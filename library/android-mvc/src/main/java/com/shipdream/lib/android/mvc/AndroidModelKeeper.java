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

package com.shipdream.lib.android.mvc;

import android.os.Parcelable;

/**
 * Implements this interface to use {@link Parcelable} to save and restore state with better
 * performance
 * @param <T>
 */
public interface AndroidModelKeeper<T> {
    /**
     * Save the given state into {@link Parcelable}
     * @param model the model to save
     */
    Parcelable saveModel(T model, Class<T> modelType);

    /**
     * Restore state from the {@link Parcelable}
     * @return The restored model
     */
    T getModel(Parcelable parceledModel, Class<T> modelType);
}
