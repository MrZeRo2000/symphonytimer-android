package com.romanpulov.symphonytimer.preference;

import android.preference.PreferenceFragment;

import com.romanpulov.library.common.loader.core.Loader;
import com.romanpulov.library.common.loader.file.FileLoader;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.loader.RestoreDropboxFileDownloader;

import java.io.File;

/**
 * Created by romanpulov on 27.11.2017.
 */

public class PreferenceRestoreDropboxProcessor implements PreferenceLoaderProcessor  {

    private final PreferenceFragment mPreferenceFragment;

    public PreferenceRestoreDropboxProcessor(PreferenceFragment preferenceFragment) {
        mPreferenceFragment = preferenceFragment;
    }

    @Override
    public void preExecute() {
        PreferenceRepository.updateDropboxRestorePreferenceSummary(mPreferenceFragment, PreferenceRepository.PREF_LOAD_LOADING);
    }

    @Override
    public void postExecute(String result) {
        PreferenceRepository.setDropboxRestoreDefaultPreferenceSummary(mPreferenceFragment);

        if (result != null)
            PreferenceRepository.displayMessage(mPreferenceFragment, result);
        else {
            FileLoader fileLoader = new RestoreDropboxFileDownloader(mPreferenceFragment.getActivity());;
            File file = new File(fileLoader.getLoadPathProvider().getDestPath());
            if (file.exists()) {

                DBStorageHelper dbStorageHelper = new DBStorageHelper(mPreferenceFragment.getActivity());

                int restoreResult = dbStorageHelper.restoreLocalXmlBackup();

                String restoreMessage;

                if (restoreResult != 0)
                    restoreMessage = String.format(mPreferenceFragment.getString(R.string.error_load_local_backup), restoreResult);
                else
                    restoreMessage = mPreferenceFragment.getString(R.string.info_load_local_backup);

                PreferenceRepository.displayMessage(mPreferenceFragment, restoreMessage);

            } else
                PreferenceRepository.displayMessage(mPreferenceFragment, mPreferenceFragment.getString(R.string.error_restore));
        }
    }

    @Override
    public Class<? extends Loader> getLoaderClass() {
        return RestoreDropboxFileDownloader.class;
    }
}
