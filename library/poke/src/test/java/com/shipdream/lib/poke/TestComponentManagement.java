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

import com.shipdream.lib.poke.exception.CircularDependenciesException;
import com.shipdream.lib.poke.exception.PokeException;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;
import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

public class TestComponentManagement extends BaseTestCases {
    private Graph graph;
    private Component rootComponent;

    class Car {
        @MyInject
        Wheel wheel;
    }

    interface Wheel {

    }

    static class Wheel15Inch implements Wheel {
    }
    static class Wheel17Inch implements Wheel {
    }
    static class Wheel19Inch implements Wheel {
    }

    interface Engine{

    }

    static class EngineV4 implements Engine{
    }
    static class EngineV6 implements Engine {
    }

    @Before
    public void setUp() throws Exception {
        rootComponent = new Component("AppSingleton");
        graph = new Graph();
        graph.setRootComponent(rootComponent);
    }

    @Test
    public void graph_should_use_corresponding_sub_components_scope_to_inject_unscoped() throws PokeException {
        Component childCom = new Component(false);
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
        Component childCom = new Component("SubScope", true);
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

        Provider<Wheel> provider = new ProviderByClassType(Wheel.class, Wheel17Inch.class);
        Assert.assertNull(provider.getComponent());

        childCom.register(provider);
        Assert.assertNotNull(provider.getComponent());

        graph.inject(car1, MyInject.class);
        Assert.assertNotNull(car1.wheel);

        childCom.unregister(Wheel.class, null);

        Assert.assertNull(provider.getComponent());

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
    public void should_clear_overriding_providers_from_detached_child_component_correctly() throws PokeException {
        Graph graph = new Graph();
        graph.setRootComponent(rootComponent);

        rootComponent.register(new Object(){
            @Provides
            public Engine engine(){
                return new EngineV6();
            }
        });

        Component childCom = new Component("MyScope");
        childCom.register(new Object() {
            @Provides
            public Engine engine(){
                return new EngineV4();
            }

            @Provides
            public Wheel wheel() {
                return new Wheel15Inch();
            }
        });
        rootComponent.attach(childCom, true);

        Car car = new Car();
        graph.inject(car, MyInject.class);
        Assert.assertTrue(car.wheel instanceof Wheel15Inch);

        rootComponent.detach(childCom);

        childCom = new Component("MyScope2");
        rootComponent.attach(childCom, true);
        childCom.register(new Object() {
            @Provides
            public Wheel wheel() {
                return new Wheel17Inch();
            }
        });

        graph.inject(car, MyInject.class);
        Assert.assertTrue(car.wheel instanceof Wheel17Inch);
        rootComponent.detach(childCom);

        childCom = new Component("MyScope3");
        rootComponent.attach(childCom, true);
        childCom.register(new Object() {
            @Provides
            public Wheel wheel() {
                return new Wheel19Inch();
            }
        });
        car = new Car();
        graph.inject(car, MyInject.class);
        Assert.assertTrue(car.wheel instanceof Wheel19Inch);
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
        } catch (Graph.IllegalRootComponentException e) {
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
        } catch (Component.MultiParentException e) {
            exp = true;
        }

        Assert.assertTrue(exp);

        rootComponent.attach(parent);
    }

    @Test
    public void should_inject_from_sub_component_with_multi_level_depth() throws PokeException {
        Graph g = new Graph();
        Component root = new Component();
        g.setRootComponent(root);

        Component c1 = new Component();

        Component c2 = new Component("c2");
        c2.register(new ProviderByClassType(Wheel.class, Wheel17Inch.class));

        root.attach(c1);
        c1.attach(c2);

        Car car1 = new Car();
        g.inject(car1, MyInject.class);
        Assert.assertNotNull(car1.wheel);

        c1.detach(c2);
        Car car2 = new Car();
        boolean car2Exp = false;
        try {
            g.inject(car2, MyInject.class);
        } catch (ProviderMissingException e) {
            car2Exp = true;
        }
        Assert.assertTrue(car2Exp);
        Assert.assertNull(car2.wheel);

        root.attach(c2);
        Car car3 = new Car();
        g.inject(car3, MyInject.class);
        Assert.assertTrue(car1.wheel == car3.wheel);
        Assert.assertNotNull(car3.wheel);
    }

    @Test
    public void should_throw_conflict_exception_when_add_conflicting_provider_to_child_component() throws PokeException {
        Graph g = new Graph();
        Component root = new Component();
        g.setRootComponent(root);

        root.register(new ProviderByClassType(Wheel.class, Wheel17Inch.class));

        Component c2 = new Component("c2");
        root.attach(c2);

        boolean exp = false;
        try {
            c2.register(new ProviderByClassType(Wheel.class, Wheel17Inch.class));
        } catch (ProviderConflictException e) {
            exp = true;
        }

        Assert.assertTrue(exp);
    }

