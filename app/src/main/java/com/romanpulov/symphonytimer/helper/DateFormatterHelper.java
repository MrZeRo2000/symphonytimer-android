package com.romanpulov.symphonytimer.helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Format date routines
 * Created by romanpulov on 24.05.2017.
 */

public class DateFormatterHelper {
    private static String LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static String TIME_FORMAT = "HH:mm:ss";

    private static DateFormat mLogDateFormat;
    private static DateFormat mTimeFormat;

    private static DateFormat getLogDateFormat() {
        if (mLogDateFormat == null)
            mLogDateFormat = new SimpleDateFormat(LOG_DATE_FORMAT, Locale.getDefault());

        return mLogDateFormat;
    }

    private static DateFormat getTimeFormat() {
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
     * Format for log with milliseconds
     * @param milliseconds date in milliseconds
     * @return formatted date string
     */
    public static String formatLog(long milliseconds) {
        return getLogDateFormat().format(new Date(milliseconds));
    }

    /**
     * Format time only
     * @param milliseconds date in milliseconds
     * @return formatted time string
     */
    public static String formatTime(long milliseconds) {
        return java.text.DateFormat.getTimeInstance().format(new Date(milliseconds));
    }

    /**
     * Format time only with predefined pattern
     * @param milliseconds date in milliseconds
     * @return formatted time string
     */

    public static String formatTimeLog(long milliseconds) {
        return getTimeFormat().format(new Date(milliseconds));
    }
}
