package com.romanpulov.symphonytimer.helper.db;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.os.Build;

import com.romanpulov.jutilscore.io.ZipFileUtils;
import com.romanpulov.jutilscore.storage.BackupProcessor;
import com.romanpulov.jutilscore.storage.BackupUtils;
import com.romanpulov.jutilscore.storage.FileBackupProcessor;
import com.romanpulov.library.common.backup.MediaStoreBackupProcessor;
import com.romanpulov.library.common.media.MediaStoreUtils;

public class DBStorageHelper {
	private static final String LOCAL_BACKUP_FOLDER_NAME = "SymphonyTimerBackup";
    // database backup file name
    private static final String LOCAL_BACKUP_DB_FILE_NAME = "symphonytimerdb_" + DBOpenHelper.DATABASE_VERSION;

    // private final BackupUtils mDatabaseBackupUtils;

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
        /*

        mDatabaseBackupUtils = new BackupUtils(
                mContext.getDatabasePath(DBOpenHelper.DATABASE_NAME).toString(),
                getLocalBackupFolderName(),
                LOCAL_BACKUP_DB_FILE_NAME);

         */
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

    public static BackupProcessor createBackupProcessor(Context context, String dataFileName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new MediaStoreBackupProcessor(
                    context,
                    dataFileName,
                    MediaStoreUtils.MEDIA_STORE_ROOT_PATH + '/' + LOCAL_BACKUP_FOLDER_NAME + '/',
                    LOCAL_BACKUP_DB_FILE_NAME
            );
        } else {
            File f = context.getExternalFilesDir(LOCAL_BACKUP_FOLDER_NAME);
            if (f == null) {
                // should not get here
                f = new File(context.getFilesDir(), LOCAL_BACKUP_FOLDER_NAME);
            }

            return new FileBackupProcessor(
                    dataFileName,
                    f.getAbsolutePath() + '/',
                    LOCAL_BACKUP_DB_FILE_NAME
                    );
        }
    }

    public static String createLocalBackup(Context context) {
        DBHelper dbHelper = DBHelper.getInstance(context);

        BackupProcessor bp = createBackupProcessor(
                context,
                context.getDatabasePath(DBOpenHelper.DATABASE_NAME).toString()
        );

        dbHelper.closeDB();
        String result = bp.createRollingBackup();
        dbHelper.openDB();

        return result;
    }

    public static String restoreLocalBackup(Context context) {
        DBHelper dbHelper = DBHelper.getInstance(context);

        BackupProcessor bp = createBackupProcessor(
                context,
                context.getDatabasePath(DBOpenHelper.DATABASE_NAME).toString()
        );

        dbHelper.closeDB();
        String result = bp.restoreBackup();
        dbHelper.openDB();

        dbHelper.setDBDataChanged();

        return result;
    }

    /**
     * Creates local backup and returns backup file name if successful     *
     * @return backup file name
     */
	public String createLocalBackup() {
	    DBHelper.getInstance(mContext).closeDB();
        String result = null;
	    /*
        String result = mDatabaseBackupUtils.createRollingLocalBackup();

	     */
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

        String result = null;
        /*
        String result = mDatabaseBackupUtils.restoreBackup();

         */
        dbHelper.openDB();
        dbHelper.setDBDataChanged();

        return result;
    }

    public static String restorePathBackup(Context context, String restorePath) {
        /*
        BackupUtils restoreUtils = new BackupUtils(
                context.getDatabasePath(DBOpenHelper.DATABASE_NAME).toString(),
                restorePath,
                LOCAL_BACKUP_DB_FILE_NAME
        );

         */

        DBHelper dbHelper = DBHelper.getInstance(context);
        dbHelper.closeDB();

        String result = null;
        /*
        String result = restoreUtils.restoreBackup();

         */
        dbHelper.openDB();
        dbHelper.setDBDataChanged();

        return result;
    }

    public static List<String> getDatabaseBackupFiles(Context context) {
        return createBackupProcessor(context, null).getBackupFileNames();
    }

    public static InputStream createBackupInputStream(Context context, String backupFileName) throws IOException {
        return createBackupProcessor(context, LOCAL_BACKUP_DB_FILE_NAME).createBackupInputStream(backupFileName);
    }

    /**
     * Returns list of database backup files
     * @return Files
     */
    public File[] getDatabaseBackupFiles()  {
        return null;
        /*
        return mDatabaseBackupUtils.getBackupFiles();

         */
    }

    /**
     * Returns local backup Zip file name
     * @return Zip file name
     */
    public static String getBackupZipFileName() {
        return ZipFileUtils.getZipFileName(LOCAL_BACKUP_DB_FILE_NAME);
    }
}
