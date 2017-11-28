package com.romanpulov.symphonytimer.loader;

import android.content.Context;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.romanpulov.library.common.loader.core.LoadPathProvider;
import com.romanpulov.library.common.loader.file.FileLoader;
import com.romanpulov.library.dropbox.DropboxHelper;
import com.romanpulov.symphonytimer.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

        DbxClientV2 dbxClient = mDropboxHelper.getClient();

        Metadata m = dbxClient.files().getMetadata(getLoadPathProvider().getSourcePath());
        if ((m == null) || !(m instanceof FileMetadata))
            throw new Exception(String.format(mContext.getString(R.string.error_dropbox_load_file_data), getLoadPathProvider().getSourcePath()));

        FileMetadata fm = (FileMetadata) m;

        File mDestFile = new File(getLoadPathProvider().getDestPath());

        // delete existing file
        if (mDestFile.exists())
            mDestFile.delete();

        OutputStream outputStream = new FileOutputStream(mDestFile);
        try {
            dbxClient.files().download(fm.getPathLower(), fm.getRev()).download(outputStream);
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
