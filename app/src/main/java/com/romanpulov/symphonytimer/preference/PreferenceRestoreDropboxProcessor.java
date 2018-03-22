package com.romanpulov.symphonytimer.preference;

import android.preference.PreferenceFragment;

import com.romanpulov.library.common.loader.core.Loader;
import com.romanpulov.library.common.loader.file.FileLoader;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.loader.dropbox.RestoreDropboxFileDownloader;
import com.romanpulov.symphonytimer.loader.helper.LoaderNotificationHelper;

import java.io.File;

import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_LOADER;

/**
 * Created by romanpulov on 27.11.2017.
 */

public class PreferenceRestoreDropboxProcessor implements PreferenceLoaderProcessor  {

    private static final String PREF_KEY_NAME = PreferenceRepository.PREF_KEY_DROPBOX_RESTORE;

    private final PreferenceFragment mPreferenceFragment;

    public PreferenceRestoreDropboxProcessor(PreferenceFragment preferenceFragment) {
        mPreferenceFragment = preferenceFragment;
    }

    @Override
    public void preExecute() {
        PreferenceRepository.updatePreferenceKeySummary(mPreferenceFragment, PREF_KEY_NAME, PreferenceRepository.PREF_LOAD_LOADING);
    }

    @Override
    public void postExecute(String result) {
        if (result == null) {
            long loadedTime = System.currentTimeMillis();
            PreferenceRepository.updatePreferenceKeySummary(mPreferenceFragment, PREF_KEY_NAME, loadedTime);
        }
        else
            PreferenceRepository.updatePreferenceKeySummary(mPreferenceFragment, PREF_KEY_NAME, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);
    }

    @Override
    public Class<? extends Loader> getLoaderClass() {
        return RestoreDropboxFileDownloader.class;
    }

}
