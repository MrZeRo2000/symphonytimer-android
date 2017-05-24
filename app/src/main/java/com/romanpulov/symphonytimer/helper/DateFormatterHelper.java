package com.romanpulov.symphonytimer.helper;

import java.util.Date;

/**
 * Format date
 * Created by romanpulov on 24.05.2017.
 */

public class DateFormatterHelper {
    /**
     * Default format
     * @param milliseconds date in milliseconds
     * @return formatted date string
     */
    public static String format(long milliseconds) {
        return java.text.DateFormat.getDateTimeInstance().format(new Date(milliseconds));
    }
}
