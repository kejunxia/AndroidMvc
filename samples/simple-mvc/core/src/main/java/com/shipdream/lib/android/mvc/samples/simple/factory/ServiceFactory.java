package com.shipdream.lib.android.mvc.samples.simple.factory;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by kejun on 11/07/2016.
 */

public class ServiceFactory {
    private Retrofit retrofit;

    public <SERVICE> SERVICE createService(Class<SERVICE> clazz) {
        return retrofit().create(clazz);
    }

    private Retrofit retrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.ipify.org/").build();
        }
        return retrofit;
    }
}
