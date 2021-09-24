package com.romanpulov.symphonytimer.cloud;

import android.app.Activity;

import com.romanpulov.library.common.account.AbstractCloudAccountManager;
import com.romanpulov.library.gdrive.OnGDActionListener;

public class GDCloudAccountManager extends AbstractCloudAccountManager<GDHelper> {

    public GDCloudAccountManager(Activity activity) {
        super(activity);
    }

    @Override
    protected GDHelper createAccountHolder() {
        return GDHelper.getInstance();
    }

    @Override
    protected void internalSetupAccount() {
        mAccountHolder.load(mActivity, new OnGDActionListener<Void>() {
            @Override
            public void onActionSuccess(Void unused) {
                mAccountSetupListener.onAccountSetupSuccess();
            }

            @Override
            public void onActionFailure(Exception e) {
                mAccountSetupListener.onAccountSetupFailure(e.getMessage());
            }
        });
    }
}
