package com.romanpulov.symphonytimer.cloud;

import android.app.Activity;

import com.romanpulov.library.common.account.AbstractCloudAccountManager;

public abstract class AbstractCloudAccountFacade {
    public abstract void setupAccount(Activity activity);
    public abstract AbstractCloudAccountManager getAccountManager(Activity activity);
    public abstract String getBackupLoaderClassName();
    public abstract String getRestoreLoaderClassName();
}
