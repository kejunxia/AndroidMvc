package com.shipdream.lib.android.mvc;

import com.shipdream.lib.android.mvc.view.AndroidMvc;
import com.shipdream.lib.poke.ScopeCache;

public class MvcGraphBridge {
    public static void hijackCache(ScopeCache scopeCache) {
        AndroidMvc.graph().hijack(scopeCache);
    }
}
