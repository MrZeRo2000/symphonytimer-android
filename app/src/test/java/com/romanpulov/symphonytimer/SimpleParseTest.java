package com.romanpulov.symphonytimer;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by romanpulov on 16.06.2017.
 */

public class SimpleParseTest {
    @Test
    public void simpleTest() {
        System.out.println("Unit test");
    }

    @Test
    public void stringParseTest() {
        String s1 = "40";
        int parsed = Integer.valueOf(s1);
        Assert.assertEquals(parsed, 40);

        s1 = "40 minutes";
        try {
            parsed = Integer.valueOf(s1);
            Assert.fail("Parsed which should not be parsed");
        } catch(NumberFormatException e) {

        }
    }
}
