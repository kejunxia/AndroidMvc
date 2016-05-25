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

import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestComponentManagement extends BaseTestCases {
    private Graph graph;
    private Component rootComponent;

    class Car {
        @MyInject
        Wheel wheel;
    }

    interface Wheel {

    }

    static class Wheel17Inch implements Wheel {

    }

    @Before
    public void setUp() throws Exception {
        rootComponent = new Component("AppSingleton");
        graph = new Graph();
        graph.setRootComponent(rootComponent);
    }

    @Test
    public void graph_should_use_corresponding_sub_components_scope_to_inject_unscoped() throws PokeException {
        Component childCom = new Component();
        childCom.register(new ProviderByClassType(Wheel.class, Wheel17Inch.class));
        rootComponent.attach(childCom);

        Car car = new Car();
        graph.inject(car, MyInject.class);

        Car car2 = new Car();
        graph.inject(car2, MyInject.class);

        Assert.assertTrue(car.wheel != car2.wheel);
    }

    @Test
    public void graph_should_use_corresponding_sub_components_scope_to_inject_scoped() throws PokeException {
        Component childCom = new Component("SubScope");
        childCom.register(new ProviderByClassType(Wheel.class, Wheel17Inch.class));
        rootComponent.attach(childCom);

        Car car = new Car();
        graph.inject(car, MyInject.class);

        Car car2 = new Car();
        graph.inject(car2, MyInject.class);

        Assert.assertTrue(car.wheel == car2.wheel);
    }

    @Test
    public void graph_is_able_to_inject_from_new_attached_sub_components() throws PokeException {
        Component childCom = new Component();
        childCom.register(new ProviderByClassType(Wheel.class, Wheel17Inch.class));

        Car car = new Car();

        boolean hasProviderMissingException = false;
        try {
            graph.inject(car, MyInject.class);
        } catch (ProviderMissingException e) {
            hasProviderMissingException = true;
        }
        Assert.assertTrue(hasProviderMissingException);
        Assert.assertNull(car.wheel);

        rootComponent.attach(childCom);

        graph.inject(car, MyInject.class);

        Assert.assertNotNull(car.wheel);
    }

    @Test
    public void graph_unable_inject_from_new_detached_sub_components() throws PokeException {
        Component childCom = new Component();
        childCom.register(new ProviderByClassType(Wheel.class, Wheel17Inch.class));

        Car car = new Car();

        rootComponent.attach(childCom);

        graph.inject(car, MyInject.class);

        Assert.assertNotNull(car.wheel);

        rootComponent.detach(childCom);

        boolean hasProviderMissingException = false;
        try {
            graph.inject(car, MyInject.class);
        } catch (ProviderMissingException e) {
            hasProviderMissingException = true;
        }
        Assert.assertTrue(hasProviderMissingException);
    }

    @Test
    public void graph_is_able_to_inject_from_sub_component_new_added_provider() throws PokeException {
        Component childCom = new Component();
        Car car = new Car();

        boolean hasProviderMissingException = false;
        try {
            graph.inject(car, MyInject.class);
        } catch (ProviderMissingException e) {
            hasProviderMissingException = true;
        }
        Assert.assertTrue(hasProviderMissingException);
        Assert.assertNull(car.wheel);

        rootComponent.attach(childCom);
        hasProviderMissingException = false;
        try {
            graph.inject(car, MyInject.class);
        } catch (ProviderMissingException e) {
            hasProviderMissingException = true;
        }
        Assert.assertTrue(hasProviderMissingException);
        Assert.assertNull(car.wheel);

        childCom.register(new ProviderByClassType(Wheel.class, Wheel17Inch.class));
        graph.inject(car, MyInject.class);
        Assert.assertNotNull(car.wheel);
    }

    @Test
    public void graph_is_unable_to_inject_from_sub_component_with_removed_provider() throws PokeException {
        Component childCom = new Component();
        Car car1 = new Car();
        rootComponent.attach(childCom);

        childCom.register(new ProviderByClassType(Wheel.class, Wheel17Inch.class));
        graph.inject(car1, MyInject.class);
        Assert.assertNotNull(car1.wheel);

        childCom.unregister(Wheel.class, null);

        Car car2 = new Car();
        boolean hasProviderMissingException = false;
        try {
            graph.inject(car2, MyInject.class);
        } catch (ProviderMissingException e) {
            hasProviderMissingException = true;
        }
        Assert.assertTrue(hasProviderMissingException);
        Assert.assertNull(car2.wheel);
    }

    @Test
    public void graph_is_able_to_inject_from_reattached_sub_component() throws PokeException {
        Component childCom = new Component("MyScope");
        rootComponent.attach(childCom);
        childCom.register(new ProviderByClassType(Wheel.class, Wheel17Inch.class));

        rootComponent.detach(childCom);

        Graph graph1 = new Graph();
        graph1.setRootComponent(childCom);

        Car car = new Car();
        boolean hasProviderMissingException = false;
        try {
            graph.inject(car, MyInject.class);
        } catch (ProviderMissingException e) {
            hasProviderMissingException = true;
        }
        Assert.assertTrue(hasProviderMissingException);
        Assert.assertNull(car.wheel);

        graph1.inject(car, MyInject.class);
        Assert.assertNotNull(car.wheel);

        rootComponent.attach(childCom);
        Car car2 = new Car();
        graph.inject(car2, MyInject.class);
        Assert.assertNotNull(car2.wheel);

        Assert.assertTrue(car2.wheel == car.wheel);
    }

    @Test(expected = Component.MismatchDetachException.class)
    public void should_throw_exception_when_detach_wrong_child_component() throws PokeException {
        Component childCom1 = new Component();
        Component parentCom = new Component();
        parentCom.attach(childCom1);
        rootComponent.detach(childCom1);
    }

    @Test(expected = Component.MismatchDetachException.class)
    public void should_throw_exception_when_detach_orphan_child_component() throws PokeException {
        Component childCom1 = new Component();
        rootComponent.detach(childCom1);
    }

    @Test
    public void should_throw_exception_when_set_non_root_component_to_graph() throws PokeException {
        Graph g = new Graph();

        Component component = new Component();

        boolean exp = false;
        Component parent = new Component();

        parent.attach(component);

        try {
            g.setRootComponent(component);
        } catch (Graph.IllegalGraphComponentException e) {
            exp = true;
        }

        Assert.assertTrue(exp);

        //OK to set parent component which is unattached component
        g.setRootComponent(parent);
    }

    @Test
    public void should_throw_exception_add_attached_child_component() throws PokeException {
        Graph g = new Graph();

        Component component = new Component();
        g.setRootComponent(component);

        boolean exp = false;
        Component parent = new Component();

        parent.attach(component);
        try {
            rootComponent.attach(component);
        } catch (Component.AlreadyAttachedException e) {
            exp = true;
        }

        Assert.assertTrue(exp);

        rootComponent.attach(parent);
    }
}
