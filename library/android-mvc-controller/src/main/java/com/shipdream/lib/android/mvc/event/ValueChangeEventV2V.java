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

package com.shipdream.lib.android.mvc.event;

public class ValueChangeEventV2V<T> extends BaseEventV2V{
    private final T lastValue;
    private final T currentValue;

    public ValueChangeEventV2V(Object sender, T lastValue, T currentValue) {
        super(sender);
        this.lastValue = lastValue;
        this.currentValue = currentValue;
    }

    public T getLastValue(){
        return lastValue;
    }

    public T getCurrentValue(){
        return currentValue;
    }
}
