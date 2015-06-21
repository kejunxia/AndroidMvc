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

import org.junit.Test;

public class TestDefaultGraphExceptions extends BaseTestCases {
    interface Pet{
    }

    static class Cat implements Pet {
    }

    static class Dog implements Pet {
    }

    @Test
    public void suppress_constructor_miss_coverage_of_pokerHelper() {
        new PokeHelper();
    }

    @Test(expected = ProviderConflictException.class)
    public void shouldDetectConflictingClassRegistry() throws ProviderConflictException {
        SimpleGraph graph = new SimpleGraph();
        graph.register(Pet.class, Cat.class);
        graph.register(Pet.class, Dog.class);
    }

    @Test(expected = ProviderConflictException.class)
    public void shouldDetectConflictingNameRegistry() throws ProviderConflictException, ClassNotFoundException {
        SimpleGraph graph = new SimpleGraph();
        graph.register(Pet.class, Cat.class.getName());
        graph.register(Pet.class, Dog.class.getName());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = ProviderConflictException.class)
    public void shouldDetectConflictingProviderRegistry() throws ProviderConflictException, ProvideException, ClassNotFoundException {
        SimpleGraph graph = new SimpleGraph();
        Provider provider = new ProviderByClassType<>(Pet.class, Cat.class);
        Provider provider2 = new ProviderByClassName(Pet.class, Dog.class.getName());
        graph.register(provider);
        graph.register(provider2);
    }

    @Test(expected = ClassNotFoundException.class)
    public void shouldDetectBadClassException() throws ProviderConflictException, ClassNotFoundException {
        SimpleGraph graph = new SimpleGraph();
        graph.register(Pet.class, "BadClass");
    }

    @Test
    public void shouldBeGoodInjection()
            throws ProviderConflictException, CircularDependenciesException, ProviderMissingException, ProvideException {
        SimpleGraph graph = new SimpleGraph();
        graph.register(Pet.class, Dog.class);

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
            throws ProviderConflictException, CircularDependenciesException, ProviderMissingException, ProvideException {
        SimpleGraph graph = new SimpleGraph();
        graph.register(Pet.class, Rabbit.class);

        class Family {
            @MyInject
            private Pet pet;
        }

        Family family = new Family();
        graph.inject(family, MyInject.class);
    }

    static class Fish implements Pet {
        private Fish() {
        }
    }

    @Test(expected = ProvideException.class)
    public void shouldDetectProvideExceptionWithClassWithInaccessibleConstructor()
            throws ProviderConflictException, CircularDependenciesException, ProviderMissingException, ProvideException {
        SimpleGraph graph = new SimpleGraph();
        graph.register(Pet.class, Fish.class);

        class Family {
            @MyInject
            private Pet pet;
        }

        Family family = new Family();
        graph.inject(family, MyInject.class);
    }
}
