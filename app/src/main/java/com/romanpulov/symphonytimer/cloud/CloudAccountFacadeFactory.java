package com.romanpulov.symphonytimer.cloud;

import com.romanpulov.symphonytimer.preference.PreferenceRepository;

public final class CloudAccountFacadeFactory {
    public static AbstractCloudAccountFacade fromCloudAccountType(int type) {
        switch (type) {
            case PreferenceRepository.PREF_CLOUD_ACCOUNT_TYPE_DROPBOX:
                return new DropboxAccountFacade();
            case PreferenceRepository.PREF_CLOUD_ACCOUNT_TYPE_ONEDRIVE:
                return new OneDriveAccountFacade();
            default:
                return null;
        }
    }
}
