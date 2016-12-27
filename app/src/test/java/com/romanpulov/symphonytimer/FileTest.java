package com.romanpulov.symphonytimer;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import dalvik.annotation.TestTargetClass;

/**
 * Created by romanpulov on 27.12.2016.
 */

public class FileTest {
    @Test
    public void simpleTest() {
        System.out.println("Unit test");
    }

    @Test
    public void tempFileTest() throws Exception {
        File tempFile = File.createTempFile("pref", "suff");
        Assert.assertNotNull(tempFile);
        System.out.println(tempFile.getPath());
        Assert.assertTrue(tempFile.delete());
    }
}
