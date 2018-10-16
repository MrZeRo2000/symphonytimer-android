package com.romanpulov.symphonytimer.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.net.Uri;

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
                try {
                    inStream.close();
                    outStream.flush();
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    public static Uri fileNameToUri(Context context, String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return Uri.parse(Uri.fromFile(file).toString());
        }	else
            return null;
    }
}
