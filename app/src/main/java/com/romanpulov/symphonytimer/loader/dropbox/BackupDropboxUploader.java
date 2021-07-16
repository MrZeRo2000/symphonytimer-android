package com.romanpulov.symphonytimer.loader.dropbox;

import android.content.Context;

import com.romanpulov.library.common.db.DBBackupManager;
import com.romanpulov.library.common.loader.core.AbstractContextLoader;
import com.romanpulov.library.dropbox.DropboxHelper;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.loader.cloud.CloudLoaderRepository;
import com.romanpulov.symphonytimer.loader.helper.LoaderNotificationHelper;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;
import java.io.InputStream;

import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_LOADER;

/**
 * Loader to backup to Dropbox
 * Created by romanpulov on 22.11.2017.
 */

public class BackupDropboxUploader extends AbstractContextLoader {

    private final DropboxHelper mDropboxHelper;

    public BackupDropboxUploader(Context context) {
        super(context);
        mDropboxHelper = DropboxHelper.getInstance(context.getApplicationContext());
    }

    @Override
    public void load() throws Exception {
        String accessToken = mDropboxHelper.getAccessToken();
        if (accessToken == null)
            throw new Exception(mContext.getResources().getString(R.string.error_dropbox_auth));

        mDropboxHelper.initClient();

        final DBBackupManager backupManager = DBStorageHelper.getInstance(mContext).getDBBackupManager();

        for (String backupFileName: backupManager.getDatabaseBackupFiles()) {
            try (InputStream inputStream = backupManager.createBackupInputStream(backupFileName)) {
                mDropboxHelper.putStream(inputStream, "/" + CloudLoaderRepository.REMOTE_PATH + "/" + backupFileName);
            }
        }

        PreferenceRepository.setPreferenceKeyLastLoadedCurrentTime(mContext, PreferenceRepository.PREF_KEY_CLOUD_BACKUP);
        LoaderNotificationHelper.notify(mContext, mContext.getString(R.string.notification_dropbox_backup_completed), NOTIFICATION_ID_LOADER);
    }
}
