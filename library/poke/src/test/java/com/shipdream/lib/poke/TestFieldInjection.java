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

import com.shipdream.lib.poke.exception.ProviderMissingException;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

public class TestFieldInjection extends BaseTestCases {

    private Graph graph;
    private Component component;

    @Before
    public void setUp() throws Exception {
        component = new Component();
        graph = new Graph();
        graph.setRootComponent(component);
    }

    @Test
    public void testInjectByClassType() throws Exception {
        System.out.println("----------------------------------------------");
        System.out.println("Test injection to fields of target object by class type");

        component.register(new ProviderByClassType(Bird.class, Crow.class));
        component.register(new ProviderByClassType(Animal.class, Horse.class));
        component.register(new ProviderByClassType(Fish.class, GoldFish.class));

        Zoo zoo = new Zoo();
        graph.inject(zoo, MyInject.class);
        zoo.show();
    }

    @Test
    public void testInjectByClassName() throws Exception {
        System.out.println("----------------------------------------------");
        System.out.println("Test injection to fields of target object by class name");

        component.register(new ProviderByClassName(Bird.class, Crow.class.getName()));
        component.register(new ProviderByClassName(Animal.class, Horse.class.getName()));
        component.register(new ProviderByClassName(Fish.class, GoldFish.class.getName()));

        Zoo zoo = new Zoo();
        graph.inject(zoo, MyInject.class);
        zoo.show();
    }

    @Test(expected = ProviderMissingException.class)
    public void testMissingProvider() throws Exception {
        System.out.println("----------------------------------------------");
        System.out.println("Test injection to fields but missing provider\n");

        component.register(new ProviderByClassName(Bird.class, Crow.class.getName()));
        component.register(new ProviderByClassName(Animal.class, Horse.class.getName()));

        BiggerZoo zoo = new BiggerZoo();
        graph.inject(zoo, MyInject.class);
        zoo.show();
    }

    @Test
    public void testInjectToBaseClass() throws Exception {
        System.out.println("----------------------------------------------");
        System.out.println("Test injection to fields to base class by class type\n");

        Component c = new Component(false);
        graph.setRootComponent(c);
        c.register(new ProviderByClassName(Bird.class, Crow.class.getName()));
        c.register(new ProviderByClassName(Animal.class, Horse.class.getName()));
        c.register(new ProviderByClassName(Fish.class, GoldFish.class.getName()));

        BiggerZoo zoo = new BiggerZoo();
        graph.inject(zoo, MyInject.class);
        zoo.show();
    }

    public static class BiggerZoo extends Zoo {
        @MyInject
        private Animal horse2;

        public void show() {
            super.show();

            horse2.born();
            String journal = horse2.journal();
            Assert.assertEquals(journal, Horse.born);
            System.out.println("Test horse show: ");
            System.out.print(journal + "\n");
        }
    }

    public static class Zoo {
        private String journal = "";

        @MyInject
        private Bird crow;

        @MyInject
        private Fish goldfish;

        @MyInject
        private Animal horse;

        public void show() {
            journal = birdShow();
            Assert.assertEquals(journal, Crow.born + Crow.fly);
            System.out.println("Test bird show: ");
            System.out.print(journal + "\n");

            journal = fishShow();
            Assert.assertEquals(journal, GoldFish.born + GoldFish.swim);
            System.out.println("Test gold fish show: ");
            System.out.print(journal + "\n");

            journal = horseShow();
            Assert.assertEquals(journal, Horse.born);
            System.out.println("Test horse show: ");
            System.out.print(journal + "\n");
        }

        private String birdShow() {
            crow.born();
            crow.fly();

            return crow.journal();
        }

        private String fishShow() {
            goldfish.born();
            goldfish.swim();

            return goldfish.journal();
        }

        private String horseShow() {
            horse.born();

            return horse.journal();
        }
    }

    public static class Horse implements Animal {
        final static String born = "I am a pony.\n";

        private String journal = "";

        @Override
        public void born() {
            journal += born;
        }

        @Override
        public String journal() {
            return journal;
        }
    }

    public static class GoldFish implements Fish {
        private String journal = "";
        final static String born = "I am a goldfish.\n";
        final static String swim = "I whip fast...\n";

        @Override
        public void born() {
            journal += born;
        }

        @Override
        public void swim() {
            journal += swim;
        }

        @Override
        public String journal() {
            return journal;
        }

    }

    public static class Crow implements Bird {
        private String journal = "";
        final static String fly = "I flip, flip...\n";
        final static String born = "I am a little crow.\n";

        @Override
        public void fly() {
            journal += fly;
        }

        @Override
        public void born() {
            journal += born;
        }

        @Override
        public String journal() {
            return journal;
        }
    }

    public interface Animal {
        void born();
        String journal();
    }

    public interface Bird extends Animal {
        void fly();
    }

    public interface Fish extends Animal {
        void swim();
    }
}
