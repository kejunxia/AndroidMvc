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

import android.os.Bundle;

import java.lang.reflect.Field;

import javax.inject.Inject;

/**
 * This class holds a stateKeeper as a singleton.
 */
class ModelKeeperHolder {
    static MvpModelKeeper stateKeeper;

    static {
        stateKeeper = new MvpModelKeeper();
    }

    /**
     * Save model of all {@link Bean}s currently live in the {@link Injector#getGraph()}
     * @param outState the out state
     */
    static void saveAllModels(Bundle outState) {
        stateKeeper.bundle = outState;
        Injector.getGraph().saveAllModels(stateKeeper);
        stateKeeper.bundle = null;
    }

    /**
     * Restore model of all {@link Bean}s currently live in the {@link Injector#getGraph()}
     * @Bundle savedState the saved state
     */
    static void restoreAllModels(Bundle savedState) {
        stateKeeper.bundle = savedState;
        Injector.getGraph().restoreAllModels(stateKeeper);
        stateKeeper.bundle = null;
    }

    /**
     * Save model of all {@link Bean}s held by fields marked by @{@link Inject} of the given object
     * @param outState The out state
     * @param object The object whose fields marked by @{@link Inject} will be saved.
     */
    static void saveModelOfInjectedMvcBeanFields(Bundle outState, Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        stateKeeper.bundle = outState;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (Bean.class.isAssignableFrom(field.getType())) {
                Bean bean = null;
                try {
                    field.setAccessible(true);
                    bean = (Bean) field.get(object);
                } catch (IllegalAccessException e) {
                    //ignore
                }
                stateKeeper.saveModel(bean.getModel(), bean.modelType());
            }
        }
    }

    /**
     * Restore model of all {@link Bean}s held by fields marked by @{@link Inject} of the given object
     * @param savedState The saved state
     * @param object The object whose fields marked by @{@link Inject} will be restored.
     */
    static void restoreModelOfInjectedMvcBeanFields(Bundle savedState, Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        stateKeeper.bundle = savedState;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (Bean.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    Bean bean = (Bean) field.get(object);
                    Object value = stateKeeper.retrieveModel(bean.modelType());
                    bean.restoreModel(value);
                } catch (IllegalAccessException e) {
                    //ignore
                }
            }
        }
    }
}
