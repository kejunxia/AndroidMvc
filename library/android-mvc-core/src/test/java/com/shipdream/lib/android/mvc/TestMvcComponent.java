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
import com.shipdream.lib.poke.Provides;

import org.junit.Assert;
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

    private class Car{
    }

    public class Tourist {
        @Inject
        private Car car;
    }

    @Test
    public void test_injection_with_different_component_for_different_scoping() throws Exception{
        //A component with a cache by default
        MvcComponent componentWithCache = new MvcComponent("SmallLender");
        componentWithCache.register(new Object(){
            @Provides
            public Car car() {
                return new Car();
            }
        });
        //Attach the component
        Mvc.graph().getRootComponent().attach(componentWithCache);

        Tourist tourist1 = new Tourist();
        Mvc.graph().inject(tourist1);

        Tourist tourist2 = new Tourist();
        Mvc.graph().inject(tourist2);

        //Tourist1 and tourist2 should borrow the same car
        //Because the component providing car instances has a scope cache
        Assert.assertTrue(tourist1.car == tourist2.car);

        //Detach the component
        Mvc.graph().getRootComponent().detach(componentWithCache);

        boolean useScopeCache = false;
        MvcComponent componentWithoutCache = new MvcComponent("BigLender", useScopeCache);
        componentWithoutCache.register(new Object(){
            @Provides
            public Car car() {
                return new Car();
            }
        });
        //Attach the component
        Mvc.graph().getRootComponent().attach(componentWithoutCache);

        Tourist tourist3 = new Tourist();
        Mvc.graph().inject(tourist3);

        Tourist tourist4 = new Tourist();
        Mvc.graph().inject(tourist4);

        //tourist3 and tourist4 should borrow the same car
        //Because the component providing car instances doesn't use a scope cache
        Assert.assertTrue(tourist3.car != tourist4.car);
    }
}
