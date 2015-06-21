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

package com.shipdream.lib.poke.exception;

import com.shipdream.lib.poke.Graph;
import com.shipdream.lib.poke.Provider;

/**
 * Occurs when there are multiple {@link Provider} linked to the same inject type in {@link Graph}
 * that are with the same type and qualifier
 */
public class ProviderConflictException extends PokeException {
    public ProviderConflictException(String message) {
        super(message);
    }
}
