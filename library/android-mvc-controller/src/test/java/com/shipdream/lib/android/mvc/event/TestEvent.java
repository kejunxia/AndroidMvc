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

import org.junit.Assert;
import org.junit.Test;

public class TestEvent {

    @Test
    public void should_return_past_and_current_value_given_in_constructor_for_ValueChangeEventC2C() {
        String v1 = "v1";
        String v2 = "v2";
        Object sender = new Object();
        ValueChangeEventC2C eventC2C = new ValueChangeEventC2C(sender, v1, v2);

        Assert.assertEquals(sender, eventC2C.getSender());
        Assert.assertEquals(v1, eventC2C.getLastValue());
        Assert.assertEquals(v2, eventC2C.getCurrentValue());
    }

    @Test
    public void should_return_past_and_current_value_given_in_constructor_for_ValueChangeEventC2V() {
        String v1 = "v1";
        String v2 = "v2";
        Object sender = new Object();
        ValueChangeEventC2V eventC2V = new ValueChangeEventC2V(sender, v1, v2);

        Assert.assertEquals(sender, eventC2V.getSender());
        Assert.assertEquals(v1, eventC2V.getLastValue());
        Assert.assertEquals(v2, eventC2V.getCurrentValue());
    }

    @Test
    public void should_return_past_and_current_value_given_in_constructor_for_ValueChangeEventV2V() {
        String v1 = "v1";
        String v2 = "v2";
        Object sender = new Object();
        ValueChangeEventV2V eventV2V = new ValueChangeEventV2V(sender, v1, v2);

        Assert.assertEquals(sender, eventV2V.getSender());
        Assert.assertEquals(v1, eventV2V.getLastValue());
        Assert.assertEquals(v2, eventV2V.getCurrentValue());
    }
}