    @Test
    public void test_component_constructor() {
        Component c1 = new Component("A");
        Assert.assertNotNull(c1.getCache());
        Assert.assertEquals("A", c1.getName());

        Component c2 = new Component("B", false);
        Assert.assertNull(c2.getCache());
        Assert.assertEquals("B", c2.getName());

        Component c3 = new Component(false);
        Assert.assertNull(c3.getCache());
        Assert.assertEquals(null, c3.getName());

        Component c4 = new Component();
        Assert.assertNotNull(c4.getCache());
        Assert.assertEquals(null, c4.getName());
    }

    @Test
    public void should_detect_provider_conflict_when_attaching_child_component() throws ProviderConflictException {
        Component c1 = new Component("A");
        c1.register(new ProviderByClassType(String.class, String.class));

        Component c2 = new Component();
        c2.register(new ProviderByClassType(String.class, String.class));

        boolean exp = false;
        try {
            c1.attach(c2);
        } catch (ProviderConflictException e) {
            exp = true;
        } catch (Component.MultiParentException e) {

        }

        Assert.assertTrue(exp);

        exp = false;
        try {
            c2.attach(c1);
        } catch (ProviderConflictException e) {
            exp = true;
        } catch (Component.MultiParentException e) {

        }

        Assert.assertTrue(exp);
    }

    static abstract class Phone {
    }

    static class iPhone extends Phone {

    }

    static class Galaxy extends Phone {

    }

    static class Note extends Phone {

    }

    static class Nexus extends Phone {

    }

    class Store {
        @MyInject
        Phone phone;
    }

    @Test
    public void should_use_overridden_components_and_not_throw_exception()
            throws PokeException {
        Graph graph = new Graph();

        Component cIphone = new Component("iPhone");
        cIphone.register(new ProviderByClassType(Phone.class, iPhone.class));

        Component cGalaxy = new Component("Galaxy");
        cGalaxy.register(new ProviderByClassType(Phone.class, Galaxy.class));

        Component cNote = new Component("Note");
        cNote.register(new ProviderByClassType(Phone.class, Note.class));

        Component cNexus = new Component("Nexus");
        cNexus.register(new ProviderByClassType(Phone.class, Nexus.class));

        graph.setRootComponent(cIphone);
        Store store = new Store();
        graph.inject(store, MyInject.class);
        Assert.assertEquals(iPhone.class, store.phone.getClass());

        boolean exp = false;
        try {
            cIphone.attach(cGalaxy, true);
        } catch (ProviderConflictException e) {
            exp = true;
        } catch (Component.MultiParentException e) {
        }
        Assert.assertFalse(exp);
        graph.inject(store, MyInject.class);
        Assert.assertEquals(Galaxy.class, store.phone.getClass());

        try {
            cIphone.attach(cNote, true);
        } catch (ProviderConflictException e) {
            exp = true;
        } catch (Component.MultiParentException e) {
        }
        Assert.assertFalse(exp);
        graph.inject(store, MyInject.class);
        Assert.assertEquals(Note.class, store.phone.getClass());

        try {
            cGalaxy.attach(cNexus, true);
        } catch (ProviderConflictException e) {
            exp = true;
        } catch (Component.MultiParentException e) {
        }
        Assert.assertFalse(exp);
        graph.inject(store, MyInject.class);
        Assert.assertEquals(Nexus.class, store.phone.getClass());

        cIphone.detach(cNote);
        graph.inject(store, MyInject.class);
        Assert.assertEquals(Nexus.class, store.phone.getClass());

        cGalaxy.detach(cNexus);
        graph.inject(store, MyInject.class);
        Assert.assertEquals(Galaxy.class, store.phone.getClass());

        cIphone.detach(cGalaxy);
        graph.inject(store, MyInject.class);
        Assert.assertEquals(iPhone.class, store.phone.getClass());

        Component c0 = new Component();
        graph.setRootComponent(c0);
        c0.attach(cIphone);
        graph.inject(store, MyInject.class);
        Assert.assertEquals(iPhone.class, store.phone.getClass());
    }

    @Test
    public void should_roll_back_root_component_keys_when_attaching_child_component_with_conflicts()
            throws ProviderConflictException, ProviderMissingException, Graph.IllegalRootComponentException {
        Component c1 = new Component("A");
        c1.register(new ProviderByClassType(Integer.class, Integer.class));

        Component c2 = new Component();
        c2.register(new ProviderByClassType(Integer.class, Integer.class));
        c2.register(new ProviderByClassType(String.class, String.class));

        boolean exp = false;
        try {
            c1.attach(c2);
        } catch (ProviderConflictException e) {
            exp = true;
        } catch (Component.MultiParentException e) {

        }
        Assert.assertTrue(exp);

        Graph graph = new Graph();
        graph.setRootComponent(c1);

        boolean missingExp = false;
        try {
            graph.reference(String.class, null, Inject.class);
        } catch (ProviderMissingException e) {
            missingExp = true;
        } catch (ProvideException e) {
            e.printStackTrace();
        } catch (CircularDependenciesException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(missingExp);
    }
}
