package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.romanpulov.library.common.logger.FileLogger;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by romanpulov on 25.11.2016.
 */

public class LoggerHelper {
    private static final String LOCAL_APP_FOLDER_NAME = "SymphonyTimer";
    private static final String LOG_FOLDER_NAME = "log";
    private static final String LOG_FILE_DATE_FORMAT = "yyyy-MM-dd";

    private static LoggerHelper mInstance;

    public static LoggerHelper getInstance(Context context) {
        if (mInstance == null)
            mInstance = new LoggerHelper(context);

        return mInstance;
    }

    public static void clearInstance() {
        mInstance = null;
    }

    private static DateFormat mDateFormat = new SimpleDateFormat(LOG_FILE_DATE_FORMAT, Locale.getDefault());

    private static FileLogger mLogger;
    private static File mLogFolder = prepareLogFolder();

    private final boolean mEnableLogging;

    public void log(String tag, String message) {
        if (mEnableLogging)
            internalLog(tag, message);

    }

    private static void internalLog(String tag, String message) {
        if (mLogFolder != null) {

            String logFileName = mLogFolder.getPath() + "/" + mDateFormat.format(System.currentTimeMillis()) + ".log";
            if (mLogger == null) {
                mLogger = new FileLogger(logFileName);
            } else if (!mLogger.getFileName().equals(logFileName)) {
                mLogger.close();
                mLogger = new FileLogger(logFileName);
            }
            mLogger.log(tag, message, 0);
        } else
            Log.d("LoggerHelper", "Folder not prepared");
        Log.d(tag, message);
    }

    public static void unconditionalLog(String tag, String message) {
        internalLog(tag, message);
    }

    public static void logContext(Context context, String tag, String message) {
        if (context != null)
            getInstance(context).log(tag, message);
    }

    private LoggerHelper(Context context) {
        mEnableLogging = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_logging", false);
    }

    private static File prepareLogFolder() {
        File appFolder = new File(Environment.getExternalStorageDirectory().toString() + "/" + LOCAL_APP_FOLDER_NAME);
        if (!appFolder.exists())
            if (!appFolder.mkdir())
                return null;

        File logFolder = new File(appFolder.getPath() + "/" + LOG_FOLDER_NAME);
        if (logFolder.exists())
            return logFolder;
        else
            if (logFolder.mkdir())
                return logFolder;
            else
                return null;
    }
}
