package com.shipdream.lib.android.mvc.service.internal;


import android.content.Context;

import com.shipdream.lib.android.mvc.service.PreferenceService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Random;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class TestPreferencesServiceImpl {
    private PreferenceService preferenceService;

    @Before
    public void setUp() throws Exception {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        preferenceService = new PreferenceServiceImpl(context, "TestPref", Context.MODE_PRIVATE);
    }

    @Test
    public void should_be_able_to_write_and_read_boolean() {
        String key = "My Boolean";
        Assert.assertFalse(preferenceService.contains(key));

        Assert.assertEquals(true, preferenceService.getBoolean(key, true));

        preferenceService.edit().putBoolean(key, false).commit();
        Assert.assertTrue(preferenceService.contains(key));
        Assert.assertEquals(false, preferenceService.getBoolean(key, true));

        preferenceService.edit().putBoolean(key, true).apply();
        Assert.assertTrue(preferenceService.contains(key));
        Assert.assertEquals(true, preferenceService.getBoolean(key, false));

        preferenceService.edit().clear().commit();
        Assert.assertFalse(preferenceService.contains(key));
    }

    @Test
    public void should_be_able_to_write_and_read_int() {
        String key = "My Integer";
        Assert.assertEquals(Integer.MIN_VALUE, preferenceService.getInt(key, Integer.MIN_VALUE));

        Random random = new Random();
        int num = random.nextInt();
        preferenceService.edit().putInt(key, num).commit();
        Assert.assertTrue(preferenceService.contains(key));
        Assert.assertEquals(num, preferenceService.getInt(key, Integer.MIN_VALUE));

        num = random.nextInt();
        preferenceService.edit().putInt(key, num).apply();
        Assert.assertTrue(preferenceService.contains(key));
        Assert.assertEquals(num, preferenceService.getInt(key, Integer.MIN_VALUE));

        preferenceService.edit().clear().commit();
        Assert.assertFalse(preferenceService.contains(key));
    }

    @Test
    public void should_be_able_to_write_and_read_long() {
        String key = "My Long";
        Assert.assertEquals(Long.MIN_VALUE, preferenceService.getLong(key, Long.MIN_VALUE));

        Random random = new Random();
        long num = random.nextLong();
        preferenceService.edit().putLong(key, num).commit();
        Assert.assertTrue(preferenceService.contains(key));
        Assert.assertEquals(num, preferenceService.getLong(key, Long.MIN_VALUE));

        num = random.nextLong();
        preferenceService.edit().putLong(key, num).apply();
        Assert.assertTrue(preferenceService.contains(key));
        Assert.assertEquals(num, preferenceService.getLong(key, Long.MIN_VALUE));

        preferenceService.edit().clear().commit();
        Assert.assertFalse(preferenceService.contains(key));
    }

    @Test
    public void should_be_able_to_write_and_read_float() {
        String key = "My float";
        Assert.assertEquals(Float.MIN_VALUE, preferenceService.getFloat(key, Float.MIN_VALUE), 0.001f);

        Random random = new Random();
        float textFloat = random.nextFloat();
        preferenceService.edit().putFloat(key, textFloat).commit();
        Assert.assertTrue(preferenceService.contains(key));
        Assert.assertEquals(textFloat, preferenceService.getFloat(key, Float.MIN_VALUE), 0.001f);

        textFloat = random.nextFloat();
        preferenceService.edit().putFloat(key, textFloat).apply();
        Assert.assertTrue(preferenceService.contains(key));
        Assert.assertEquals(textFloat, preferenceService.getFloat(key, Float.MIN_VALUE), 0.001f);

        preferenceService.edit().clear().commit();
        Assert.assertFalse(preferenceService.contains(key));
    }

    @Test
    public void should_be_able_to_write_and_read_string() {
        String key = "My String";
        Assert.assertEquals(null, preferenceService.getString(key, null));

        String textString = "iujqwkh786*%^9786y123";
        preferenceService.edit().putString(key, textString).commit();
        Assert.assertTrue(preferenceService.contains(key));
        Assert.assertEquals(textString, preferenceService.getString(key, null));

        textString = "kvjubemuyek12-_-3294i0";
        preferenceService.edit().putString(key, textString).apply();
        Assert.assertTrue(preferenceService.contains(key));
        Assert.assertEquals(textString, preferenceService.getString(key, null));

        preferenceService.edit().clear().commit();
        Assert.assertFalse(preferenceService.contains(key));
    }

}
