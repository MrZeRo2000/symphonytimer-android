package com.romanpulov.symphonytimer.cloud;

import com.romanpulov.symphonytimer.preference.PreferenceRepository;

public final class CloudAccountManagerFactory {
    public static AbstractCloudAccountManager fromCloudAccountType(int type) {
        switch (type) {
            case PreferenceRepository.PREF_CLOUD_ACCOUNT_TYPE_DROPBOX:
                return new DropboxAccountManager();
            case PreferenceRepository.PREF_CLOUD_ACCOUNT_TYPE_ONEDRIVE:
                return new OneDriveAccountManager();
            default:
                return null;
        }
    }
}
