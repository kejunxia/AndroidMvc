package com.shipdream.lib.poke;

import com.shipdream.lib.poke.util.CommonUtils;

import org.junit.Assert;
import org.junit.Test;

public class TestCommonUtils {
    @Test
    public void should_compare_object_equality_correctly() {
        Assert.assertTrue(CommonUtils.areObjectsEqual(null, null));
        Assert.assertFalse(CommonUtils.areObjectsEqual(null, new Object()));
        Assert.assertFalse(CommonUtils.areObjectsEqual(new Object(), null));

        Object a = new Object();
        Object b = new Object();

        Assert.assertFalse(CommonUtils.areObjectsEqual(a, b));
        Assert.assertFalse(CommonUtils.areObjectsEqual(b, a));
        Assert.assertFalse(CommonUtils.areObjectsEqual(null, a));
        Assert.assertFalse(CommonUtils.areObjectsEqual(a, null));

        String s1 = "abc";
        String s2 = "abc";

        Assert.assertTrue(CommonUtils.areObjectsEqual(s1, s2));
        Assert.assertTrue(CommonUtils.areObjectsEqual(s2, s1));

        Assert.assertFalse(CommonUtils.areObjectsEqual(null, s1));
        Assert.assertFalse(CommonUtils.areObjectsEqual(s1, null));
    }

    @Test
    public void should_be_able_to_create_commonUtils() {
        CommonUtils commonUtils = new CommonUtils();
    }
}
