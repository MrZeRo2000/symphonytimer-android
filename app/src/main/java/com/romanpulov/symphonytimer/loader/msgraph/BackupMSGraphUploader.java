package com.romanpulov.symphonytimer.loader.msgraph;

import android.content.Context;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.loader.helper.LoaderNotificationHelper;

import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_LOADER;

public class BackupMSGraphUploader extends AbstractBackupMSGraphUploader {

    public BackupMSGraphUploader(Context context) {
        super(context);
    }

    @Override
    protected void afterLoad() {
        LoaderNotificationHelper.notify(mContext, mContext.getString(R.string.notification_onedrive_backup_completed), NOTIFICATION_ID_LOADER);
    }
}
