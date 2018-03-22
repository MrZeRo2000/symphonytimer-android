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

public class RestoreDropboxFileDownloader extends DropboxFileDownloader {

    private static final String PREF_KEY_NAME = PreferenceRepository.PREF_KEY_DROPBOX_BACKUP;

    public RestoreDropboxFileDownloader(Context context) {
        super(context, new RestoreDropboxLoadPathProvider(context));
    }

    @Override
    public void load() throws Exception {
        super.load();
        File file = new File(getLoadPathProvider().getDestPath());

        if (file.exists()) {

            DBStorageHelper dbStorageHelper = new DBStorageHelper(mContext);

            int restoreResult = dbStorageHelper.restoreLocalXmlBackup();

            String restoreMessage;

            if (restoreResult != 0) {
                restoreMessage = String.format(mContext.getString(R.string.error_load_local_backup), restoreResult);
                //PreferenceRepository.updatePreferenceKeySummary(mContext, PREF_KEY_NAME, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);
            }
            else {
                restoreMessage = mContext.getString(R.string.info_load_local_backup);
                long loadedTime = System.currentTimeMillis();
                //PreferenceRepository.updatePreferenceKeySummary(mContext, PREF_KEY_NAME, loadedTime);
            }

            LoaderNotificationHelper.notify(mContext, restoreMessage, NOTIFICATION_ID_LOADER);
            //PreferenceRepository.displayMessage(mPreferenceFragment, restoreMessage);

        } else {
            //PreferenceRepository.updatePreferenceKeySummary(mContext, PREF_KEY_NAME, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);
            LoaderNotificationHelper.notify(mContext, mContext.getString(R.string.error_restore), NOTIFICATION_ID_LOADER);
            //PreferenceRepository.displayMessage(mPreferenceFragment, mPreferenceFragment.getString(R.string.error_restore));
        }


        PreferenceRepository.setPreferenceKeyLastLoadedCurrentTime(mContext, PreferenceRepository.PREF_KEY_DROPBOX_RESTORE);
    }
}
