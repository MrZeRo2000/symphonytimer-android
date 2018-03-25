package com.romanpulov.symphonytimer.helper.db;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Environment;

import com.romanpulov.library.common.io.ZipFileUtils;
import com.romanpulov.library.common.storage.BackupUtils;

public class DBStorageHelper {
	private static final String LOCAL_BACKUP_FOLDER_NAME = "SymphonyTimerBackup";
    // database backup file name
    private static final String LOCAL_BACKUP_DB_FILE_NAME = "symphonytimerdb_" + DBOpenHelper.DATABASE_VERSION;;
    // XML backup file name
	private static final String LOCAL_BACKUP_FILE_NAME = "symphonytimerdata";
    // XML data file
    private static final String LOCAL_BACKUP_DATA_FILE_NAME = "symphonytimerdbdata";

	private final BackupUtils mXMLBackupUtils;
    private final BackupUtils mDatabaseBackupUtils;

	private final Context mContext;
	
    private final String mXMLFileName;

    private String getLocalBackupFolderName() {
        return Environment.getExternalStorageDirectory() + File.separator + LOCAL_BACKUP_FOLDER_NAME;
    }
	
	public DBStorageHelper(Context context) {
        this.mContext = context;

        mXMLFileName = BackupUtils.getBackupFolderName(getLocalBackupFolderName()) + LOCAL_BACKUP_DATA_FILE_NAME;
		mXMLBackupUtils = new BackupUtils(mXMLFileName, getLocalBackupFolderName(), LOCAL_BACKUP_FILE_NAME);
        mDatabaseBackupUtils = new BackupUtils(
                mContext.getDatabasePath(DBOpenHelper.DATABASE_NAME).toString(),
                getLocalBackupFolderName(),
                LOCAL_BACKUP_DB_FILE_NAME);
	}

    /**
     * Creates local backup and returns backup file name if successful     *
     * @return backup file name
     */
	public String createLocalBackup() {

        mDatabaseBackupUtils.createRollingLocalBackup();

        // write XML file
        File xmlFile = new File(mXMLFileName);
        try {
            FileWriter xmlFileWriter = new FileWriter(xmlFile);
            new DBXMLHelper(mContext).writeDBXML(xmlFileWriter);
            try {
                xmlFileWriter.flush();
                xmlFileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // backup
            mXMLBackupUtils.createRollingLocalBackup();

            // delete XML file and ignore any errors
            xmlFile.delete();

            return mXMLFileName;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
	}

    /**
     * Restore from local backup and return error code:
     * 1 - local backup not found
     * 2 - local backup could not be unarchived
     * 3 - XML data parse error
     * @return error code or 0 if successful
     */
	public int restoreLocalXmlBackup() {
        int result = 0;

        if (mXMLBackupUtils.restoreBackup() != null) {

            File xmlFile = new File(mXMLFileName);

            try {
                InputStream xmlInputStream = new BufferedInputStream(new FileInputStream(xmlFile));
                Map<String, List<DBHelper.RawRecItem>> tableData = new HashMap<>() ;

                //reading data
                int res = new DBXMLHelper(mContext).parseDBXML(xmlInputStream, tableData);
                if (0 == res) {
                    DBHelper.getInstance(mContext).restoreBackupData(tableData);
                    //delete file ignoring errors
                    xmlFile.delete();
                } else
                    result = 3;

            } catch (FileNotFoundException e) {
                result = 2;
                e.printStackTrace();
            }

        } else
            result = 1;

        return result;
	}

    /**
     * Returns list of XML backup files
     * @return Files
     */
    public File[] getXMLBackupFiles()  {
        return mXMLBackupUtils.getBackupFiles();
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
        return ZipFileUtils.getZipFileName(LOCAL_BACKUP_FILE_NAME);
    }
}
