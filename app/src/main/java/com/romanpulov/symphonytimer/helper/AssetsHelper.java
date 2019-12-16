package com.romanpulov.symphonytimer.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;

public class AssetsHelper {
    private void log(String message) {
        LoggerHelper.logContext(mContext, "AssetsHelper", message);
    }

    private static final String PATH = "pre_inst_images";
    private static final String PREF_NAME = "assets_private";
    private static final String PREF_FIRST_RUN_PARAM_NAME = "is_first_run";
    private static final String DEST_PATH = "symphonytimer";

    private final Context mContext;
    private final AssetManager mAssetManager;
    private final File mDestFolderPathFile;

    public AssetsHelper(@NonNull Context context) {
        this.mContext = context.getApplicationContext();
        this.mAssetManager = mContext.getResources().getAssets();
        this.mDestFolderPathFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }

	public List<String> getAssets() {
        List<String> result = new ArrayList<>();

        if (mAssetManager != null) {
            String[] list = null;
            File destFolderFile = null;
            try {
                list = mAssetManager.list(PATH);
                destFolderFile = new File(mDestFolderPathFile.getAbsolutePath() + File.separator + DEST_PATH);
            } catch (IOException e) {
                log(e.getMessage());
                e.printStackTrace();
            }

            if (list != null) {
                for (String s : list) {
                    if (destFolderFile.exists()) {
                        File destFile = new File(destFolderFile, s);
                        if (!destFile.exists()) {
                            result.add(s);
                        }
                    } else {
                        result.add(s);
                    }
                }
            }
        }

        return result;
    }

    public void copyAssets(List<String> assets) {
        if (mAssetManager != null) {
            Intent mediaScannerIntent = null;
            for (String s : assets) {
                try {
                    InputStream inStream = mAssetManager.open(PATH.concat(File.separator).concat(s));

                    File destFolderFile = new File(mDestFolderPathFile.getAbsolutePath() + File.separator + DEST_PATH);

                    if (!destFolderFile.exists()) {
                        if (!destFolderFile.mkdirs()) {
                            return;
                        }
                    }

                    File destFile = new File(destFolderFile, s);
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
                            log(e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    // update media library
                    if (null == mediaScannerIntent) {
                        mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    }
                    mediaScannerIntent.setData(Uri.fromFile(destFile));
                    mContext.sendBroadcast(mediaScannerIntent);

                } catch (IOException e) {
                        log(e.getMessage());
                        e.printStackTrace();
                    }
            }
        }
    }

    public boolean isFirstRun() {
        return mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(PREF_FIRST_RUN_PARAM_NAME, true);
    }

    public void clearFirstRun() {
        mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(PREF_FIRST_RUN_PARAM_NAME, false).apply();
    }

}
