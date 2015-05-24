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
 * This provider uses default/empty constructor by provided class type to get dependencies. So
 * make sure the implementation has default public constructor
 */
public class ProviderByClassType<T> extends ProviderByClassName {
    /**
     * Construct a {@link ProviderByClassType} with {@link javax.inject.Qualifier}
     * @param type The contract of the implementation
     * @param implementationClass The class type of the implementation. It has to have a default
     *                            public constructor
     */
    public ProviderByClassType(Class<T> type, Class<? extends T> implementationClass) {
        super(type, implementationClass);
    }
}
