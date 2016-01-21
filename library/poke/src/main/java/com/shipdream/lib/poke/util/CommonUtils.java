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

package com.shipdream.lib.poke.util;

public class CommonUtils {
    /**
     * Check if the two of the given objects are equal with their {@link Object#equals(Object)}
     * methods. It's safe to pass NULL as the objects, and when they are both NULL they will be
     * considered equal.
     * @param obj1
     * @param obj2
     * @return
     */
    public static boolean areObjectsEqual(Object obj1, Object obj2) {
        boolean equal = false;
        if(obj1 == null && obj2 == null) {
            equal = true;
        } else {
            if(obj1 != null) {
                if(obj1.equals(obj2)) {
                    equal = true;
                }
            }
        }
        return equal;
    }
}
