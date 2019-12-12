package com.romanpulov.symphonytimer.loader.onedrive;

import android.content.Context;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.loader.dropbox.RestoreCloudLoadPathProvider;
import com.romanpulov.symphonytimer.loader.helper.LoaderNotificationHelper;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;

import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_LOADER;


public class RestoreOneDriveDownloader extends com.romanpulov.symphonytimer.loader.onedrive.OneDriveFileLoader {

    public RestoreOneDriveDownloader(Context context) {
        super(context, new RestoreCloudLoadPathProvider(context));
    }

    @Override
    public void load() throws Exception {
        super.load();

        boolean isRestoreSuccess = DBStorageHelper.restoreFromBackupPath(mContext, getLoadPathProvider().getDestPath());

        String restoreMessage = mContext.getString(isRestoreSuccess ? R.string.info_load_cloud_backup : R.string.error_restore);

        PreferenceRepository.setPreferenceKeyLastLoadedCurrentTime(mContext, PreferenceRepository.PREF_KEY_CLOUD_RESTORE);
        LoaderNotificationHelper.notify(mContext, restoreMessage, NOTIFICATION_ID_LOADER);
    }
}
