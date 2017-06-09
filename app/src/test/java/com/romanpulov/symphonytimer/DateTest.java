package com.romanpulov.symphonytimer;

import com.romanpulov.symphonytimer.helper.DateFormatterHelper;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by rpulov on 01.04.2017.
 */

public class DateTest {
    @Test
    public void simpleTest() {
        System.out.println("Date test");
        System.out.println(DateFormatterHelper.format(1491054630812L));
    }

    @Test
    public void logTest() {
        System.out.println("Date debug test");
        System.out.println(DateFormatterHelper.formatLog(1491054630812L));
    }
}
