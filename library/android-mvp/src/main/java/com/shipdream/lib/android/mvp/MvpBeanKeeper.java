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
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MvpBeanKeeper implements BeanKeeper {
    private static Gson gson;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private AndroidModelKeeper navigationModelKeeper = new NavigationModelKeeperModelKeeper();
    AndroidModelKeeper customModelKeeper;
    Bundle bundle;

    MvpBeanKeeper() {
        gson = new GsonBuilder().create();
    }

    private static String getModelKey(String modelTypeName) {
        return AndroidMvp.MVP_SATE_PREFIX + modelTypeName.replace("com.shipdream.lib.android.graph", "graph");
    }

    //TODO: first param should be Bean and bean's model should be saved recursively
    @Override
    public void saveBean(Bean bean) {
        Class type = bean.modelType();
        if (type != null) {
            Parcelable parcelable = null;

            Object model = bean.getModel();

            if (NavigationManager.Model.class == type) {
                //Use navigation model keeper to save model
                parcelable = navigationModelKeeper.saveModel(model, type);
            } else {
                if (customModelKeeper != null) {
                    //Use customs model manager to restore model
                    parcelable = customModelKeeper.saveModel(model, type);
                }
            }

            long ts = System.currentTimeMillis();
            if (parcelable != null) {
                String modelKey = getModelKey(type.getName());
                bundle.putParcelable(modelKey, parcelable);
                logger.trace("Save model by parcel model keeper - {}, {}ms used.",
                        type.getName(), System.currentTimeMillis() - ts);
            } else {
                //Use Gson to restore model
                String modelKey = getModelKey(type.getName());
                String json = gson.toJson(model);
                bundle.putString(modelKey, json);

                logger.trace("Save model by JSON - {}, {}ms used. Content: {}",
                        type.getName(), System.currentTimeMillis() - ts, json);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T retrieveBean(Class<T> type) {
        T model = null;
        if (type != null) {
            long ts = System.currentTimeMillis();
            String modelKey = getModelKey(type.getName());
            Object value = bundle.get(modelKey);
            Parcelable parcelable = null;
            if (value instanceof Parcelable) {
                parcelable = (Parcelable) value;
            }
            if (NavigationManager.Model.class == type) {
                //Use navigation model keeper to restore model
                model = (T) navigationModelKeeper.getModel(parcelable, type);
                logger.trace("Restore model by parcel model keeper - {}, {}ms used.",
                        type.getName(), System.currentTimeMillis() - ts);
            } else {
                //Use custom model keeper or gson model keeper to restore model.
                if (customModelKeeper != null) {
                    if (parcelable != null) {
                        //Use custom model manager to restore model
                        model = (T) customModelKeeper.getModel(parcelable, type);
                        logger.trace("Restore model by parcel model keeper - {}, {}ms used.",
                                type.getName(), System.currentTimeMillis() - ts);
                    }
                }
                if (model == null) {
                    //Model is not restored successfully by custom model keeper nor navigation
                    //model keeper. So try to use Gson to restore model
                    model = deserialize(bundle, type);
                }
            }

            if (model == null) {
                throw new IllegalStateException("Can't find restore model for " + type.getName());
            }
        }
        return model;
    }

    private <T> T deserialize(Bundle outState, Class<T> type) {
        T model;
        long ts = System.currentTimeMillis();

        String modelKey = getModelKey(type.getName());
        String json = outState.getString(modelKey);
        try {
            //recover the model
            model = gson.fromJson(json, type);
            //rebind the model to the controller
        } catch (JsonSyntaxException exception) {
            String errorMessage = String.format(
                    "Failed to restore model(%s) by json deserialization", type.getName());
            throw new RuntimeException(errorMessage, exception);
        }

        logger.trace("Restore model by JSON - {}, {}ms used.",
                type.getName(), System.currentTimeMillis() - ts);

        return model;
    }
}