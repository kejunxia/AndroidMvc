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

import java.lang.annotation.Annotation;

import javax.inject.Named;

class PokeHelper {
    static String makeProviderKey(Class type, Annotation qualifier) {
        String qualifierStr;
        if (qualifier == null) {
            qualifierStr = "@null";
        } else {
            if (qualifier.annotationType() == Named.class) {
                qualifierStr = qualifier.toString() + ":" + ((Named) qualifier).value();
            } else {
                qualifierStr = qualifier.toString();
            }
        }
        return type.getName() + qualifierStr;
    }

}
