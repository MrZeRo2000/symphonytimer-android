package com.romanpulov.symphonytimer;

import org.junit.Test;

import java.util.Date;

/**
 * Created by rpulov on 01.04.2017.
 */

public class DateTest {
    @Test
    public void simpleTest() {
        System.out.println("Date test");
        System.out.println(java.text.DateFormat.getDateTimeInstance().format(new Date(1491054630812L)));
    }
}
