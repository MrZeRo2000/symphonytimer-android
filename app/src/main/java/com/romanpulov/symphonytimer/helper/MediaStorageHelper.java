package com.romanpulov.symphonytimer.helper;

import android.content.Context;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

public class MediaStorageHelper {
    final public static int MEDIA_TYPE_IMAGE = 0;
    final public static int MEDIA_TYPE_SOUND = 1;
    final private static String[] MEDIA_TAGS = {"IMG", "SND"};

    private static MediaStorageHelper mediaStorageHelperInstance;
    private File mDir;

    public static MediaStorageHelper getInstance(Context context) {
        if (mediaStorageHelperInstance == null)
            mediaStorageHelperInstance = new MediaStorageHelper(context);
        return mediaStorageHelperInstance;
    }

    private MediaStorageHelper(Context context) {
        mDir = context.getCacheDir();
    }

    public File createMediaFile(int mediaType, int mediaId) {
        File result;
        try {
            result = File.createTempFile(MEDIA_TAGS[mediaType] + mediaId, "IMG", mDir);
            return result;
        } catch (IOException e) {
            return null;
        }
    }

    public void cleanupMedia(final List<String> fileNames) {
        File[] cleanupFiles = mDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                boolean result = true;
                for (String name : fileNames) {
                    if (s.equals(name))
                        return false;
                }
                return result;
            }
        });

        for (File f : cleanupFiles) {
            f.delete();
        }
    }

}
