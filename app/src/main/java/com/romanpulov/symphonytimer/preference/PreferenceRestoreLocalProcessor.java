package com.romanpulov.symphonytimer.preference;

import android.preference.PreferenceFragment;

import com.romanpulov.library.common.loader.core.Loader;
import com.romanpulov.symphonytimer.loader.local.RestoreLocalLoader;

/**
 * Created by romanpulov on 30.11.2017.
 */

public class PreferenceRestoreLocalProcessor implements PreferenceLoaderProcessor {

    private final PreferenceFragment mPreferenceFragment;

    public PreferenceRestoreLocalProcessor(PreferenceFragment preferenceFragment) {
        mPreferenceFragment = preferenceFragment;
    }

    @Override
    public void preExecute() {
        PreferenceRepository.updateLocalRestorePreferenceSummary(mPreferenceFragment, PreferenceRepository.PREF_LOAD_LOADING);
    }

    @Override
    public void postExecute(String result) {
        if (result == null) {
            long loadedTime = System.currentTimeMillis();
            PreferenceRepository.updateLocalRestorePreferenceSummary(mPreferenceFragment, loadedTime);
        } else {
            PreferenceRepository.displayMessage(mPreferenceFragment, result);
            PreferenceRepository.updateLocalRestorePreferenceSummary(mPreferenceFragment, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);
        }
    }

    @Override
    public Class<? extends Loader> getLoaderClass() {
        return RestoreLocalLoader.class;
    }
}
