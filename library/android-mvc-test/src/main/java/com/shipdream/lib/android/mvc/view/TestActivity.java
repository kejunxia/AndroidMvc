package com.shipdream.lib.android.mvc.view;

import android.os.Bundle;

import com.shipdream.lib.android.mvc.MvcActivity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by kejun on 6/07/2016.
 */

public abstract class TestActivity extends MvcActivity {
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

    private State state;

    public State getState() {
        return state;
    }

    private List<Proxy> proxies = new CopyOnWriteArrayList<>();;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state = State.CREATE;
        for (Proxy proxy : proxies) {
            proxy.onCreate();
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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
    }


}
