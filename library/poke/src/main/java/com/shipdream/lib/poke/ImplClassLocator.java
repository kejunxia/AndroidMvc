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

/**
 * Interface to locate implementation class of the given contract class.
 */
public abstract class ImplClassLocator {
    /**
     * Locate implementation class of the given contract class.
     * @param contract
     * @param <T>
     * @param <S>
     * @return
     */
    public abstract <T, S extends T> Class<S> locateImpl(Class<T> contract) throws ImplClassNotFoundException;

    /**
     * Define the {@link ScopeCache} for the injectable contract located by this
     * {@link ImplClassLocator}
     * @return
     */
    public abstract ScopeCache getScopeCache();
}
