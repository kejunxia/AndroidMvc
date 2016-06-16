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

package com.shipdream.lib.android.mvp.view;

import android.os.Bundle;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.shipdream.lib.android.mvc.ModelKeeper;
import com.shipdream.lib.android.mvc.manager.NavigationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultModelKeeper implements ModelKeeper {
    static final String MVC_SATE_PREFIX = "__--AndroidMvc:State:";

    private static Gson gson;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private AndroidStateKeeper navigationModelKeeper = new NavigationModelKeeper();
    AndroidStateKeeper customStateKeeper;
    Bundle bundle;

    DefaultModelKeeper() {
        gson = new GsonBuilder().create();
    }

    private static String getStateKey(String stateTypeName) {
        return MVC_SATE_PREFIX + stateTypeName;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> void saveModel(T model, Class<T> type) {
        if (type != null) {
            Parcelable parcelable = null;

            if (NavigationManager.Model.class == type) {
                //Use navigation model keeper to save state
                parcelable = navigationModelKeeper.saveState(model, type);
            } else {
                if (customStateKeeper != null) {
                    //Use customs state manager to restore state
                    parcelable = customStateKeeper.saveState(model, type);
                }
            }

            long ts = System.currentTimeMillis();
            if (parcelable != null) {
                String stateKey = getStateKey(type.getName());
                bundle.putParcelable(stateKey, parcelable);
                logger.trace("Save state by parcel state keeper - {}, {}ms used.",
                        type.getName(), System.currentTimeMillis() - ts);
            } else {
                //Use Gson to restore state
                String stateKey = getStateKey(type.getName());
                String json = gson.toJson(model);
                bundle.putString(stateKey, json);

                logger.trace("Save state by JSON - {}, {}ms used. Content: {}",
                        type.getName(), System.currentTimeMillis() - ts, json);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T retrieveModel(Class<T> type) {
        T state = null;
        if (type != null) {
            long ts = System.currentTimeMillis();
            String stateKey = getStateKey(type.getName());
            Object value = bundle.get(stateKey);
            Parcelable parcelable = null;
            if (value instanceof Parcelable) {
                parcelable = (Parcelable) value;
            }
            if (NavigationManager.Model.class == type) {
                //Use navigation model keeper to restore state
                state = (T) navigationModelKeeper.getState(parcelable, type);
                logger.trace("Restore state by parcel state keeper - {}, {}ms used.",
                        type.getName(), System.currentTimeMillis() - ts);
            } else {
                //Use custom state keeper or gson state keeper to restore state.
                if (customStateKeeper != null) {
                    if (parcelable != null) {
                        //Use custom state manager to restore state
                        state = (T) customStateKeeper.getState(parcelable, type);
                        logger.trace("Restore state by parcel state keeper - {}, {}ms used.",
                                type.getName(), System.currentTimeMillis() - ts);
                    }
                }
                if (state == null) {
                    //State is not restored successfully by custom state keeper nor navigation
                    //model keeper. So try to use Gson to restore state
                    state = deserialize(bundle, type);
                }
            }

            if (state == null) {
                throw new IllegalStateException("Can't find restore state for " + type.getName());
            }
        }
        return state;
    }

    private <T> T deserialize(Bundle outState, Class<T> type) {
        T state;
        long ts = System.currentTimeMillis();

        String stateKey = getStateKey(type.getName());
        String json = outState.getString(stateKey);
        try {
            //recover the model
            state = gson.fromJson(json, type);
            //rebind the model to the controller
        } catch (JsonSyntaxException exception) {
            String errorMessage = String.format(
                    "Failed to restore state(%s) by json deserialization", type.getName());
            throw new RuntimeException(errorMessage, exception);
        }

        logger.trace("Restore state by JSON - {}, {}ms used.",
                type.getName(), System.currentTimeMillis() - ts);

        return state;
    }
}