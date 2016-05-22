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

import com.shipdream.lib.poke.exception.IllegalScopeException;
import com.shipdream.lib.poke.exception.ProvideException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Qualifier;
import javax.inject.Scope;

/**
 * This provider uses default/empty constructor by provided class type to get dependencies. So
 * make sure the implementation has default public constructor
 */
public class ProviderByClassType<T> extends Provider<T> {
    private final Class<? extends T> clazz;
    private final String implClassName;

    private static class ClassInfo {
        Annotation qualifier;
        String scope;
    }

    private static Map<Class, ClassInfo> classInfoCache = new HashMap<>();

    private static ClassInfo getClassInfo(Class implClass) {
        ClassInfo classInfo = classInfoCache.get(implClass);
        if (classInfo == null) {
            classInfo = new ClassInfo();
            classInfoCache.put(implClass, classInfo);
            Annotation[] annotations = implClass.getAnnotations();
            if (annotations != null) {
                for (Annotation a : annotations) {
                    Class<? extends Annotation> annotationType = a.annotationType();
                    if (annotationType.isAnnotationPresent(Qualifier.class)) {
                        if (classInfo.qualifier != null) {
                            classInfo.qualifier = a;
                        }
                    } else if (annotationType.isAnnotationPresent(Scope.class)) {
                        if (classInfo.scope != null) {
                            classInfo.scope = annotationType.getName();
                        }
                    }
                }
            }
        }
        return classInfo;
    }

    /**
     * Construct a provider binding the type and the implementation class type. The found
     * implementation class may be annotated by {@link Qualifier} and {@link Scope}
     * @param type The contract of the implementation
     * @param implementationClass The class type of the implementation. It must have a default
     *                            public constructor
     * @param scopeCache The scope cache to be used when the implementation class is annotated with a
     *                   {@link Scope}
     * @throws IllegalScopeException Thrown when the scope marked by the implementationClass and
     *                              scopeCache are not matched
     */
    public ProviderByClassType(Class<T> type, Class<? extends T> implementationClass,
                               ScopeCache scopeCache) throws IllegalScopeException {
        super(type,
                getClassInfo(implementationClass).qualifier,
                getClassInfo(implementationClass).scope,
                scopeCache);
        this.implClassName = implementationClass.getName();
        this.clazz = implementationClass;

        //Clean helping cache
        classInfoCache.remove(implementationClass);
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
