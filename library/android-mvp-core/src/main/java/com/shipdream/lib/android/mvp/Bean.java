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

import com.shipdream.lib.poke.util.ReflectUtils;

public abstract class Bean<MODEL> {
    private MODEL model;

    /**
     * Bind model to MvpBean
     * @param model non-null model
     * @throws IllegalArgumentException thrown when null is being bound
     */
    public void bindModel(MODEL model) {
        if (model == null) {
            throw new IllegalArgumentException("Can't bind null model explicitly.");
        } else {
            this.model = model;
        }
    }

    /**
     * Called when the MvpBean is injected for the first time or restored when a new instance of
     * this MvpBean needs to be instantiated.
     *
     * <p>The model of the MvpBean will be instantiated by model's default no-argument constructor.
     * However, if the MvpBean needs to be restored, a new instance of model restored by
     * {@link #restoreModel(Object)} will replace the model created by this method.</p>
     */
    public void onCreated() {
        model = instantiateModel();
    }

    private MODEL instantiateModel() {
        Class<MODEL> type = modelType();
        if (type == null) {
            return null;
        } else {
            try {
                return new ReflectUtils.newObjectByType<>(type).newInstance();
            } catch (Exception e) {
                throw new RuntimeException(
                        String.format("Fail to instantiate model of %s by its default constructor", type.getName()), e);
            }
        }
    }

    /**
     * Called when the MvpBean is disposed. This occurs when the MvpBean is de-referenced and
     * not retained by any other objects.
     */
    public void onDisposed() {
    }

    /**
     * Model represents the state of this MvpBean.
     * @return Null if the MvpBean doesn't need to get its model saved and restored automatically.
     */
    public MODEL getModel() {
        return model;
    }

    /**
     * Provides the type class of the model.
     * @return Implementing class should return the type class of the model that will be used by
     * this MvpBean to instantiate its model in {@link #onCreated()} and restores model in
     * {@link #restoreModel(Object)}. Returning null is allowed which means this MvpBean doesn't
     * have a model needs to be automatically saved and restored.
     */
    public abstract Class<MODEL> modelType();

    /**
     * Restores the model of this MvpBean.
     * <p>
     * Note that when {@link #modelType()} returns null, this method will have no effect.
     * </p>
     *
     * @param restoredModel The restored model by {@link StateKeeper} that will be rebound to the
     *                      MvpBean.
     */
    public void restoreModel(MODEL restoredModel) {
        if (modelType() != null) {
            this.model = restoredModel;
            onRestored();
        }
    }

    /**
     * Called after {@link #restoreModel(Object)} is called only when {@link #modelType()} returns
     * a non-null type class.
     */
    public void onRestored() {
    }
}
