package com.romanpulov.symphonytimer.cloud;

import android.app.Activity;

import com.romanpulov.library.dropbox.DropboxHelper;
import com.romanpulov.symphonytimer.R;

public class DropboxAccountManager extends AbstractCloudAccountManager {
    @Override
    public void setupAccount(Activity activity) {
        DropboxHelper.getInstance(activity.getApplicationContext()).invokeAuthActivity(activity.getResources().getString(R.string.app_key));
    }
}
