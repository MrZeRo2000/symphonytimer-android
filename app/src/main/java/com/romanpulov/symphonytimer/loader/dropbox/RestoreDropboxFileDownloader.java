package com.romanpulov.symphonytimer.loader.dropbox;

import android.content.Context;

/**
 * Loader to restore from Dropbox
 * Created by romanpulov on 27.11.2017.
 */

public class RestoreDropboxFileDownloader extends DropboxFileDownloader {

    public RestoreDropboxFileDownloader(Context context) {
        super(context, new RestoreDropboxLoadPathProvider(context));
    }
}
