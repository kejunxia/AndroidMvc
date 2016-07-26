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

import com.shipdream.lib.poke.util.ReflectUtils;

/**
 * A bean in AndroidMvc is a basic unit has a model that can be automatically saved and
 * restored by the framework. Also, all beans have their life cycles. So when they are created on
 * injection {@link #onCreated()} will be called so do {@link #onDestroy()} when the last object
 * referencing them is not used any more.
 * @param <MODEL>
 */
public abstract class Bean<MODEL> {
    private MODEL model;

    /**
     * Bind model to Bean
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
     * Called when the Bean is injected for the first time or restored when a new instance of
     * this Bean needs to be instantiated.
     *
     * <p>The model of the Bean will be instantiated by model's default no-argument constructor.
     * However, if the Bean needs to be restored, a new instance of model restored by
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
     * Called when the Bean is disposed. This occurs when the Bean is de-referenced and
     * not retained by any other objects.
     */
    public void onDestroy() {
    }

    /**
     * Model represents the state of this Bean.
     * @return Null if the Bean doesn't need to get its model saved and restored automatically.
     */
    public MODEL getModel() {
        return model;
    }

    /**
     * Provides the type class of the model.
     * @return Implementing class should return the type class of the model that will be used by
     * this Bean to instantiate its model in {@link #onCreated()} and restores model in
     * {@link #restoreModel(Object)}. Returning null is allowed which means this Bean doesn't
     * have a model needs to be automatically saved and restored.
     */
    public abstract Class<MODEL> modelType();

    /**
     * Restores the model of this Bean.
     * <p>
     * Note that when {@link #modelType()} returns null, this method will have no effect.
     * </p>
     *
     * @param restoredModel The restored model by {@link StateKeeper} that will be rebound to the
     *                      Bean.
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
