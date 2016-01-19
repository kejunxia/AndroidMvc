package com.shipdream.lib.android.mvc.view;

import android.os.Bundle;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.shipdream.lib.android.mvc.ModelKeeper;
import com.shipdream.lib.android.mvc.controller.NavigationController;

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

            if (NavigationController.Model.class == type) {
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
            if (NavigationController.Model.class == type) {
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