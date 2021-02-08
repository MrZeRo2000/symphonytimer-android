package com.romanpulov.symphonytimer.helper.db;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.romanpulov.jutilscore.io.ZipFileUtils;
import com.romanpulov.jutilscore.storage.BackupProcessor;
import com.romanpulov.jutilscore.storage.FileBackupProcessor;
import com.romanpulov.library.common.backup.MediaStoreBackupProcessor;
import com.romanpulov.library.common.media.MediaStoreUtils;

public class DBStorageHelper {
	private static final String LOCAL_BACKUP_FOLDER_NAME = "SymphonyTimerBackup";
    // database backup file name
    private static final String LOCAL_BACKUP_DB_FILE_NAME = "symphonytimerdb_" + DBOpenHelper.DATABASE_VERSION;

	private DBStorageHelper() {
	}

    /**
     * Restores backup from a temporary file
     * @param context Context to create BackupProcessor
     * @param path File path to restore
     * @return success flag
     */
    public static boolean restoreFromBackupPath(@NonNull Context context, String path) {
        File file = new File(path);
        if (file.exists()) {
            String restoreResult = DBStorageHelper.restorePathBackup(context, file.getParent());

            return  restoreResult != null;
        } else {
            return false;
        }
    }

    /**
     * Create internally BackupProcessor to support differences between Android versions
     * @param context Context
     * @param dataFileName Data file name
     * @return BackupProcessor implementation
     */
    @NonNull
    private static BackupProcessor createBackupProcessor(@NonNull Context context, String dataFileName) {
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

    /**
     * Creates backup locally
     * @param context Context
     * @return backup creation result as file name, null if failed
     */
    public static String createLocalBackup(@NonNull Context context) {
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

    /**
     * Restores backup from local file via BackupProcessor
     * @param context Context
     * @return restore result as file name, null if failed
     */
    public static String restoreLocalBackup(@NonNull Context context) {
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
     * Restores backup from local file via FileBackupProcessor
     * @param context Context
     * @return restore result as file name, null if failed
     */
    public static String restorePathBackup(@NonNull Context context, String restorePath) {

        BackupProcessor bp =  new FileBackupProcessor(
                context.getDatabasePath(DBOpenHelper.DATABASE_NAME).toString(),
                restorePath,
                LOCAL_BACKUP_DB_FILE_NAME
        );

        DBHelper dbHelper = DBHelper.getInstance(context);
        dbHelper.closeDB();
        String result = bp.restoreBackup();
        dbHelper.openDB();

        dbHelper.setDBDataChanged();

        return result;
    }

    /**
     * Returns database backup file names
     * @param context Context
     * @return File name list
     */
    public static List<String> getDatabaseBackupFiles(@NonNull Context context) {
        return createBackupProcessor(context, null).getBackupFileNames();
    }

    /**
     * Creates InputStream for backup
     * @param context Context
     * @param backupFileName Backup file name
     * @return InputStream
     * @throws IOException
     */
    public static InputStream createBackupInputStream(@NonNull Context context, String backupFileName) throws IOException {
        return createBackupProcessor(context, LOCAL_BACKUP_DB_FILE_NAME).createBackupInputStream(backupFileName);
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
