/*
 * Copyright 2015 Kejun Xia
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

package com.shipdream.lib.android.mvc.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.internal.bind.DateTypeAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.converter.ConversionException;
import retrofit.converter.GsonConverter;
import retrofit.mime.MimeUtil;
import retrofit.mime.TypedInput;

/**
 *
 */
public class RetrofitServiceFactory {
    /**
     * Listen to all request
     */
    interface OnRequestListener {
        void onRequest(RequestInterceptor.RequestFacade requestFacade);
    }

    /**
     * Listen to successful response which comes with status code in range [200, 300)
     */
    interface OnSuccessListener {
        void onSuccessfulResponse();
    }

    private String endpoint;
    private RestAdapter restAdapter;
    private Client client;
    private Gson gson;
    private FlexibleConverter jsonConverter;
    private List<OnRequestListener> onRequestListeners = new CopyOnWriteArrayList();
    private List<OnSuccessListener> onSuccessListeners = new CopyOnWriteArrayList();

    public void registerOnRequestListener(OnRequestListener listener) {
        onRequestListeners.add(listener);
    }

    public void unregisterOnRequestListener(OnRequestListener listener) {
        onRequestListeners.remove(listener);
    }

    public void registerOnResponseListener(OnSuccessListener listener) {
        onSuccessListeners.add(listener);
    }

    public void unregisterOnResponseListener(OnSuccessListener listener) {
        onSuccessListeners.remove(listener);
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        recreateRestAdapter();
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public <SERVICE> SERVICE createService(Class<SERVICE> serviceClass) {
        return restAdapter.create(serviceClass);
    }

    public RetrofitServiceFactory(Client client) {
        this(null, client);
    }

    public RetrofitServiceFactory(String endpoint, Client client) {
        this.endpoint = endpoint;
        this.client = client;

        if(this.endpoint != null) {
            gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                    .registerTypeAdapter(Date.class, new DateTypeAdapter())
                    .create();
            jsonConverter = new FlexibleConverter(gson, "UTF-8", onSuccessListeners);
            recreateRestAdapter();
        }
    }

    private void recreateRestAdapter() {
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(final RequestFacade request) {
                for(OnRequestListener listener : onRequestListeners) {
                    listener.onRequest(new RequestFacade() {
                        @Override
                        public void addHeader(String name, String value) {
                            request.addHeader(name, value);
                        }

                        @Override
                        public void addPathParam(String name, String value) {
                            request.addPathParam(name, value);
                        }

                        @Override
                        public void addEncodedPathParam(String name, String value) {
                            request.addEncodedPathParam(name, value);
                        }

                        @Override
                        public void addQueryParam(String name, String value) {
                            request.addQueryParam(name, value);
                        }

                        @Override
                        public void addEncodedQueryParam(String name, String value) {
                            request.addEncodedQueryParam(name, value);
                        }
                    });
                }
            }
        };
        restAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(requestInterceptor)
                .setClient(client)
                .setConverter(jsonConverter)
                .build();
    }

    public static class FlexibleConverter extends GsonConverter {
        private Logger logger = LoggerFactory.getLogger(getClass());
        private Gson gson;
        private List<OnSuccessListener> onSuccessListeners;
        private String encoding = "UTF-8";
        public FlexibleConverter(Gson gson, String encoding, List<OnSuccessListener> onSuccessListeners) {
            super(gson, encoding);
            if(this.encoding != null) {
                this.encoding = encoding;
            }
            this.onSuccessListeners = onSuccessListeners;
            this.gson = gson;
        }

        @Override
        public Object fromBody(TypedInput body, Type type) throws ConversionException {
            String charset = "UTF-8";
            if (type == String.class) {
                //return raw response string
                try {
                    String content = inputStreamToString(body.in(), charset);
                    logger.debug("Responding json to raw string : {}", content);

                    for(OnSuccessListener listener : onSuccessListeners) {
                        listener.onSuccessfulResponse();
                    }

                    return content;
                } catch (IOException e) {
                    throw new ConversionException("Reading body as string failed. Message: " + e.getMessage(), e);
                }
            } else {
                //map the response to given type

                if (body.mimeType() != null) {
                    charset = MimeUtil.parseCharset(body.mimeType(), "UTF-8");
                }
                InputStreamReader isr = null;
                try {
                    Object response = null;
                    if(logger.isDebugEnabled()) {
                        String content = inputStreamToString(body.in(), charset);
                        logger.debug("Responding json to object: {}", content);
                        response = gson.fromJson(content, type);
                    } else {
                        isr = new InputStreamReader(body.in(), charset);
                        response = gson.fromJson(isr, type);
                    }

                    for(OnSuccessListener listener : onSuccessListeners) {
                        listener.onSuccessfulResponse();
                    }

                    return response;
                } catch (IOException e) {
                    throw new ConversionException(e);
                } catch (JsonParseException e) {
                    throw new ConversionException(e);
                } finally {
                    if (isr != null) {
                        try {
                            isr.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        }

        private static String inputStreamToString(InputStream inputStream, String encoding)
                throws IOException{
            StringBuilder textBuilder = new StringBuilder();
            Charset charset = Charset.forName(encoding);
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, charset));
            try{
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }finally{
                inputStream.close();
            }
            return textBuilder.toString();
        }
    }
}
