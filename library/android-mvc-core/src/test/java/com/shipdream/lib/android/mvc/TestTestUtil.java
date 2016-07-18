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

import static org.mockito.Mockito.mock;

public class TestTestUtil {
    @Test
    public void can_construct_testUtil() {
        new TestUtil();
    }

    @Test
    public void should_assign_controller_view_correctly() {
        Controller controller = new Controller() {
            @Override
            public Class modelType() {
                return null;
            }
        };
        UiView view = mock(UiView.class);

        TestUtil.assignControllerView(controller, view);

        Assert.assertTrue(controller.view == view);
    }
}
