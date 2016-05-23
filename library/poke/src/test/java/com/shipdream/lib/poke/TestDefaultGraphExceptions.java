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
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.fail;

public class TestDefaultGraphExceptions extends BaseTestCases {
    interface Pet{
    }

    static class Cat implements Pet {
    }

    static class Dog implements Pet {
    }

    private Graph graph;
    private Component component;

    @Before
    public void setUp() throws Exception {
        component = new Component("AppSingleton");
        graph = new Graph();
        graph.setRootComponent(component);
    }

    @Test
    public void suppress_constructor_miss_coverage_of_pokerHelper() {
        new PokeHelper();
    }

    @Test(expected = ProviderConflictException.class)
    public void shouldDetectConflictingClassRegistry() throws PokeException {
        component.register(new ProviderByClassType(Pet.class, Cat.class));
        component.register(new ProviderByClassType(Pet.class, Dog.class));
    }

    @Test(expected = ProviderConflictException.class)
    public void shouldDetectConflictingNameRegistry() throws PokeException, ClassNotFoundException {
        component.register(new ProviderByClassName(Pet.class, Cat.class.getName()));
        component.register(new ProviderByClassName(Pet.class, Dog.class.getName()));
    }

    @SuppressWarnings("unchecked")
    @Test(expected = ProviderConflictException.class)
    public void shouldDetectConflictingProviderRegistry() throws PokeException, ClassNotFoundException {
        Provider provider = new ProviderByClassType<>(Pet.class, Cat.class);
        Provider provider2 = new ProviderByClassName(Pet.class, Dog.class.getName());
        component.register(provider);
        component.register(provider2);
    }

    @Test(expected = ClassNotFoundException.class)
    public void shouldDetectBadClassException() throws PokeException, ClassNotFoundException {
        component.register(new ProviderByClassName(Pet.class, "BadClass"));
    }

    @Test
    public void shouldBeGoodInjection()
            throws PokeException, ProvideException {
        component.register(new ProviderByClassType(Pet.class, Dog.class));

        class Family {
            @MyInject
            private Pet pet;
        }

        Family family = new Family();
        graph.inject(family, MyInject.class);
    }

    static class Rabbit implements Pet {
        String name;
        public Rabbit(String name) {
            this.name = name;
        }
    }

    @Test(expected = ProvideException.class)
    public void shouldDetectProvideExceptionWithClassDoesHaveDefaultConstructor()
            throws PokeException {
        component.register(new ProviderByClassType(Pet.class, Rabbit.class));

        class Family {
            @MyInject
            private Pet pet;
        }

        Family family = new Family();
        graph.inject(family, MyInject.class);
    }

    private static abstract class AbstractBean {
    }

    @Test
    public void should_handle_InstantiationException_when_create_class_instance_in_ProviderByClassName()
            throws PokeException, ClassNotFoundException {
        Provider provider = new ProviderByClassName(AbstractBean.class, AbstractBean.class.getName());
        component.register(provider);

        class Consumer {
            @MyInject
            AbstractBean bean;
        }

        Consumer c = new Consumer();

        try {
            graph.inject(c, MyInject.class);
            fail("Should have caught InstantiationException but not");
        } catch (PokeException e) {
            Assert.assertTrue(e.getCause() instanceof InstantiationException);
        }
    }

    private static class BadInstantiatingBean {
        {
            int x = 1 / 0;
        }
    }

    @Test
    public void should_handle_InvocationTargetException_when_create_class_instance_in_ProviderByClassName()
            throws PokeException, ClassNotFoundException {
        Provider provider = new ProviderByClassName(BadInstantiatingBean.class, BadInstantiatingBean.class.getName());
        component.register(provider);

        class Consumer {
            @MyInject
            BadInstantiatingBean bean;
        }

        Consumer c = new Consumer();

        try {
            graph.inject(c, MyInject.class);

            fail("Should have caught InvocationTargetException but not");
        } catch (PokeException e) {
            Assert.assertTrue(e.getCause() instanceof InvocationTargetException);
        }
    }

    @Test
    public void should_handle_NoSuchMethodException_when_create_class_instance_in_ProviderByClassName()
            throws PokeException, ClassNotFoundException {
        class BadBean {
        }

        Provider provider = new ProviderByClassName(BadBean.class, BadBean.class.getName());
        component.register(provider);

        class Consumer {
            @MyInject
            BadBean bean;
        }

        Consumer c = new Consumer();

        try {
            graph.inject(c, MyInject.class);

            fail("Should have caught NoSuchMethodException but not");
        } catch (PokeException e) {
            Assert.assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
    }

}
