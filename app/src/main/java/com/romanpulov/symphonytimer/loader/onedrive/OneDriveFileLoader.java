package com.romanpulov.symphonytimer.loader.onedrive;

import android.content.Context;

import com.romanpulov.jutilscore.io.FileUtils;
import com.romanpulov.library.common.loader.core.LoadPathProvider;
import com.romanpulov.library.common.loader.file.FileLoader;
import com.romanpulov.library.onedrive.OneDriveHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class OneDriveFileLoader extends FileLoader {
    private final OneDriveHelper mOneDriveHelper;

    @Override
    public void load() throws Exception {
        File mDestFile = new File(getLoadPathProvider().getDestPath());
        try (
                InputStream inputStream = new BufferedInputStream(mOneDriveHelper.getInputStreamByPath(getLoadPathProvider().getSourcePath()));
                OutputStream outputStream = new FileOutputStream(mDestFile);
                ) {
            FileUtils.copyStream(inputStream, outputStream);
        }
    }

    public OneDriveFileLoader(Context context, LoadPathProvider loadPathProvider) {
        super(context, loadPathProvider);
        mOneDriveHelper = OneDriveHelper.getInstance();
    }
}
