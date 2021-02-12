package com.romanpulov.symphonytimer.helper.db;

import android.content.Context;

import androidx.annotation.NonNull;

import com.romanpulov.jutilscore.io.ZipFileUtils;
import com.romanpulov.library.common.db.DBBackupManager;

public class DBStorageHelper {
    private static DBStorageHelper mInstance = null;

	private static final String LOCAL_BACKUP_FOLDER_NAME = "SymphonyTimerBackup";
    // database backup file name
    private static final String LOCAL_BACKUP_DB_FILE_NAME = "symphonytimerdb_" + DBOpenHelper.DATABASE_VERSION;

    private final DBBackupManager mDBBackupManager;

    public static DBStorageHelper getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new DBStorageHelper(context.getApplicationContext());
        }
        return mInstance;
    }

	private DBStorageHelper(Context context) {
        mDBBackupManager = new DBBackupManager(
                context,
                LOCAL_BACKUP_FOLDER_NAME,
                LOCAL_BACKUP_DB_FILE_NAME,
                DBHelper.getInstance(context)
                );
	}

	public DBBackupManager getDBBackupManager() {
        return mDBBackupManager;
    }

    /**
     * Returns local backup Zip file name
     * @return Zip file name
     */
    @NonNull
    public static String getBackupZipFileName() {
        return ZipFileUtils.getZipFileName(LOCAL_BACKUP_DB_FILE_NAME);
    }
}
