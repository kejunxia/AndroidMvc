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

package com.shipdream.lib.android.mvp;

/**
 * ModelKeeper can save model of objects implementing {@link MvpBean} into it. The model can
 * be got back from the keeper later on.
 */
public interface ModelKeeper {
    /**
     * Save model into this {@link ModelKeeper}
     * @param model The model to save
     * @param type The class type of the model
     * @param <T>
     */
    <T> void saveModel(T model, Class<T> type);

    /**
     * Retrieves model
     * @param type The class type of the model
     * @param <T>
     * @return null if the model with the given type has not been saved or a null model was saved,
     * otherwise returns model saved previously
     */
    <T> T retrieveModel(Class<T> type);
}
