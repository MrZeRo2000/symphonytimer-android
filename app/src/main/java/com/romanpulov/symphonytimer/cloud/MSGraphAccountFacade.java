package com.romanpulov.symphonytimer.cloud;

import android.app.Activity;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.romanpulov.library.common.account.AbstractCloudAccountManager;
import com.romanpulov.library.msgraph.OnMSActionListener;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.loader.msgraph.BackupMSGraphUploader;
import com.romanpulov.symphonytimer.loader.msgraph.RestoreMSGraphDownloader;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;

public class MSGraphAccountFacade extends AbstractCloudAccountFacade {

    @Override
    public void setupAccount(Activity activity) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);

        alert
                .setTitle(R.string.title_confirmation)
                .setPositiveButton(R.string.caption_login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MSGraphHelper.getInstance().login(
                                activity,
                                new OnMSActionListener<String>() {
                                    @Override
                                    public void onActionSuccess(int action, String data) {
                                        PreferenceRepository.displayMessage(activity, R.string.notification_onedrive_successfully_logged_in);
                                    }

                                    @Override
                                    public void onActionFailure(int action, String errorMessage) {
                                        PreferenceRepository.displayMessage(activity, R.string.error_onedrive_login, errorMessage);
                                    }
                                }
                        );
                    }
                })
                .setNegativeButton(R.string.caption_logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MSGraphHelper.getInstance().logout(
                                activity,
                                new OnMSActionListener<Void>() {
                                    @Override
                                    public void onActionSuccess(int action, Void data) {
                                        PreferenceRepository.displayMessage(activity, R.string.notification_onedrive_successfully_logged_out);
                                    }

                                    @Override
                                    public void onActionFailure(int action, String errorMessage) {
                                        PreferenceRepository.displayMessage(activity, R.string.error_onedrive_logout, errorMessage);
                                    }
                                }
                        );
                    }
                })
                .show();
    }

    @Override
    public AbstractCloudAccountManager<?> getAccountManager(Activity activity) {
        return new MSGraphCloudAccountManager(activity);
    }

    @Override
    public String getBackupLoaderClassName() {
        return BackupMSGraphUploader.class.getName();
    }

    @Override
    public String getRestoreLoaderClassName() {
        return RestoreMSGraphDownloader.class.getName();
    }
}
