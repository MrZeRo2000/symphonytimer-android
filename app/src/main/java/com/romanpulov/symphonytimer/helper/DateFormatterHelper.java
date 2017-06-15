package com.romanpulov.symphonytimer.helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Format date
 * Created by romanpulov on 24.05.2017.
 */

public class DateFormatterHelper {
    public static String LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static String TIME_FORMAT = "HH:mm:ss";

    public static DateFormat mLogDateFormat;
    public static DateFormat mTimeFormat;

    public static DateFormat getLogDateFormat() {
        if (mLogDateFormat == null)
            mLogDateFormat = new SimpleDateFormat(LOG_DATE_FORMAT, Locale.getDefault());

        return mLogDateFormat;
    }

    public static DateFormat getTimeFormat() {
        if (mTimeFormat == null)
            mTimeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());

        return mTimeFormat;
    }


    /**
     * Default format
     * @param milliseconds date in milliseconds
     * @return formatted date string
     */
    public static String format(long milliseconds) {
        return java.text.DateFormat.getDateTimeInstance().format(new Date(milliseconds));
    }

    /**
     * Format for log with miliseconds
     * @param miliseconds date in miliseconds
     * @return formatted date string
     */
    public static String formatLog(long miliseconds) {
        return getLogDateFormat().format(new Date(miliseconds));
    }

    /**
     * Format time only
     * @param miliseconds date in miliseconds
     * @return formatted time string
     */
    public static String formatTime(long miliseconds) {
        return getTimeFormat().format(new Date(miliseconds));
    }
}
