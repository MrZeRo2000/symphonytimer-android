package com.romanpulov.symphonytimer.preference;

import android.preference.PreferenceFragment;

import com.romanpulov.library.common.loader.core.Loader;
import com.romanpulov.symphonytimer.loader.BackupDropboxUploader;

/**
 * Backup to dropbox processor
 * Created by romanpulov on 21.11.2017.
 */

public class PreferenceBackupDropboxProcessor implements PreferenceLoaderProcessor {

    private final PreferenceFragment mPreferenceFragment;

    public PreferenceBackupDropboxProcessor(PreferenceFragment preferenceFragment) {
        mPreferenceFragment = preferenceFragment;
    }

    @Override
    public void preExecute() {
        PreferenceRepository.updateDropboxBackupPreferenceSummary(mPreferenceFragment, PreferenceRepository.PREF_LOAD_LOADING);
    }

    @Override
    public Class<? extends Loader> getLoaderClass() {
        return BackupDropboxUploader.class;
    }

    @Override
    public void postExecute(String result) {
        if (result == null) {
            long loadedTime = System.currentTimeMillis();
            PreferenceRepository.updateDropboxBackupPreferenceSummary(mPreferenceFragment, loadedTime);
        } else {
            PreferenceRepository.displayMessage(mPreferenceFragment, result);
            PreferenceRepository.updateDropboxBackupPreferenceSummary(mPreferenceFragment, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);
        }
    }
}
