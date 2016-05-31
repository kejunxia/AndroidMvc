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

/**
 * This class holds a stateKeeper as a singleton.
 */
class ModelKeeperHolder {
    static MvpBeanKeeper stateKeeper;

    static {
        stateKeeper = new MvpBeanKeeper();
    }

    /**
     * Save model of all {@link Bean}s currently live in the {@link Injector#getGraph()}
     * @param outState the out state
     */
    static void saveAllModels(Bundle outState) {
        stateKeeper.bundle = outState;
        //TODO: need to fix
        Injector.getGraph().rootComponent.saveAllBeans(stateKeeper);
        stateKeeper.bundle = null;
    }

    /**
     * Restore model of all {@link Bean}s currently live in the {@link Injector#getGraph()}
     * @Bundle savedState the saved state
     */
    static void restoreAllModels(Bundle savedState) {
        stateKeeper.bundle = savedState;
        //TODO: need to fix
        Injector.getGraph().rootComponent.restoreAllBeans(stateKeeper);
        stateKeeper.bundle = null;
    }

}
