package com.romanpulov.symphonytimer.cloud;

import android.app.Activity;

import com.romanpulov.library.common.account.AbstractCloudAccountManager;
import com.romanpulov.library.msgraph.OnMSActionListener;

public class MSGraphCloudAccountManager extends AbstractCloudAccountManager<MSGraphHelper> {

    public MSGraphCloudAccountManager(Activity activity) {
        super(activity);
    }

    @Override
    protected MSGraphHelper createAccountHolder() {
        return MSGraphHelper.getInstance();
    }

    @Override
    protected void internalSetupAccount() {
        mAccountHolder.load(
                mActivity,
                new OnMSActionListener<Void>() {
                    @Override
                    public void onActionSuccess(int action, Void data) {
                        mAccountSetupListener.onAccountSetupSuccess();
                    }

                    @Override
                    public void onActionFailure(int action, String errorMessage) {
                        mAccountSetupListener.onAccountSetupFailure(errorMessage);
                    }
                }
        );
    }
}
