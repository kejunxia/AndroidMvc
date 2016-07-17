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
import android.util.Log;

import com.shipdream.lib.android.mvc.event.bus.EventBus;
import com.shipdream.lib.android.mvc.event.bus.annotation.EventBusV;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

/**
 * Created by kejun on 6/07/2016.
 */

public abstract class TestActivity extends MvcActivity {
    private static final String EXTRA_BRING_BACK_SENDER = "Intent_BringBackSender";

    public interface Event {
        class OnFragmentsResumed {
            public String sender;
            public OnFragmentsResumed(String sender) {
                this.sender = sender;
            }
        }
    }

    public enum State{
        CREATE,
        RESUME,
        RESUME_FRAGMENTS,
        PAUSE,
        SAVE_INSTANCE_STATE,
        DESTROY
    }

    public static class Proxy {
        protected void onCreate(){}
        protected void onResume(){}
        protected void onResumeFragments(){}
        protected void onPause(){}
        protected void onSaveInstanceState(){}
        protected void onDestroy(){}
    }

    @Inject
    @EventBusV
    private EventBus eventBusV;

    private State state;

    public State getState() {
        return state;
    }

    private List<Proxy> proxies = new CopyOnWriteArrayList<>();;

    public DelegateFragment getDelegateFragment() {
        return delegateFragment;
    }

    public void addProxy(Proxy proxy) {
        synchronized (proxies) {
            proxies.add(proxy);
        }
    }

    public void removeProxy(Proxy proxy) {
        synchronized (proxies) {
            proxies.remove(proxy);
        }
    }

    private String bringBackSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Mvc.graph().inject(this);

        super.onCreate(savedInstanceState);
        state = State.CREATE;
        for (Proxy proxy : proxies) {
            proxy.onCreate();
        }

        if (bringBackSender == null &&
                savedInstanceState != null && savedInstanceState.containsKey(EXTRA_BRING_BACK_SENDER)) {
            bringBackSender = savedInstanceState.getString(EXTRA_BRING_BACK_SENDER);
            Log.v("TrackLifeSync:BringBack", "Ticket found: " + bringBackSender);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        state = State.RESUME;
        for (Proxy proxy : proxies) {
            proxy.onResume();
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        state = State.RESUME_FRAGMENTS;
        for (Proxy proxy : proxies) {
            proxy.onResumeFragments();
        }

        if (bringBackSender != null) {
            Log.v("TrackLifeSync:BringBack", "Send skip event for: " + bringBackSender);
            eventBusV.post(new Event.OnFragmentsResumed(bringBackSender));
            bringBackSender = null;
        }
    }

    public static String ticket = null;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ticket != null) {
            outState.putString(EXTRA_BRING_BACK_SENDER, ticket);
        }

        state = State.SAVE_INSTANCE_STATE;
        for (Proxy proxy : proxies) {
            proxy.onSaveInstanceState();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        state = State.PAUSE;
        for (Proxy proxy : proxies) {
            proxy.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        state = State.DESTROY;
        for (Proxy proxy : proxies) {
            proxy.onDestroy();
        }

        Mvc.graph().release(this);
    }


}
