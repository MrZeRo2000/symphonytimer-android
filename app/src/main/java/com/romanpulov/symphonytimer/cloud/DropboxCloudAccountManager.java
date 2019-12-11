package com.romanpulov.symphonytimer.cloud;

import android.app.Activity;

import com.romanpulov.library.dropbox.DropboxHelper;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.library.common.account.AbstractCloudAccountManager;

public class DropboxCloudAccountManager extends AbstractCloudAccountManager<DropboxHelper> {
    @Override
    protected DropboxHelper createAccountHolder() {
        return DropboxHelper.getInstance(mActivity.getApplicationContext());
    }

    @Override
    protected void internalSetupAccount() {
        String accessToken = mAccountHolder.getAccessToken();
        if (accessToken == null) {
            mAccountSetupListener.onAccountSetupFailure(mContext.getResources().getString(R.string.error_dropbox_auth));
        } else {
            mAccountSetupListener.onAccountSetupSuccess();
        }
    }

    public DropboxCloudAccountManager(Activity activity) {
        super(activity);
    }
}
