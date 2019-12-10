package com.romanpulov.symphonytimer.preference;

import androidx.preference.PreferenceFragmentCompat;

import com.romanpulov.library.common.loader.core.Loader;
import com.romanpulov.symphonytimer.loader.dropbox.BackupDropboxUploader;

/**
 * Backup to dropbox processor
 * Created by romanpulov on 21.11.2017.
 */

public class PreferenceBackupDropboxProcessor implements PreferenceLoaderProcessor {

    private static final String PREF_KEY_NAME = PreferenceRepository.PREF_KEY_DROPBOX_BACKUP;

    private final PreferenceFragmentCompat mPreferenceFragment;

    public PreferenceBackupDropboxProcessor(PreferenceFragmentCompat preferenceFragment) {
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
            long loadedTime = System.currentTimeMillis();
            PreferenceRepository.updatePreferenceKeySummary(mPreferenceFragment, PREF_KEY_NAME, loadedTime);
        } else
            PreferenceRepository.updatePreferenceKeySummary(mPreferenceFragment, PREF_KEY_NAME, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);
    }
}
