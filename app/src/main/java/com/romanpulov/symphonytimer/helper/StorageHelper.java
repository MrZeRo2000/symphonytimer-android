package com.romanpulov.symphonytimer.helper;

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
import android.util.Log;

import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.helper.db.DBOpenHelper;
import com.romanpulov.symphonytimer.helper.db.DBXMLHelper;

public class StorageHelper {
	
	private static final String TAG = "StorageManager";
	
	private static final String LOCAL_BACKUP_FOLDER_NAME = "SymphonyTimerBackup";
	private static final String LOCAL_BACKUP_FILE_NAME = "symphonytimerdb";
	
	//private static StorageManager storageManagerInstance = null;
	private Context mContext;
	
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
	
	public StorageHelper(Context context) {
		this.mContext = context;
		initLocalFileNames();
	}
	
	private String getDatabasePath () {
		return mContext.getDatabasePath(DBOpenHelper.DATABASE_NAME).toString();
	}
	
	public String createLocalBackup() {		
		
		Log.d(TAG, mLocalBackupFolderName);
		Log.d(TAG, mDestDBFileName);
		
		// create backup folder if not exists
		File backupFolder = new File(mLocalBackupFolderName);
		if (!backupFolder.exists()) {
			
			Log.d(TAG, "Folder does not exist, creating one");
			
			if (!backupFolder.mkdir()) {
				
				Log.d(TAG, "Unable to create folder");
				
				return null;
			}
		}
		
		//IO operations
		try {
		
			//copy source database to backup folder
			Log.d(TAG, "Creating Input Stream");
			FileInputStream inStream = new FileInputStream(mSourceDBFileName);
			Log.d(TAG, "Creating Output Stream");
			FileOutputStream outStream = new FileOutputStream(mDestDBFileName);
			try {
				
				Log.d(TAG, "Copying file");
				byte[] buf = new byte[1024];
				int len;
				 while ((len = inStream.read(buf)) > 0) {
					 outStream.write(buf, 0, len);
				 }				
				
			} 
			finally {
				
				inStream.close();
				outStream.flush();
				outStream.close();
			}
			
			Log.d(TAG, "File copied");
			
			// generate xml
			File xmlFile = new File(mDestXmlFileName);
			FileWriter xmlFileWriter = new FileWriter(xmlFile);
			new DBXMLHelper(mContext).writeDBXML(xmlFileWriter);
			xmlFileWriter.flush();
			xmlFileWriter.close();
			
			Log.d(TAG, "XML generated");
			
			//for test only - parsing XML
			//FileInputStream xmlInputStream = new FileInputStream(destXmlFileName);
			//DBXMLHelper.getInstance(context).parseDBXML(xmlInputStream);
			
			
		} catch (IOException e) {
			
			e.printStackTrace();
			return null;
			
		}		

		return mDestXmlFileName;
		
	}	
	
	public int restoreLocalXmlBackup() {
		
		int res = 0;
		
		try {
			
			InputStream xmlInputStream = new BufferedInputStream(new FileInputStream(mDestXmlFileName));
			Map<String, List<DBHelper.RawRecItem>> tableData = new HashMap<String, List<DBHelper.RawRecItem>>() ;
			
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
