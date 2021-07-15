package com.romanpulov.symphonytimer.cloud;

import android.app.Activity;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.romanpulov.library.common.account.AbstractCloudAccountManager;
import com.romanpulov.library.onedrive.OneDriveHelper;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.loader.onedrive.BackupOneDriveUploader;
import com.romanpulov.symphonytimer.loader.onedrive.RestoreOneDriveDownloader;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;

import static com.romanpulov.library.onedrive.OneDriveHelper.ONEDRIVE_ACTION_LOGIN;
import static com.romanpulov.library.onedrive.OneDriveHelper.ONEDRIVE_ACTION_LOGOUT;

public class OneDriveAccountFacade extends AbstractCloudAccountFacade {
    @Override
    public String getRestoreLoaderClassName() {
        return RestoreOneDriveDownloader.class.getName();
    }

    private static class OneDriveActionHandler implements OneDriveHelper.OnOneDriveActionListener {
        @Override
        public void onActionCompleted(int action, boolean result, String message) {
            int successMessageId;
            int failureMessageId;
            int displayMessageId;

            switch(action) {
                case ONEDRIVE_ACTION_LOGIN:
                    successMessageId = R.string.notification_onedrive_successfully_logged_in;
                    failureMessageId = R.string.error_onedrive_login;
                    break;
                case ONEDRIVE_ACTION_LOGOUT:
                    successMessageId = R.string.notification_onedrive_successfully_logged_out;
                    failureMessageId = R.string.error_onedrive_logout;
                    break;
                default:
                    return;
            }

            if (result) {
                displayMessageId = successMessageId;
            } else {
                displayMessageId = failureMessageId;
            }

            PreferenceRepository.displayMessage(mActivity, mActivity.getString(displayMessageId));
        }

        private final Activity mActivity;

        private OneDriveActionHandler(Activity activity) {
            mActivity = activity;
        }
    }

    /*
    private OneDriveHelper.OnOneDriveActionListener onOneDriveActionListener = new OneDriveHelper.OnOneDriveActionListener() {
        @Override
        public void onActionCompleted(int action, boolean result, String message) {
            int successMessageId;
            int failureMessageId;
            int displayMessageId;

            switch(action) {
                case ONEDRIVE_ACTION_LOGIN:
                    successMessageId = R.string.notification_onedrive_successfully_logged_in;
                    failureMessageId = R.string.error_onedrive_login;
                    break;
                case ONEDRIVE_ACTION_LOGOUT:
                    successMessageId = R.string.notification_onedrive_successfully_logged_out;
                    failureMessageId = R.string.error_onedrive_logout;
                    break;
                default:
                    return;
            }

            if (result) {
                displayMessageId = successMessageId;
            } else {
                displayMessageId = failureMessageId;
            }

            PreferenceRepository.displayMessage(mContext, mContext.getString(displayMessageId));
        }
    };

     */

    @Override
    public void setupAccount(final Activity activity) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        final OneDriveActionHandler oneDriveActionHandler = new OneDriveActionHandler(activity);
        alert
                .setTitle(R.string.title_confirmation)
                .setPositiveButton(R.string.caption_login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OneDriveHelper oneDriveHelper = OneDriveHelper.getInstance();
                        oneDriveHelper.setOnOneDriveActionListener(oneDriveActionHandler);
                        oneDriveHelper.createClient(activity);
                    }
                })

                .setNegativeButton(R.string.caption_logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OneDriveHelper oneDriveHelper = OneDriveHelper.getInstance();
                        oneDriveHelper.setOnOneDriveActionListener(oneDriveActionHandler);
                        oneDriveHelper.logout(activity);
                    }
                })
                .show();

    }

    @Override
    public AbstractCloudAccountManager<?> getAccountManager(Activity activity) {
        return new OneDriveCloudAccountManager(activity);
    }

    @Override
    public String getBackupLoaderClassName() {
        return BackupOneDriveUploader.class.getName();
    }
}
