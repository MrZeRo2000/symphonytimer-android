package com.romanpulov.symphonytimer.loader.cloud;

import android.content.Context;

import com.romanpulov.library.common.loader.core.ContextLoadPathProvider;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.loader.dropbox.DropboxLoaderRepository;

import java.io.File;

/**
 * Created by romanpulov on 27.11.2017.
 */

public class RestoreCloudLoadPathProvider extends ContextLoadPathProvider {

    public RestoreCloudLoadPathProvider(Context context) {
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
