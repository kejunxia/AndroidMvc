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

package com.shipdream.lib.poke;

import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;
import org.junit.Test;

import javax.inject.Singleton;

@SuppressWarnings("unchecked")
public class TestInjectionReferenceCount2 extends BaseTestCases {

    interface ControllerA {

    }

    static class ControllerAImpl implements ControllerA {
        @MyInject
        ManageA manageA;
    }

    interface ManageA {

    }

    static class ManageAImpl implements ManageA {

    }

    static class ViewA {
        @MyInject
        ControllerA controllerA;
    }

    static class ViewB {
        @MyInject
        ControllerA controllerA;
    }

    static class ViewC {
        @MyInject
        ControllerA controllerA;
    }

    static class TestComp extends Component {
        @Singleton
        @Provides
        public ControllerA providesController() {
            return new ControllerAImpl();
        }

        @Singleton
        @Provides
        public ManageA providersManageA() {
            return new ManageAImpl();
        }
    }

    @Test
    public void should_release_nested_injection_until_last_instance_dereferenced() throws
            ProvideException, ProviderConflictException, CircularDependenciesException,
            ProviderMissingException {
        ViewA viewA = new ViewA();
        ViewB viewB = new ViewB();
        ViewC viewC = new ViewC();

        SimpleGraph graph = new SimpleGraph();
        Component component = new TestComp();
        graph.register(component);

        graph.inject(viewA, MyInject.class);
        ControllerA controllerAInViewA = viewA.controllerA;
        ManageA manageAInViewA = ((ControllerAImpl)viewA.controllerA).manageA;

        graph.inject(viewB, MyInject.class);
        ControllerA controllerAInViewB = viewB.controllerA;
        ManageA manageAInViewB = ((ControllerAImpl)viewA.controllerA).manageA;

        graph.inject(viewC, MyInject.class);
        ControllerA controllerAInViewC = viewB.controllerA;
        ManageA manageAInViewC = ((ControllerAImpl)viewA.controllerA).manageA;

        Assert.assertTrue(manageAInViewA == manageAInViewB);

        //ControllerA released first time by viewB but still referenced by viewA
        //So controllerA should not be freed so does the manageA held by ControllerA
        graph.release(viewC, MyInject.class);
        Assert.assertTrue(graph.getProvider(ControllerA.class, null).get() == controllerAInViewA);
        Assert.assertTrue(graph.getProvider(ManageA.class, null).get() == manageAInViewA);
        Assert.assertEquals(2, graph.getProvider(ManageA.class, null).getReferenceCount());

        graph.release(viewB, MyInject.class);
        Assert.assertTrue(graph.getProvider(ControllerA.class, null).get() == controllerAInViewA);
        Assert.assertTrue(graph.getProvider(ManageA.class, null).get() == manageAInViewA);
        Assert.assertEquals(1, graph.getProvider(ManageA.class, null).getReferenceCount());

        graph.release(viewA, MyInject.class);
        Assert.assertTrue(graph.getProvider(ControllerA.class, null).get() != controllerAInViewA);
        Assert.assertTrue(graph.getProvider(ManageA.class, null).get() != manageAInViewA);
        Assert.assertEquals(0, graph.getProvider(ManageA.class, null).getReferenceCount());
    }

}
