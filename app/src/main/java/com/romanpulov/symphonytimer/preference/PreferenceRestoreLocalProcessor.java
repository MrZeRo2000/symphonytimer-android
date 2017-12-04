package com.romanpulov.symphonytimer.preference;

import android.preference.PreferenceFragment;

import com.romanpulov.library.common.loader.core.Loader;
import com.romanpulov.symphonytimer.loader.local.RestoreLocalLoader;

/**
 * Created by romanpulov on 30.11.2017.
 */

public class PreferenceRestoreLocalProcessor implements PreferenceLoaderProcessor {

    private static final String PREF_KEY_NAME = PreferenceRepository.PREF_KEY_LOCAL_RESTORE;

    private final PreferenceFragment mPreferenceFragment;

    public PreferenceRestoreLocalProcessor(PreferenceFragment preferenceFragment) {
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
        } else {
            PreferenceRepository.displayMessage(mPreferenceFragment, result);
            PreferenceRepository.updatePreferenceKeySummary(mPreferenceFragment, PREF_KEY_NAME, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);
        }
    }

    @Override
    public Class<? extends Loader> getLoaderClass() {
        return RestoreLocalLoader.class;
    }
}
