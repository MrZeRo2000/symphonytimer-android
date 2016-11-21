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

public class AssetsHelper {

	public static void listAssets(Context context, String path) {		
		AssetManager assetManager = context.getResources().getAssets();

		StringBuilder destFolderStringBuilder = new StringBuilder(Environment.getExternalStorageDirectory().toString());
		destFolderStringBuilder.append("/").append(context.getPackageName());
		String packageFolder = destFolderStringBuilder.toString();
		File packageFolderFile = new File(packageFolder);
		
		destFolderStringBuilder.append("/media");
		String destFolder = destFolderStringBuilder.toString();
		File destFolderFile = new File(destFolder);
		
		Intent mediaScannerIntent = null;
		
		try {
			for (String s : assetManager.list(path)) {
				File destFile = new File(destFolder, s);
				if (!destFile.exists()) {
					if (!packageFolderFile.exists()) {
						if (!packageFolderFile.mkdir()) {
							return;
						}
					}
					
					if (!destFolderFile.exists()) {
						if (!destFolderFile.mkdir()) {
							return;
						}
					}
					
					InputStream inStream = assetManager.open(path.concat("/").concat(s));
					FileOutputStream outStream = new FileOutputStream(destFile);
					try {
						byte[] buf = new byte[1024];
						int len;
						 while ((len = inStream.read(buf)) > 0) {
							 outStream.write(buf, 0, len);
						 }
					} finally {
						try {
							inStream.close();
							outStream.flush();
							outStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					// update media library
					if (null == mediaScannerIntent) {
						mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					}
					mediaScannerIntent.setData(Uri.fromFile(destFile));
					context.sendBroadcast(mediaScannerIntent);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
