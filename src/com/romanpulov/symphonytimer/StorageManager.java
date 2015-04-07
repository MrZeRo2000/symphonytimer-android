package com.romanpulov.symphonytimer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class StorageManager {
	
	private static final String TAG = "StorageManager";
	
	private static final String LOCAL_BACKUP_FOLDER_NAME = "SymphonyTimerBackup";
	private static final String LOCAL_BACKUP_FILE_NAME = "symphonytimerdb";
	
	private static StorageManager storageManagerInstance = null;
	private Context context;
	
	private StorageManager(Context context) {
		this.context = context;
		
	}
	
	public static StorageManager getInstance(Context context) {
		
		if (null == storageManagerInstance) {
			storageManagerInstance = new StorageManager(context);
		}
		
		return storageManagerInstance;
	}
	
	private String getDatabasePath () {
		return context.getDatabasePath(DBOpenHelper.DATABASE_NAME).toString();
	}
	
	public String CreateLocalBackup() {
		
		final String sourceFileName = getDatabasePath();
		StringBuilder destFolderStringBuilder = new StringBuilder(Environment.getExternalStorageDirectory().toString());	
		destFolderStringBuilder.append("/").append(LOCAL_BACKUP_FOLDER_NAME);
		final String backupFolderName = destFolderStringBuilder.toString();
		destFolderStringBuilder.append("/").append(LOCAL_BACKUP_FILE_NAME);
		final String destFileName = destFolderStringBuilder.toString();
		destFolderStringBuilder.append(".xml");
		final String destXmlFileName = destFolderStringBuilder.toString();
		
		Log.d(TAG, backupFolderName);
		Log.d(TAG, destFileName);
		
		// create backup folder if not exists
		File backupFolder = new File(backupFolderName);
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
			FileInputStream inStream = new FileInputStream(sourceFileName);
			Log.d(TAG, "Creating Output Stream");
			FileOutputStream outStream = new FileOutputStream(destFileName);
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
			File xmlFile = new File(destXmlFileName);
			FileWriter xmlFileWriter = new FileWriter(xmlFile);
			DBXMLHelper.getInstance(context).writeDBXML(xmlFileWriter);
			xmlFileWriter.flush();
			xmlFileWriter.close();
			
			Log.d(TAG, "XML generated");
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}		

		return destFileName;
		
	}	

}
