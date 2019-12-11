package com.romanpulov.symphonytimer.cloud;

import android.app.Activity;

import com.romanpulov.library.common.account.AbstractCloudAccountManager;

import static com.romanpulov.symphonytimer.preference.PreferenceRepository.PREF_CLOUD_ACCOUNT_TYPE_DROPBOX;
import static com.romanpulov.symphonytimer.preference.PreferenceRepository.PREF_CLOUD_ACCOUNT_TYPE_ONEDRIVE;

public class CloudAccountManagerFactory {

    public static AbstractCloudAccountManager fromCloudSourceType(Activity activity, int type) {
        switch (type) {
            case PREF_CLOUD_ACCOUNT_TYPE_DROPBOX:
                return new DropboxCloudAccountManager(activity);
            case PREF_CLOUD_ACCOUNT_TYPE_ONEDRIVE:
                return new OneDriveCloudAccountManager(activity);
            default:
                return null;
        }
    }

}
