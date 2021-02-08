package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import androidx.preference.PreferenceManager;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.romanpulov.library.common.logger.AbstractLogger;
import com.romanpulov.library.common.logger.FileLogger;
import com.romanpulov.library.common.logger.MediaStoreLogger;
import com.romanpulov.library.common.media.MediaStoreUtils;

import java.io.File;

/**
 * Created by romanpulov on 25.11.2016.
 */

public class LoggerHelper {
    private static final String LOCAL_APP_FOLDER_NAME = "SymphonyTimer";
    private static final String LOG_FOLDER_NAME = "Log";

    private static LoggerHelper mInstance;

    public static LoggerHelper getInstance(Context context) {
        if (mInstance == null)
            mInstance = new LoggerHelper(context);

        return mInstance;
    }

    public static void clearInstance() {
        mInstance = null;
    }

    private static AbstractLogger mLogger;

    private final Context mContext;
    private final boolean mEnableLogging;

    private String mLogFolderName;

    public void log(String tag, String message) {
        if (mEnableLogging)
            internalLog(tag, message);
    }

    private void internalLog(String tag, String message) {
        String logFileName =  DateFormatterHelper.formatLogFileDate(System.currentTimeMillis()) + ".log";

        if (mLogger == null) {
            mLogger = prepareLogger(logFileName);
        } else if (!mLogger.getFileName().equals(logFileName)) {
            mLogger.close();
            mLogger = prepareLogger(logFileName);
        }

        if (mLogger != null) {
            mLogger.log(tag, message, 0);
        }

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
        mContext = context;
        mEnableLogging = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_logging", false);
    }

    private File prepareLogFolder() {
        File logFolder = mContext.getExternalFilesDir(LOG_FOLDER_NAME);
        if (logFolder == null) {
            logFolder = new File(mContext.getFilesDir(), LOG_FOLDER_NAME);
        }

        if (!logFolder.exists() && !logFolder.mkdir()) {
            Log.d("LoggerHelper", "Log folder does not exist and can't be created");
            return null;
        }

        return logFolder;
    }

    private AbstractLogger prepareLogger(String logFileName) {
        // String logFileName = DateFormatterHelper.formatLogFileDate(System.currentTimeMillis()) + ".log";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new MediaStoreLogger(
                    mContext,
                    MediaStoreUtils.MEDIA_STORE_ROOT_PATH + "/" + LOCAL_APP_FOLDER_NAME + LOG_FOLDER_NAME + "/",
                    logFileName
            );
        } else {
            File logFolder = prepareLogFolder();
            if (logFolder != null) {
                return new FileLogger(logFolder.getPath() + "/", logFileName);
            } else {
                return null;
            }
        }
    }
}
