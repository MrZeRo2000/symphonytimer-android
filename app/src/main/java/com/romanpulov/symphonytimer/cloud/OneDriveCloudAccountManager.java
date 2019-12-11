package com.romanpulov.symphonytimer.cloud;

import android.app.Activity;

import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.extensions.Item;
import com.romanpulov.library.onedrive.OneDriveHelper;
import com.romanpulov.library.common.account.AbstractCloudAccountManager;

public class OneDriveCloudAccountManager extends AbstractCloudAccountManager<OneDriveHelper> {

    @Override
    protected OneDriveHelper createAccountHolder() {
        return OneDriveHelper.getInstance();
    }

    @Override
    protected void internalSetupAccount() {
        mAccountHolder.setOnOneDriveActionListener(new OneDriveHelper.OnOneDriveActionListener() {
            @Override
            public void onActionCompleted(int action, boolean result, String message) {
                if (result) {
                    mAccountSetupListener.onAccountSetupSuccess();
                } else {
                    mAccountSetupListener.onAccountSetupFailure(message);
                }
            }
        });
        mAccountHolder.createClient(mActivity);
    }

    @Override
    public void setupItemId(String path) {
        mAccountHolder.setOnOneDriveItemListener(new OneDriveHelper.OnOneDriveItemListener() {
            @Override
            public void onItemSuccess(Item item) {
                if (mAccountSetupItemListener != null) {
                    mAccountSetupItemListener.onSetupItemSuccess(item.id);
                }
            }

            @Override
            public void onItemFailure(ClientException ex) {
                if (mAccountSetupItemListener != null) {
                    mAccountSetupItemListener.onSetupItemFailure(ex.getMessage());
                }

            }
        });
        String listPath = path.startsWith("root:") ? path : "root:/" + path;
        mAccountHolder.listItems(mActivity, listPath);
    }


    public OneDriveCloudAccountManager(Activity activity) {
        super(activity);
    }
}
