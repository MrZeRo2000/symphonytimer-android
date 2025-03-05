package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import android.os.Environment;

import android.util.Log;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MediaStorageHelper {
    private static final String TAG = MediaStorageHelper.class.getSimpleName();

    final public static int MEDIA_TYPE_IMAGE = 0;
    final public static int MEDIA_TYPE_SOUND = 1;
    final private static String[] MEDIA_TAGS = {"IMG", "SND", "REC"};

    private static MediaStorageHelper mediaStorageHelperInstance;
    private final File mDir;

    public static MediaStorageHelper getInstance(Context context) {
        if (mediaStorageHelperInstance == null)
            mediaStorageHelperInstance = new MediaStorageHelper(context);
        return mediaStorageHelperInstance;
    }

    private MediaStorageHelper(@NonNull Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mDir = context.getExternalFilesDir(null);
        } else {
            mDir = context.getCacheDir();
        }
    }

    public static int mediaTypeFromMIMEType(String mimeType) {
        if (mimeType.startsWith("image/")) {
            return MEDIA_TYPE_IMAGE;
        } else if (mimeType.startsWith("audio/")) {
            return MEDIA_TYPE_SOUND;
        } else {
            return -1;
        }
    }

    public File createMediaFile(int mediaType, int mediaId) {
        File result;
        try {
            result = File.createTempFile(MEDIA_TAGS[mediaType] + mediaId, MEDIA_TAGS[mediaType], mDir);
            return result;
        } catch (IOException e) {
            Log.e(TAG, "createMediaFile error: " + e.getMessage(), e);
            return null;
        }
    }

    public void cleanupMedia(final List<String> fileNames) {
        File[] cleanupFiles = mDir.listFiles((file, s) -> {
            if (s == null)
                return false;

            String prefix = s.substring(0, 3);
            boolean tagFound = false;
            for (String mediaTag : MEDIA_TAGS) {
                if (prefix.equals(mediaTag)) {
                    tagFound = true;
                    break;
                }
            }
            if (!tagFound)
                return  false;

            String filterFileName = file.getPath() + "/" + s;
            for (String name : fileNames) {
                if (filterFileName.equals(name))
                    return false;
            }
            return true;
        });

        if (cleanupFiles != null) {
            for (File f : cleanupFiles) {
                f.delete();
            }
        }
    }
}
