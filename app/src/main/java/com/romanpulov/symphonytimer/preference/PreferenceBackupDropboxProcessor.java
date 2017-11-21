package com.romanpulov.symphonytimer.preference;

import android.preference.PreferenceFragment;

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
