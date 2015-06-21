package com.shipdream.lib.poke;

import com.shipdream.lib.poke.util.ReflectUtils;

import org.junit.Test;

public class TestReflectUtils {
    @Test
    public void should_compare_object_equality_correctly() {

    }

    @Test
    public void should_be_able_to_create_reflectUtils() {
        ReflectUtils reflectUtils = new ReflectUtils();

        ReflectUtils.newObjectByType newObjectByType = new ReflectUtils.newObjectByType(String.class);
    }
}
