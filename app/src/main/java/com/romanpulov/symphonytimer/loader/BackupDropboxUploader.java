package com.romanpulov.symphonytimer.loader;

import android.content.Context;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import com.romanpulov.library.common.loader.core.AbstractContextLoader;
import com.romanpulov.library.dropbox.DropboxHelper;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Loader to backup to Dropbox
 * Created by romanpulov on 22.11.2017.
 */

public class BackupDropboxUploader extends AbstractContextLoader {

    private final DropboxHelper mDropboxHelper;
    private final DBStorageHelper mDBStorageHelper;

    public BackupDropboxUploader(Context context) {
        super(context);
        mDropboxHelper = DropboxHelper.getInstance(context.getApplicationContext());
        mDBStorageHelper = new DBStorageHelper(context);
    }

    @Override
    public void load() throws Exception {
        String accessToken = mDropboxHelper.getAccessToken();
        if (accessToken == null)
            throw new Exception(mContext.getResources().getString(R.string.error_dropbox_auth));

        DbxClientV2 client = mDropboxHelper.getClient();

        File[] files = mDBStorageHelper.getXMLBackupFiles();
        for (File f : files) {
            String remoteFileName = f.getName();
            InputStream inputStream = new FileInputStream(f);
            try {
                client.files().uploadBuilder(DropboxLoaderRepository.REMOTE_PATH + remoteFileName).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        PreferenceRepository.setDropboxBackupLastLoadedCurrentTime(mContext);
    }
}
