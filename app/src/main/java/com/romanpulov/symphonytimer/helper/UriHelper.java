package com.romanpulov.symphonytimer.helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;

public final class UriHelper {

    public static boolean uriSaveToFile(Context context, Uri uri, File file) {
        try {
            InputStream inStream = context.getContentResolver().openInputStream(uri);
            if (inStream == null)
                return false;
            OutputStream outStream = new FileOutputStream(file);
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = inStream.read(buf)) > 0) {
                    outStream.write(buf, 0, len);
                }
            } finally {
                inStream.close();
                outStream.flush();
                outStream.close();
            }
        }
        catch (FileNotFoundException e) {
            return  false;
        }
        catch (IOException e) {
            return false;
        }

        return true;
    }

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
