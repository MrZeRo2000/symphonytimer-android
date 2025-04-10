package com.romanpulov.symphonytimer.cloud;

import android.app.Activity;

import com.romanpulov.library.common.account.AbstractCloudAccountManager;
import org.jetbrains.annotations.NotNull;

public interface AbstractCloudAccountFacade {
    void setupAccount(Activity activity);
    @NotNull AbstractCloudAccountManager<?> getAccountManager(Activity activity);
    @NotNull String getBackupLoaderClassName();
    @NotNull String getSilentBackupLoaderClassName();
    @NotNull String getRestoreLoaderClassName();
}
