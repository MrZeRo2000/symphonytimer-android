package com.romanpulov.symphonytimer.preference;

import androidx.preference.PreferenceFragmentCompat;

import com.romanpulov.library.common.loader.core.Loader;

/**
 * Created by romanpulov on 27.11.2017.
 */

public class PreferenceRestoreCloudProcessor implements PreferenceLoaderProcessor  {

    private static final String PREF_KEY_NAME = PreferenceRepository.PREF_KEY_CLOUD_RESTORE;

    private final PreferenceFragmentCompat mPreferenceFragment;

    public PreferenceRestoreCloudProcessor(PreferenceFragmentCompat preferenceFragment) {
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
        return null;
    }

}
