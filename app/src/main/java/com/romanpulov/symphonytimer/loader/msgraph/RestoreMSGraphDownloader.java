package com.romanpulov.symphonytimer.loader.msgraph;

import android.content.Context;

import com.romanpulov.jutilscore.io.FileUtils;
import com.romanpulov.library.common.loader.core.LoadPathProvider;
import com.romanpulov.library.common.loader.file.FileLoader;
import com.romanpulov.library.msgraph.MSALGetBytesByPathAction;
import com.romanpulov.library.msgraph.MSActionExecutor;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.cloud.MSGraphHelper;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.loader.cloud.RestoreCloudLoadPathProvider;
import com.romanpulov.symphonytimer.loader.helper.LoaderNotificationHelper;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_LOADER;

public class RestoreMSGraphDownloader extends FileLoader  {
    public RestoreMSGraphDownloader(Context context) {
        super(context, new RestoreCloudLoadPathProvider(context));
    }

    @Override
    public void load() throws Exception {
        // get data from cloud
        byte[] bytes = MSActionExecutor.executeSync(
                new MSALGetBytesByPathAction(
                        mContext,
                        getLoadPathProvider().getSourcePath(),
                        null
                )
        );

        // copy file to dest
        File destFile = new File(getLoadPathProvider().getDestPath());
        try (
                InputStream inputStream = new ByteArrayInputStream(bytes);
                OutputStream outputStream = new FileOutputStream(destFile)
        ) {
            FileUtils.copyStream(inputStream, outputStream);
        }

        // perform restore
        boolean isRestoreSuccess = DBStorageHelper.getInstance(mContext).getDBBackupManager().restoreFromBackupPath(getLoadPathProvider().getDestPath());

        String restoreMessage = mContext.getString(isRestoreSuccess ? R.string.info_load_cloud_backup : R.string.error_restore);

        PreferenceRepository.setPreferenceKeyLastLoadedCurrentTime(mContext, PreferenceRepository.PREF_KEY_CLOUD_RESTORE);
        LoaderNotificationHelper.notify(mContext, restoreMessage, NOTIFICATION_ID_LOADER);
    }
}
