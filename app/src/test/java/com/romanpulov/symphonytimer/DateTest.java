package com.romanpulov.symphonytimer;

import com.romanpulov.symphonytimer.helper.DateFormatterHelper;

import org.junit.Test;

import java.util.Date;

/**
 * Created by rpulov on 01.04.2017.
 */

public class DateTest {
    @Test
    public void simpleTest() {
        System.out.println("Date test");
        System.out.println(DateFormatterHelper.format(1491054630812L));
    }
}
