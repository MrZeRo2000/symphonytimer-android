package com.romanpulov.symphonytimer.cloud;

import android.app.Activity;

import com.romanpulov.library.common.account.AbstractCloudAccountManager;
import com.romanpulov.library.dropbox.DropboxHelper;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.loader.dropbox.BackupDropboxUploader;

public class DropboxAccountFacade extends AbstractCloudAccountFacade {
    @Override
    public void setupAccount(Activity activity) {
        DropboxHelper.getInstance(activity.getApplicationContext()).invokeAuthActivity(activity.getResources().getString(R.string.app_key));
    }

    @Override
    public AbstractCloudAccountManager getAccountManager(Activity activity) {
        return new DropboxCloudAccountManager(activity);
    }

    @Override
    public String getBackupLoaderClassName() {
        return BackupDropboxUploader.class.getName();
    }
}
