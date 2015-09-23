package com.romanpulov.symphonytimer.helper;

import java.io.File;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;

public final class UriHelper {	
	
	public static String uriMediaToFileName(Context context, Uri contentUri) {		
		Cursor cursor = null;
		
		if (ContentResolver.SCHEME_FILE.equals(contentUri.getScheme())) {
			return contentUri.getPath();
			
		} else {
		
			try {			
			    String[] proj = { MediaColumns.DATA };
			    cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
			    int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
			    cursor.moveToFirst();
			    return cursor.getString(column_index);
			} finally {
				if (cursor != null) {
					cursor.close();
			    }
			}
		}
	}
	
	public static Uri fileNameToUri(Context context, String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			return Uri.parse(Uri.fromFile(file).toString());
		}	else
			return null;
	}
	
	public static Uri getImageContentUri(Context context, String fileName) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { fileName }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
       } else {
    	   return null;
       }
    }
	
	public static Uri getSoundContentUri(Context context, String fileName) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media._ID },
                MediaStore.Audio.Media.DATA + "=? ",
                new String[] { fileName }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + id);
       } else {
    	   return null;
       }
    }
	
	public static String getSoundTitleFromFileName(Context context, String fileName) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media.TITLE },
                MediaStore.Audio.Media.DATA + "=? ",
                new String[] { fileName }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns.TITLE));
            return cursor.getString(id);
       } else {
    	   return null;
       }
    }
	
	public static String getSoundTitleFromUri(Context context, Uri uri) {
		String res = null;
        Cursor mCursor = context.getContentResolver().query(
        		uri,   // The content URI of the words table
        	    new String[] {MediaStore.Audio.Media.TITLE},                        // The columns to return for each row
        	    null,                    // Selection criteria
        	    null,                     // Selection criteria
        	    null);
        if (null != mCursor)  {
        	try {
        		mCursor.moveToFirst();
        		res = mCursor.getString(0);        		
        	} finally {
        		mCursor.close();
        	}
        }
        return res;
	}
	
	
	
	
}
