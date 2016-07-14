package com.shipdream.lib.android.mvc;

import org.junit.Assert;
import org.junit.Test;

public class TestReason {
    @Test
    public void reason_should_be_set_and_read_correctly() {
        Reason reason = new Reason();
        reason.isNewInstance = true;
        reason.isFirstTime = true;
        reason.isRestored = true;
        reason.isRotated = true;
        reason.isPoppedOut = true;

        Assert.assertTrue(reason.isNewInstance());
        Assert.assertTrue(reason.isFirstTime());
        Assert.assertTrue(reason.isRestored());
        Assert.assertTrue(reason.isRotated());
        Assert.assertTrue(reason.isPoppedOut());

        reason.toString();

        reason.isNewInstance = false;
        reason.isFirstTime = false;
        reason.isRestored = false;
        reason.isRotated = false;
        reason.isPoppedOut = false;

        Assert.assertFalse(reason.isNewInstance());
        Assert.assertFalse(reason.isFirstTime());
        Assert.assertFalse(reason.isRestored());
        Assert.assertFalse(reason.isRotated());
        Assert.assertFalse(reason.isPoppedOut());
    }
}
