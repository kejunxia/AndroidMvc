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

package com.shipdream.lib.poke.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import javax.inject.Qualifier;

public class ReflectUtils {
    /**
     * Wrapper class to generate an object instance by generic type.Note that this method only
     * generate object with its
     * empty constructor.
     * @param <T>
     */
    public static class newObjectByType<T>{
        private Class<T> clazz;
        public newObjectByType(Class<T> cls){
            clazz = cls;
        }

        /**
         * @return The new instance.
         * @throws NoSuchMethodException The class doesn't have a default constructor
         * @throws IllegalAccessException Unable to access the instance default constructor
         * @throws InvocationTargetException if the underlying constructor throws an exception.
         * @throws InstantiationException if the class that declares the underlying constructor represents an abstract class.
         */
        public T newInstance() throws NoSuchMethodException, IllegalAccessException,
                InvocationTargetException, InstantiationException {
            Constructor<T> constructor = clazz.getConstructor();
            return constructor.newInstance();
        }
    }

    /**
     * Find the first qualifier of the given field
     *
     * @param field The field
     * @return Null if no qualifier is found otherwise the first qualifier
     */
    public static Annotation findFirstQualifierInAnnotations(Field field) {
        return findFirstQualifierInAnnotations(field.getAnnotations());
    }

    /**
     * Find the first qualifier annotation of the given class
     *
     * @param clazz The class of which we are look for qualifier
     * @return Null if given clazz is null or no qualifier found, otherwise the first qualifier found
     */
    public static Annotation findFirstQualifierInAnnotations(Class clazz) {
        return findFirstQualifierInAnnotations(clazz.getAnnotations());
    }

    private static Annotation findFirstQualifierInAnnotations(Annotation[] annotations) {
        if (annotations != null) {
            for (Annotation a : annotations) {
                if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
                    return a;
                }
            }
        }

        return null;
    }

    /**
     * Sets value to the field of the given object.
     * @param obj The object
     * @param field The field
     * @param value The value
     */
    public static void setField(Object obj, Field field, Object value) {
        boolean accessible = field.isAccessible();
        //hack accessibility
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            //ignore should not happen as accessibility has been updated to suit assignment
            e.printStackTrace(); // $COVERAGE-IGNORE$
        }
        //restore accessibility
        field.setAccessible(accessible);
    }

    /**
     * Gets value of the field of the given object.
     * @param obj The object
     * @param field The field
     */
    public static Object getFieldValue(Object obj, Field field) {
        Object value = null;
        boolean accessible = field.isAccessible();
        //hack accessibility
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            value = field.get(obj);
        } catch (IllegalAccessException e) {
            //ignore should not happen as accessibility has been updated to suit assignment
            e.printStackTrace(); // $COVERAGE-IGNORE$
        }
        //restore accessibility
        field.setAccessible(accessible);
        return value;
    }

}
