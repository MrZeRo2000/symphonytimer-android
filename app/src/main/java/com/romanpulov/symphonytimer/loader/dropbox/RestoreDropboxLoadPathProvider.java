package com.romanpulov.symphonytimer.loader.dropbox;

import android.content.Context;

import com.romanpulov.library.common.loader.core.ContextLoadPathProvider;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;

import java.io.File;

/**
 * Created by romanpulov on 27.11.2017.
 */

public class RestoreDropboxLoadPathProvider extends ContextLoadPathProvider {

    public RestoreDropboxLoadPathProvider(Context context) {
        super(context);
    }

    @Override
    public String getSourcePath() {
        return DropboxLoaderRepository.REMOTE_PATH + DBStorageHelper.getBackupZipFileName();
    }

    @Override
    public String getDestPath() {
        return getContext().getCacheDir() + File.separator + DBStorageHelper.getBackupZipFileName();
    }
}
