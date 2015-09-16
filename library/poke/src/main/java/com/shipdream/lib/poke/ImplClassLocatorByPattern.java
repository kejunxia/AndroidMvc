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

public class ImplClassLocatorByPattern extends ImplClassLocator {
    private final ScopeCache scopeCache;

    /**
     * Construct class locator without scope cache thus all instances will be created on each
     * injection
     */
    public ImplClassLocatorByPattern() {
        this(null);
    }

    /**
     * Construct class locator with given scope cache.
     * @param scopeCache
     */
    public ImplClassLocatorByPattern(ScopeCache scopeCache) {
        this.scopeCache = scopeCache;
    }

    @Override
    public <T, S extends T> Class<S> locateImpl(Class<T> contract) throws ImplClassNotFoundException {
        String pkg = contract.getPackage().getName();
        String implClassName = pkg + ".internal." + contract.getSimpleName() + "Impl";
        try {
            return (Class<S>) Class.forName(implClassName);
        } catch (ClassNotFoundException e) {
            throw new ImplClassNotFoundException("Can't find implementation class for " + contract.getName(), e);
        }
    }

    @Override
    public ScopeCache getScopeCache() {
        return scopeCache;
    }
}
