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

package com.shipdream.lib.android.mvc;

import org.junit.Assert;
import org.junit.Test;

public class TestReason {
    @Test
    public void reason_should_be_set_and_read_correctly() {
        Reason reason = new Reason();
        reason.isNewInstance = true;
        reason.isFirstTime = true;
        reason.isRestored = true;
        reason.isRotated = true;
        reason.isPoppedOut = true;

        Assert.assertTrue(reason.isNewInstance());
        Assert.assertTrue(reason.isFirstTime());
        Assert.assertTrue(reason.isRestored());
        Assert.assertTrue(reason.isRotated());
        Assert.assertTrue(reason.isPoppedOut());

        reason.toString();

        reason.isNewInstance = false;
        reason.isFirstTime = false;
        reason.isRestored = false;
        reason.isRotated = false;
        reason.isPoppedOut = false;

        Assert.assertFalse(reason.isNewInstance());
        Assert.assertFalse(reason.isFirstTime());
        Assert.assertFalse(reason.isRestored());
        Assert.assertFalse(reason.isRotated());
        Assert.assertFalse(reason.isPoppedOut());
    }
}
