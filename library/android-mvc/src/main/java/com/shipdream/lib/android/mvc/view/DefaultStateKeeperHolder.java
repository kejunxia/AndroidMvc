package com.shipdream.lib.android.mvc.view;

import android.os.Bundle;

import com.shipdream.lib.android.mvc.Injector;
import com.shipdream.lib.android.mvc.MvcBean;

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
            if (MvcBean.class.isAssignableFrom(field.getType())) {
                MvcBean mvcBean = null;
                try {
                    field.setAccessible(true);
                    mvcBean = (MvcBean) field.get(object);
                } catch (IllegalAccessException e) {
                    //ignore
                }
                stateKeeper.saveModel(mvcBean.getModel(), mvcBean.modelType());
            }
        }
    }

    static void restoreControllerStateByTheirOwn(Bundle savedState, Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        stateKeeper.bundle = savedState;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (MvcBean.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    MvcBean mvcBean = (MvcBean) field.get(object);
                    Object value = stateKeeper.retrieveModel(mvcBean.modelType());
                    mvcBean.restoreModel(value);
                } catch (IllegalAccessException e) {
                    //ignore
                }
            }
        }
    }
}
