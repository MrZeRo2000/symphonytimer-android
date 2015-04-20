package com.romanpulov.symphonytimer;

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

public class StorageHelper {
	
	private static final String TAG = "StorageManager";
	
	private static final String LOCAL_BACKUP_FOLDER_NAME = "SymphonyTimerBackup";
	private static final String LOCAL_BACKUP_FILE_NAME = "symphonytimerdb";
	
	//private static StorageManager storageManagerInstance = null;
	private Context context;
	
	private String sourceDBFileName;
	private String localBackupFolderName; 
	private String destDBFileName;
	private String destXmlFileName;
	
	private void initLocalFileNames () {
		
		this.sourceDBFileName = getDatabasePath();
		
		StringBuilder destFolderStringBuilder = new StringBuilder(Environment.getExternalStorageDirectory().toString());	
		destFolderStringBuilder.append("/").append(LOCAL_BACKUP_FOLDER_NAME);
		this.localBackupFolderName = destFolderStringBuilder.toString();
		
		destFolderStringBuilder.append("/").append(LOCAL_BACKUP_FILE_NAME);
		this.destDBFileName = destFolderStringBuilder.toString();
		
		destFolderStringBuilder.append(".xml");
		this.destXmlFileName = destFolderStringBuilder.toString();	
		
	}
	
	public StorageHelper(Context context) {
		this.context = context;
		initLocalFileNames();
	}
	
	private String getDatabasePath () {
		return context.getDatabasePath(DBOpenHelper.DATABASE_NAME).toString();
	}
	
	public String createLocalBackup() {		
		
		Log.d(TAG, localBackupFolderName);
		Log.d(TAG, destDBFileName);
		
		// create backup folder if not exists
		File backupFolder = new File(localBackupFolderName);
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
			FileInputStream inStream = new FileInputStream(sourceDBFileName);
			Log.d(TAG, "Creating Output Stream");
			FileOutputStream outStream = new FileOutputStream(destDBFileName);
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
			new DBXMLHelper(context).writeDBXML(xmlFileWriter);
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

		return destXmlFileName;
		
	}	
	
	public int restoreLocalXmlBackup() {
		
		int res = 0;
		
		try {
			
			InputStream xmlInputStream = new BufferedInputStream(new FileInputStream(destXmlFileName));
			Map<String, List<DBHelper.RawRecItem>> tableData = new HashMap<String, List<DBHelper.RawRecItem>>() ;
			
			//reading data
			res = new DBXMLHelper(context).parseDBXML(xmlInputStream, tableData);
			
			if (0 == res) {				
				DBHelper.getInstance(context).restoreBackupData(tableData);				
			}
		
		} catch (FileNotFoundException e) {
			
			res = 1;
			e.printStackTrace();
			
		}
		
		return res;
		
	}

}
