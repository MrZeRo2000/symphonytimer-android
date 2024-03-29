package com.romanpulov.symphonytimer.cloud;

import android.app.Activity;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.romanpulov.library.common.account.AbstractCloudAccountManager;
import com.romanpulov.library.gdrive.OnGDActionListener;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.loader.gdrive.BackupGDriveUploader;
import com.romanpulov.symphonytimer.loader.gdrive.RestoreGDriveDownloader;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;

public class GDAccountFacade extends AbstractCloudAccountFacade {
    @Override
    public void setupAccount(Activity activity) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);

        alert
                .setTitle(R.string.title_confirmation)
                .setPositiveButton(R.string.caption_login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GDHelper.getInstance().login(
                                activity, new OnGDActionListener<Void>() {
                                    @Override
                                    public void onActionSuccess(Void unused) {

                                    }

                                    @Override
                                    public void onActionFailure(Exception e) {

                                    }
                                }
                        );
                    }
                })
                .setNegativeButton(R.string.caption_logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GDHelper.getInstance().logout(
                                activity, new OnGDActionListener<Void>() {
                                    @Override
                                    public void onActionSuccess(Void unused) {
                                        PreferenceRepository.displayMessage(activity, R.string.notification_gdrive_successfully_logged_out);
                                    }

                                    @Override
                                    public void onActionFailure(Exception e) {
                                        PreferenceRepository.displayMessage(activity, R.string.error_gdrive_logout, e.getMessage());
                                    }
                                }
                        );
                    }
                })
                .show();
    }

    @Override
    public AbstractCloudAccountManager<?> getAccountManager(Activity activity) {
        return new GDCloudAccountManager(activity);
    }

    @Override
    public String getBackupLoaderClassName() {
        return BackupGDriveUploader.class.getName();
    }

    @Override
    public String getRestoreLoaderClassName() {
        return RestoreGDriveDownloader.class.getName();
    }
}
