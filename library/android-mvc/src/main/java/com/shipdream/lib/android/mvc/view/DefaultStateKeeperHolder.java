package com.shipdream.lib.android.mvc.view;

import android.os.Bundle;

import com.shipdream.lib.android.mvc.Injector;
import com.shipdream.lib.android.mvc.StateManaged;
import com.shipdream.lib.android.mvc.controller.BaseController;

import java.lang.reflect.Field;

/**
 * This class holds a stateKeeper as a singleton.
 */
class DefaultStateKeeperHolder {
    static DefaultStateKeeper stateKeeper;

    static {
        stateKeeper = new DefaultStateKeeper();
    }

    static void saveStateOfAllControllers(Bundle outState) {
        stateKeeper.bundle = outState;
        Injector.getGraph().saveAllStates(stateKeeper);
        stateKeeper.bundle = null;
    }

    static void restoreStateOfAllControllers(Bundle savedState) {
        stateKeeper.bundle = savedState;
        Injector.getGraph().restoreAllStates(stateKeeper);
        stateKeeper.bundle = null;
    }

    static void saveControllerStateOfTheirOwn(Bundle outState, Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        stateKeeper.bundle = outState;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (BaseController.class.isAssignableFrom(field.getType())) {
                StateManaged stateManaged = null;
                try {
                    field.setAccessible(true);
                    stateManaged = (StateManaged) field.get(object);
                } catch (IllegalAccessException e) {
                    //ignore
                }
                stateKeeper.saveState(stateManaged.getState(), stateManaged.getStateType());
            }
        }
    }

    static void restoreControllerStateByTheirOwn(Bundle savedState, Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        stateKeeper.bundle = savedState;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (BaseController.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    StateManaged stateManaged = (StateManaged) field.get(object);
                    Object value = stateKeeper.getState(stateManaged.getStateType());
                    stateManaged.restoreState(value);
                } catch (IllegalAccessException e) {
                    //ignore
                }
            }
        }
    }
}
