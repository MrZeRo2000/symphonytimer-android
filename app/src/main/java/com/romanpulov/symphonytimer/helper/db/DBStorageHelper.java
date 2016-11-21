package com.romanpulov.symphonytimer.helper.db;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Environment;

public class DBStorageHelper {
	private static final String LOCAL_BACKUP_FOLDER_NAME = "SymphonyTimerBackup";
	private static final String LOCAL_BACKUP_FILE_NAME = "symphonytimerdb";
	
	private final Context mContext;
	
	private String mSourceDBFileName;
	private String mLocalBackupFolderName; 
	private String mDestDBFileName;
	private String mDestXmlFileName;
	
	private void initLocalFileNames () {
		this.mSourceDBFileName = getDatabasePath();
		
		StringBuilder destFolderStringBuilder = new StringBuilder(Environment.getExternalStorageDirectory().toString());	
		destFolderStringBuilder.append("/").append(LOCAL_BACKUP_FOLDER_NAME);
		this.mLocalBackupFolderName = destFolderStringBuilder.toString();
		
		destFolderStringBuilder.append("/").append(LOCAL_BACKUP_FILE_NAME);
		this.mDestDBFileName = destFolderStringBuilder.toString();
		
		destFolderStringBuilder.append(".xml");
		this.mDestXmlFileName = destFolderStringBuilder.toString();
	}
	
	public DBStorageHelper(Context context) {
		this.mContext = context;
		initLocalFileNames();
	}
	
	private String getDatabasePath () {
		return mContext.getDatabasePath(DBOpenHelper.DATABASE_NAME).toString();
	}
	
	public String createLocalBackup() {		
		// create backup folder if not exists
		File backupFolder = new File(mLocalBackupFolderName);
		if (!backupFolder.exists()) {
			if (!backupFolder.mkdir()) {
				return null;
			}
		}
		
		//IO operations
		try {
			//copy source database to backup folder
			FileInputStream inStream = new FileInputStream(mSourceDBFileName);
			FileOutputStream outStream = new FileOutputStream(mDestDBFileName);
			try {
				byte[] buf = new byte[1024];
				int len;
				 while ((len = inStream.read(buf)) > 0) {
					 outStream.write(buf, 0, len);
				 }				
			}
			finally {
				try {
					inStream.close();
					outStream.flush();
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// generate xml
			File xmlFile = new File(mDestXmlFileName);
			FileWriter xmlFileWriter = new FileWriter(xmlFile);
			new DBXMLHelper(mContext).writeDBXML(xmlFileWriter);
			xmlFileWriter.flush();
			xmlFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return mDestXmlFileName;
	}
	
	public int restoreLocalXmlBackup() {
		int res;
		try {
			InputStream xmlInputStream = new BufferedInputStream(new FileInputStream(mDestXmlFileName));
			Map<String, List<DBHelper.RawRecItem>> tableData = new HashMap<>() ;
			//reading data
			res = new DBXMLHelper(mContext).parseDBXML(xmlInputStream, tableData);
			if (0 == res) {
				DBHelper.getInstance(mContext).restoreBackupData(tableData);				
			}
		
		} catch (FileNotFoundException e) {
			res = 1;
			e.printStackTrace();
		}
		
		return res;
	}
}
