package com.romanpulov.symphonytimer.loader.gdrive;

import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_LOADER;

import android.content.Context;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.loader.helper.LoaderNotificationHelper;

public class BackupGDriveUploader extends AbstractBackupGDriveUploader {
    public BackupGDriveUploader(Context context) {
        super(context);
    }

    @Override
    protected void beforeLoad() {}

    @Override
    protected void afterLoad() {
        LoaderNotificationHelper.notify(mContext, mContext.getString(R.string.notification_gdrive_backup_completed), NOTIFICATION_ID_LOADER);
    }
}
