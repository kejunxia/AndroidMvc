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

import com.shipdream.lib.android.mvc.inject.test.Phone;
import com.shipdream.lib.android.mvc.inject.test.Robot;
import com.shipdream.lib.android.mvc.inject.test.Smart;

import org.junit.Test;

import javax.inject.Inject;

public class TestMvcComponent extends BaseTest {
    @Test(expected = MvcGraphException.class)
    public void should_throw_provider_missing_exception_when_locate_an_qualified_class() {
        class Shop {
            @Inject
            private Phone nexus6;
        }

        Mvc.graph().inject(new Shop());
    }

    @Test(expected = MvcGraphException.class)
    public void should_throw_provider_missing_exception_when_locate_an_unqualified_class() {
        class Shop {
            @Inject
            @Smart
            private Robot nexus6;
        }

        Mvc.graph().inject(new Shop());
    }
}
