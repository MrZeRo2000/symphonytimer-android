package com.romanpulov.symphonytimer.loader.dropbox;

import android.content.Context;

import com.romanpulov.library.common.loader.core.LoadPathProvider;
import com.romanpulov.library.common.loader.file.FileLoader;
import com.romanpulov.library.dropbox.DropboxHelper;
import com.romanpulov.symphonytimer.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Dropbox downloader class
 * Created by romanpulov on 27.11.2017.
 */

public abstract class DropboxFileDownloader extends FileLoader {

    private final DropboxHelper mDropboxHelper;

    public DropboxFileDownloader(Context context, LoadPathProvider loadPathProvider) {
        super(context, loadPathProvider);

        //dropbox
        mDropboxHelper = DropboxHelper.getInstance(context.getApplicationContext());
    }

    @Override
    public void load() throws Exception {
        String accessToken = mDropboxHelper.getAccessToken();
        if (accessToken == null)
            throw new Exception(mContext.getResources().getString(R.string.error_dropbox_auth));

        mDropboxHelper.initClient();

        File destFile = new File(getLoadPathProvider().getDestPath());

        try(OutputStream outputStream = new FileOutputStream(destFile)) {
            mDropboxHelper.getStream(outputStream, getLoadPathProvider().getSourcePath());
        } catch (DropboxHelper.DBHFileNotFoundException e) {
            throw new Exception(String.format(
                    mContext.getString(R.string.error_dropbox_load_file_data),
                    getLoadPathProvider().getSourcePath()
            ));
        }
    }
}
