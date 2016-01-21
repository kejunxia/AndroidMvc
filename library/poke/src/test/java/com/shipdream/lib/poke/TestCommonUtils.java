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

import com.shipdream.lib.poke.util.CommonUtils;

import org.junit.Assert;
import org.junit.Test;

public class TestCommonUtils {
    @Test
    public void should_compare_object_equality_correctly() {
        Assert.assertTrue(CommonUtils.areObjectsEqual(null, null));
        Assert.assertFalse(CommonUtils.areObjectsEqual(null, new Object()));
        Assert.assertFalse(CommonUtils.areObjectsEqual(new Object(), null));

        Object a = new Object();
        Object b = new Object();

        Assert.assertFalse(CommonUtils.areObjectsEqual(a, b));
        Assert.assertFalse(CommonUtils.areObjectsEqual(b, a));
        Assert.assertFalse(CommonUtils.areObjectsEqual(null, a));
        Assert.assertFalse(CommonUtils.areObjectsEqual(a, null));

        String s1 = "abc";
        String s2 = "abc";

        Assert.assertTrue(CommonUtils.areObjectsEqual(s1, s2));
        Assert.assertTrue(CommonUtils.areObjectsEqual(s2, s1));

        Assert.assertFalse(CommonUtils.areObjectsEqual(null, s1));
        Assert.assertFalse(CommonUtils.areObjectsEqual(s1, null));
    }

    @Test
    public void should_be_able_to_create_commonUtils() {
        CommonUtils commonUtils = new CommonUtils();
    }
}
