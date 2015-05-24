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

package com.shipdream.lib.poke;

import javax.inject.Singleton;

/**
 * A component can provide providers by methods annotated by {@link com.shipdream.lib.poke.Provides}.
 * <p><strong>Note that: By default, provide methods annotated by {@link Singleton} will be
 * singleton in the scope of the component.</strong> For example, if ComponentA#ProvidesObjectX() is
 * annotated by {@link javax.inject.Singleton}, every time ComponentA#ProvidesObjectX() is called,
 * it provides the same instance cached by its {@link #getScopeCache()}. In other words the methods
 * annotated by {@link javax.inject.Singleton} could be relative singleton to this component if the
 * component has it's stand alone scope cache. Otherwise provide a custom scope cache to control how
 * to generate new instances.</p>
 */
public abstract class Component {
    private final ScopeCache scopeCache;

    /**
     * Constructor with a stand alone scope cache used only by this component
     */
    public Component() {
        this(new ScopeCache());
    }

    /**
     * Constructor with the given scope cache.
     * @param scopeCache The scope cache associated to this component.
     */
    public Component(ScopeCache scopeCache) {
        this.scopeCache = scopeCache;
    }

    /**
     * The scope cache that all providers registered by {@link Provides} with {@link javax.inject.Singleton}
     * will share.
     * @return
     */
    public ScopeCache getScopeCache() {
        return scopeCache;
    }

}
