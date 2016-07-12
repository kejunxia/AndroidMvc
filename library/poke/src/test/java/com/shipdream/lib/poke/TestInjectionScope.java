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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestInjectionScope extends BaseTestCases {
    private Graph graph;
    private Component component;
    @Before
    public void setUp() throws Exception {
        component = new Component("AppSingleton");
        graph = new Graph();
        graph.setRootComponent(component);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInjectSingleton() throws PokeException {
        System.out.println("----------------------------------------------");
        System.out.println("Test injection scope - singleton\n");

        //Provider is scoped as singleton so all instances are be the same shared one
        Provider<PowerSupply> powerSupplyProvider = new ProviderByClassType<>(PowerSupply.class, Generator.class);

        component.register(powerSupplyProvider);

        Graph graph2 = new Graph();
        graph2.setRootComponent(component);

        Pipeline p1 = new Pipeline();
        Pipeline p2 = new Pipeline();

        graph.inject(p1, MyInject.class);
        graph2.inject(p2, MyInject.class);

        Assert.assertEquals(p1.powerSupply, p2.powerSupply);

        Assert.assertFalse(p1.powerSupply.isOn());
        Assert.assertFalse(p2.powerSupply.isOn());

        p1.powerSupply.switchOn();
        Assert.assertTrue(p1.powerSupply.isOn());
        Assert.assertTrue(p2.powerSupply.isOn());

        p2.powerSupply.switchOff();
        Assert.assertFalse(p1.powerSupply.isOn());
        Assert.assertFalse(p2.powerSupply.isOn());

        p2.powerSupply.switchOn();
        Assert.assertTrue(p1.powerSupply.isOn());
        Assert.assertTrue(p2.powerSupply.isOn());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInjectSingletonWithDifferentScopes() throws PokeException {
        System.out.println("----------------------------------------------");
        System.out.println("Test injection scope - singleton\n");

        Provider<PowerSupply> powerSupplyProvider = new ProviderByClassType<>(PowerSupply.class, Generator.class);
        component.register(powerSupplyProvider);

        Graph graph2 = new Graph();
        graph2.setRootComponent(component);

        Pipeline p1 = new Pipeline();
        Pipeline p2 = new Pipeline();

        graph.inject(p1, MyInject.class);
        graph2.inject(p2, MyInject.class);

        Assert.assertTrue(p1.powerSupply == p2.powerSupply);

        Assert.assertFalse(p1.powerSupply.isOn());
        Assert.assertFalse(p2.powerSupply.isOn());

        p1.powerSupply.switchOn();
        Assert.assertTrue(p1.powerSupply.isOn());
        Assert.assertTrue(p2.powerSupply.isOn());

        p2.powerSupply.switchOff();
        Assert.assertFalse(p1.powerSupply.isOn());
        Assert.assertFalse(p2.powerSupply.isOn());

        p2.powerSupply.switchOn();
        Assert.assertTrue(p1.powerSupply.isOn());
        Assert.assertTrue(p2.powerSupply.isOn());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInjectUnScoped() throws PokeException {
        System.out.println("----------------------------------------------");
        System.out.println("Test injection scope - unscoped\n");

        //Provider is not scoped so all instances are separated
        Provider<PowerSupply> powerSupplyProvider = new ProviderByClassType<>(PowerSupply.class, Generator.class);
        Component c = new Component(false);
        c.register(powerSupplyProvider);
        graph.setRootComponent(c);

        Pipeline p1 = new Pipeline();
        Pipeline p2 = new Pipeline();

        graph.inject(p1, MyInject.class);
        graph.inject(p2, MyInject.class);

        Assert.assertTrue(p1.powerSupply != p2.powerSupply);

        Assert.assertFalse(p1.powerSupply.isOn());
        Assert.assertFalse(p2.powerSupply.isOn());

        p1.powerSupply.switchOn();
        Assert.assertTrue(p1.powerSupply.isOn());
        Assert.assertFalse(p2.powerSupply.isOn());

        p1.powerSupply.switchOff();
        Assert.assertFalse(p1.powerSupply.isOn());
        Assert.assertFalse(p2.powerSupply.isOn());

        p2.powerSupply.switchOn();
        Assert.assertFalse(p1.powerSupply.isOn());
        Assert.assertTrue(p2.powerSupply.isOn());

        p2.powerSupply.switchOff();
        Assert.assertFalse(p1.powerSupply.isOn());
        Assert.assertFalse(p2.powerSupply.isOn());

        p1.powerSupply.switchOn();
        p2.powerSupply.switchOn();
        Assert.assertTrue(p1.powerSupply.isOn());
        Assert.assertTrue(p2.powerSupply.isOn());
    }

    public static class Pipeline {
        @MyInject
        PowerSupply powerSupply;
    }

    public static class Generator implements PowerSupply {
        private boolean isOn = false;

        @Override
        public boolean isOn() {
            return isOn;
        }

        @Override
        public void switchOn() {
            isOn = true;
        }

        @Override
        public void switchOff() {
            isOn = false;
        }
    }

    public interface PowerSupply {
        boolean isOn();
        void switchOn();
        void switchOff();
    }
}
