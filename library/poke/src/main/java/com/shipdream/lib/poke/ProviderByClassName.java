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

package com.shipdream.lib.poke;

import javax.inject.Qualifier;

/**
 * This provider uses default/empty constructor by provided class name to get dependencies. So
 * make sure the implementation has default public constructor
 */
public class ProviderByClassName<T> extends ProviderByClassType<T> {
    /**
     * Construct a provider binding the type and the implementation class type. The found
     * implementation class may be annotated by {@link Qualifier}.
     * @param type The contract of the implementation
     * @param implementationClassName The name of the implementation class. It must have a default
     *                            public constructor
     * @throws ClassNotFoundException Thrown if the class with the given name cannot be found
     */
    public ProviderByClassName(Class type, String implementationClassName)
            throws ClassNotFoundException {
        super(type, (Class<? extends T>) Class.forName(implementationClassName));
    }
}
