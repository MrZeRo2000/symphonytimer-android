package com.romanpulov.symphonytimer.preference;

import android.preference.PreferenceFragment;

import com.romanpulov.library.common.loader.core.Loader;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.NotificationHelper;
import com.romanpulov.symphonytimer.loader.dropbox.BackupDropboxUploader;
import com.romanpulov.symphonytimer.loader.helper.LoaderNotificationHelper;

import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_LOADER;

/**
 * Backup to dropbox processor
 * Created by romanpulov on 21.11.2017.
 */

public class PreferenceBackupDropboxProcessor implements PreferenceLoaderProcessor {

    private static final String PREF_KEY_NAME = PreferenceRepository.PREF_KEY_DROPBOX_BACKUP;

    private final PreferenceFragment mPreferenceFragment;

    public PreferenceBackupDropboxProcessor(PreferenceFragment preferenceFragment) {
        mPreferenceFragment = preferenceFragment;
    }

    @Override
    public void preExecute() {
        PreferenceRepository.updatePreferenceKeySummary(mPreferenceFragment, PREF_KEY_NAME, PreferenceRepository.PREF_LOAD_LOADING);
    }

    @Override
    public Class<? extends Loader> getLoaderClass() {
        return BackupDropboxUploader.class;
    }

    @Override
    public void postExecute(String result) {
        if (result == null) {
            LoaderNotificationHelper.notify(mPreferenceFragment.getActivity(), mPreferenceFragment.getActivity().getString(R.string.notification_dropbox_backup_completed), NOTIFICATION_ID_LOADER);

            long loadedTime = System.currentTimeMillis();
            PreferenceRepository.updatePreferenceKeySummary(mPreferenceFragment, PREF_KEY_NAME, loadedTime);
        } else {
            //PreferenceRepository.displayMessage(mPreferenceFragment, result);
            String errorNotificationError = mPreferenceFragment.getActivity().getString(R.string.notification_load_operation_error, result);
            LoaderNotificationHelper.notify(mPreferenceFragment.getActivity(), errorNotificationError, NOTIFICATION_ID_LOADER);

            PreferenceRepository.updatePreferenceKeySummary(mPreferenceFragment, PREF_KEY_NAME, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);
        }
    }
}
