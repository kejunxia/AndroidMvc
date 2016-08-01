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

import android.os.Bundle;

import com.shipdream.lib.poke.Component;

import java.util.Map;

/**
 * This class holds a stateKeeper as a singleton.
 */
class MvcStateKeeperHolder {
    static MvcStateKeeper stateKeeper;

    static {
        stateKeeper = new MvcStateKeeper();
    }

    /**
     * Save model of all {@link Bean}s currently live in the {@link Mvc#graph()}
     * @param outState the out state
     */
    static void saveState(Bundle outState) {
        stateKeeper.bundle = outState;
        MvcComponent root = getRootComponent();
        doSaveState(root);

        stateKeeper.bundle = null;
    }

    private static void doSaveState(Component component) {
        if (component.getChildrenComponents() != null
                && !component.getChildrenComponents().isEmpty()) {
            for (Component childComponent : component.getChildrenComponents()) {
                doSaveState(childComponent);
            }
        }

        Map<String, Object> cache = component.getCache();

        for (String key : cache.keySet()) {
            Object v = cache.get(key);
            if (v != null && v instanceof Bean) {
                Bean bean = (Bean) v;

                if (bean.modelType() != null) {
                    stateKeeper.saveState(key, bean.getModel());
                }
            }
        }
    }

    /**
     * Restore model of all {@link Bean}s currently live in the {@link Mvc#graph()}
     * @Bundle savedState the saved state
     */
    static void restoreState(Bundle savedState) {
        stateKeeper.bundle = savedState;

        MvcComponent root = getRootComponent();
        doRestoreState(root);

        stateKeeper.bundle = null;
    }

    private static void doRestoreState(Component component) {
        if (component.getChildrenComponents() != null
                && !component.getChildrenComponents().isEmpty()) {
            for (Component child : component.getChildrenComponents()) {
                doRestoreState(child);
            }
        }

        Map<String, Object> cache = component.getCache();
        for (String key : cache.keySet()) {
            Object v = cache.get(key);
            if (v instanceof Bean) {
                Bean bean = (Bean) v;
                if (bean.modelType() != null) {
                    Object model = stateKeeper.restoreState(key, bean.modelType());

                    bean.restoreModel(model);
                }
            }
        }
    }

    private static MvcComponent getRootComponent() {
        Component root = Mvc.graph().getRootComponent();
        if (root == null && root instanceof MvcComponent) {
            throw new IllegalStateException("RootComponent of MvcGraph must inherit MvcComponent");
        }
        MvcComponent mvcComponent = (MvcComponent) root;
        return mvcComponent;
    }

}
