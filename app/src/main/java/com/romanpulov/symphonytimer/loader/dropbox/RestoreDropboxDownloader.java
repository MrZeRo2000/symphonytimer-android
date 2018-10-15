package com.romanpulov.symphonytimer.loader.dropbox;

import android.content.Context;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.loader.helper.LoaderNotificationHelper;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;

import java.io.File;

import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_LOADER;

/**
 * Loader to restore from Dropbox
 * Created by romanpulov on 27.11.2017.
 */

public class RestoreDropboxDownloader extends DropboxFileDownloader {

    public RestoreDropboxDownloader(Context context) {
        super(context, new RestoreDropboxLoadPathProvider(context));
    }

    @Override
    public void load() throws Exception {
        super.load();
        File file = new File(getLoadPathProvider().getDestPath());

        if (file.exists()) {
            DBStorageHelper dbStorageHelper = new DBStorageHelper(mContext);
            String restoreResult = dbStorageHelper.restoreLocalBackup();

            String restoreMessage;
            if (restoreResult == null)
                restoreMessage = mContext.getString(R.string.error_load_local_backup);
            else
                restoreMessage = mContext.getString(R.string.info_load_local_backup);

            LoaderNotificationHelper.notify(mContext, restoreMessage, NOTIFICATION_ID_LOADER);
        } else
            LoaderNotificationHelper.notify(mContext, mContext.getString(R.string.error_restore), NOTIFICATION_ID_LOADER);

        PreferenceRepository.setPreferenceKeyLastLoadedCurrentTime(mContext, PreferenceRepository.PREF_KEY_DROPBOX_RESTORE);
    }
}
