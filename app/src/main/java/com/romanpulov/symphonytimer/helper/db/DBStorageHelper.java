package com.romanpulov.symphonytimer.helper.db;

import java.io.File;

import android.content.Context;

import com.romanpulov.library.common.io.ZipFileUtils;
import com.romanpulov.library.common.storage.BackupUtils;

public class DBStorageHelper {
	private static final String LOCAL_BACKUP_FOLDER_NAME = "SymphonyTimerBackup";
    // database backup file name
    private static final String LOCAL_BACKUP_DB_FILE_NAME = "symphonytimerdb_" + DBOpenHelper.DATABASE_VERSION;

    private final BackupUtils mDatabaseBackupUtils;

	private final Context mContext;

    private String getLocalBackupFolderName() {
        File f = mContext.getExternalFilesDir(LOCAL_BACKUP_FOLDER_NAME);
        if (f == null) {
            f = new File(mContext.getFilesDir(), LOCAL_BACKUP_FOLDER_NAME);
        }
        return f.getAbsolutePath();
    }
	
	public DBStorageHelper(Context context) {
        this.mContext = context;

        mDatabaseBackupUtils = new BackupUtils(
                mContext.getDatabasePath(DBOpenHelper.DATABASE_NAME).toString(),
                getLocalBackupFolderName(),
                LOCAL_BACKUP_DB_FILE_NAME);
	}

    public static boolean restoreFromBackupPath(Context context, String path) {
        File file = new File(path);
        if (file.exists()) {
            String restoreResult = DBStorageHelper.restorePathBackup(context, file.getParent());

            return  restoreResult != null;
        } else {
            return false;
        }
    }

    /**
     * Creates local backup and returns backup file name if successful     *
     * @return backup file name
     */
	public String createLocalBackup() {
	    DBHelper.getInstance(mContext).closeDB();
        String result = mDatabaseBackupUtils.createRollingLocalBackup();
        DBHelper.getInstance(mContext).openDB();

        return result;
	}

    /**
     * Restores local backup
     * @return Restored file name if successful
     */
    public String restoreLocalBackup() {
        DBHelper dbHelper = DBHelper.getInstance(mContext);
        dbHelper.closeDB();
        String result = mDatabaseBackupUtils.restoreBackup();
        dbHelper.openDB();
        dbHelper.setDBDataChanged();

        return result;
    }

    public static String restorePathBackup(Context context, String restorePath) {
        BackupUtils restoreUtils = new BackupUtils(
                context.getDatabasePath(DBOpenHelper.DATABASE_NAME).toString(),
                restorePath,
                LOCAL_BACKUP_DB_FILE_NAME
        );

        DBHelper dbHelper = DBHelper.getInstance(context);
        dbHelper.closeDB();
        String result = restoreUtils.restoreBackup();
        dbHelper.openDB();
        dbHelper.setDBDataChanged();

        return result;
    }

    /**
     * Returns list of database backup files
     * @return Files
     */
    public File[] getDatabaseBackupFiles()  {
        return mDatabaseBackupUtils.getBackupFiles();
    }

    /**
     * Returns local backup Zip file name
     * @return Zip file name
     */
    public static String getBackupZipFileName() {
        return ZipFileUtils.getZipFileName(LOCAL_BACKUP_DB_FILE_NAME);
    }
}
