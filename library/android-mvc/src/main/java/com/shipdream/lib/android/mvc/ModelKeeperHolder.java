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

/**
 * This class holds a stateKeeper as a singleton.
 */
class ModelKeeperHolder {
    static MvcStateKeeper stateKeeper;

    static {
        stateKeeper = new MvcStateKeeper();
    }

    /**
     * Save model of all {@link Bean}s currently live in the {@link Mvc#graph()}
     * @param outState the out state
     */
    static void saveAllModels(Bundle outState) {
        stateKeeper.bundle = outState;
        getRootComponent().saveState(stateKeeper);
        stateKeeper.bundle = null;
    }

    /**
     * Restore model of all {@link Bean}s currently live in the {@link Mvc#graph()}
     * @Bundle savedState the saved state
     */
    static void restoreAllModels(Bundle savedState) {
        stateKeeper.bundle = savedState;
        getRootComponent().restoreState(stateKeeper);
        stateKeeper.bundle = null;
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
