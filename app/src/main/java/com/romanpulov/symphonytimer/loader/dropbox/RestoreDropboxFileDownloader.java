package com.romanpulov.symphonytimer.loader.dropbox;

import android.content.Context;

import com.romanpulov.symphonytimer.preference.PreferenceRepository;

/**
 * Loader to restore from Dropbox
 * Created by romanpulov on 27.11.2017.
 */

public class RestoreDropboxFileDownloader extends DropboxFileDownloader {

    public RestoreDropboxFileDownloader(Context context) {
        super(context, new RestoreDropboxLoadPathProvider(context));
    }

    @Override
    public void load() throws Exception {
        super.load();
        PreferenceRepository.setDropboxRestoreLastLoadedCurrentTime(mContext);
    }
}
