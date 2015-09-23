package com.romanpulov.symphonytimer.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class AssetsHelper {
	public static String TAG = "AssetsHelper";
	
	public static void listAssets(Context context, String path) {		
		AssetManager assetManager = context.getResources().getAssets();
		Log.d(TAG, "Before");
		
		//String destFolder = Environment.getExternalStorageDirectory().toString().concat(context.getPackageName()).concat("media");
		
		StringBuilder destFolderStringBuilder = new StringBuilder(Environment.getExternalStorageDirectory().toString());		
		//destFolderStringBuilder.append("/").append(context.getPackageName()).append("/").append("media");
		destFolderStringBuilder.append("/").append(context.getPackageName());
		String packageFolder = destFolderStringBuilder.toString();
		File packageFolderFile = new File(packageFolder);
		
		destFolderStringBuilder.append("/media");
		String destFolder = destFolderStringBuilder.toString();
		File destFolderFile = new File(destFolder);
		
		Intent mediaScannerIntent = null;
		
		Log.d(TAG, destFolder);
		try {
			for (String s : assetManager.list(path)) {
				Log.d(TAG, s);
				File destFile = new File(destFolder, s);				
				Log.d(TAG, destFile.getPath());
				if (!destFile.exists()) {
					Log.d(TAG, "not exists");
					
					if (!packageFolderFile.exists()) {
						if (!packageFolderFile.mkdir()) {
							Log.d(TAG, "Failed to create " + packageFolder);
							return;
						}
					}
					
					if (!destFolderFile.exists()) {
						if (!destFolderFile.mkdir()) {
							Log.d(TAG, "Failed to create " + destFolder);
							return;
						}
					}
					
					InputStream inStream = assetManager.open(path.concat("/").concat(s));
					Log.d(TAG, "Created InputStream");
					
					FileOutputStream outStream = new FileOutputStream(destFile);
					Log.d(TAG, "Created OutputStream");
					
					try {
						byte[] buf = new byte[1024];
						int len;
						 while ((len = inStream.read(buf)) > 0) {
							 outStream.write(buf, 0, len);
						 }
						 Log.d(TAG, "File is written");						 						 

					} finally {
						inStream.close();
						outStream.flush();
						outStream.close();
					}
					
					// update media library
					if (null == mediaScannerIntent) {
						mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					}
					Log.d(TAG, "Setting dest file for media scanner");
					mediaScannerIntent.setData(Uri.fromFile(destFile));
					Log.d(TAG, "Sending broadcast");
					context.sendBroadcast(mediaScannerIntent);
				}
				
			}
			Log.d(TAG, "After");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
