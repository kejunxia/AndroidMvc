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

import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.util.ReflectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.inject.Qualifier;

/**
 * This provider uses default/empty constructor by provided class type to get dependencies. So
 * make sure the implementation has default public constructor
 */
public class ProviderByClassType<T> extends Provider<T> {
    private final Class<? extends T> clazz;
    private final String implClassName;

    /**
     * Construct a provider binding the type and the implementation class type. The found
     * implementation class may be annotated by {@link Qualifier}
     * @param type The contract of the implementation
     * @param implementationClass The class type of the implementation. It must have a default
     *                            public constructor
     */
    public ProviderByClassType(Class<T> type, Class<? extends T> implementationClass) {
        super(type, ReflectUtils.findFirstQualifierInAnnotations(implementationClass));
        this.implClassName = implementationClass.getName();
        this.clazz = implementationClass;
    }

    @Override
    public T createInstance() throws ProvideException {
        try {
            Constructor constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (T)constructor.newInstance();
        } catch (InstantiationException e) {
            throwProvideException(e);
        } catch (IllegalAccessException e) {
            throwProvideException(e);
        } catch (NoSuchMethodException e) {
            throwProvideException(e);
        } catch (InvocationTargetException e) {
            throwProvideException(e);
        }

        throw new ProvideException(String.format("Failed to provide class - %s as newInstance " +
                "of it returns null", type()));
    }

    private void throwProvideException(Exception e) throws ProvideException {
        throw new ProvideException(String.format("Failed to provide class - %s. Make sure %s exist " +
                "and with a default empty constructor.", clazz.getName(), implClassName), e);
    }

}
