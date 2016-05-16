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

/**
 * This class holds a stateKeeper as a singleton.
 */
class DefaultStateKeeperHolder {
    static DefaultModelKeeper stateKeeper;

    static {
        stateKeeper = new DefaultModelKeeper();
    }

    static void saveStateOfAllControllers(Bundle outState) {
        stateKeeper.bundle = outState;
        Injector.getGraph().saveAllModels(stateKeeper);
        stateKeeper.bundle = null;
    }

    static void restoreStateOfAllControllers(Bundle savedState) {
        stateKeeper.bundle = savedState;
        Injector.getGraph().restoreAllModels(stateKeeper);
        stateKeeper.bundle = null;
    }

    static void saveControllerStateOfTheirOwn(Bundle outState, Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        stateKeeper.bundle = outState;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (MvpBean.class.isAssignableFrom(field.getType())) {
                MvpBean mvpBean = null;
                try {
                    field.setAccessible(true);
                    mvpBean = (MvpBean) field.get(object);
                } catch (IllegalAccessException e) {
                    //ignore
                }
                stateKeeper.saveModel(mvpBean.getModel(), mvpBean.modelType());
            }
        }
    }

    static void restoreControllerStateByTheirOwn(Bundle savedState, Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        stateKeeper.bundle = savedState;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (MvpBean.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    MvpBean mvpBean = (MvpBean) field.get(object);
                    Object value = stateKeeper.retrieveModel(mvpBean.modelType());
                    mvpBean.restoreModel(value);
                } catch (IllegalAccessException e) {
                    //ignore
                }
            }
        }
    }
}
