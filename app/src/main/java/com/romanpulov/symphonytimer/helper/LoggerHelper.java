package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import androidx.preference.PreferenceManager;
import android.util.Log;

import com.romanpulov.library.common.logger.FileLogger;

import java.io.File;

/**
 * Created by romanpulov on 25.11.2016.
 */

public class LoggerHelper {
    private static final String LOCAL_APP_FOLDER_NAME = "SymphonyTimer";
    private static final String LOG_FOLDER_NAME = "log";

    private static LoggerHelper mInstance;

    public static LoggerHelper getInstance(Context context) {
        if (mInstance == null)
            mInstance = new LoggerHelper(context);

        return mInstance;
    }

    public static void clearInstance() {
        mInstance = null;
    }

    private static FileLogger mLogger;
    private final File mLogFolder;

    private final boolean mEnableLogging;

    public void log(String tag, String message) {
        if (mEnableLogging)
            internalLog(tag, message);

    }

    private void internalLog(String tag, String message) {
        if (mLogFolder != null) {

            String logFileName = mLogFolder.getPath() + "/" + DateFormatterHelper.formatLogFileDate(System.currentTimeMillis()) + ".log";
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

    public void unconditionalLog(String tag, String message) {
        internalLog(tag, message);
    }

    public static void logContext(Context context, String tag, String message) {
        if (context != null)
            getInstance(context).log(tag, message);
    }

    public static void unconditionalLogContext(Context context, String tag, String message) {
        if (context != null) {
            getInstance(context).unconditionalLog(tag, message);
        }
    }


    private LoggerHelper(Context context) {
        mEnableLogging = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_logging", false);
        mLogFolder = prepareLogFolder(context);
    }

    private static File prepareLogFolder(Context context) {
        File logFolder = context.getExternalFilesDir(LOG_FOLDER_NAME);
        if (logFolder == null) {
            logFolder = new File(context.getFilesDir(), LOG_FOLDER_NAME);
        }

        if (!logFolder.exists())
            if (!logFolder.mkdir())
                return null;

        return logFolder;
    }
}
