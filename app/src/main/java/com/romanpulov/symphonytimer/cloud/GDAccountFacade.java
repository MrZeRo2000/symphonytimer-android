package com.romanpulov.symphonytimer.cloud;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import com.romanpulov.library.common.account.AbstractCloudAccountManager;
import com.romanpulov.library.gdrive.OnGDActionListener;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.loader.gdrive.BackupGDriveUploader;
import com.romanpulov.symphonytimer.loader.gdrive.RestoreGDriveDownloader;
import com.romanpulov.symphonytimer.loader.gdrive.SilentBackupGDriveUploader;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;
import org.jetbrains.annotations.NotNull;

public class GDAccountFacade implements AbstractCloudAccountFacade {
    @Override
    public void setupAccount(Activity activity) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);

        alert
                .setTitle(R.string.title_confirmation)
                .setPositiveButton(R.string.caption_login, (dialog, which) -> GDHelper.getInstance().login(
                        new OnGDActionListener<>() {
                            @Override
                            public void onActionSuccess(Void unused) {
                                PreferenceRepository.displayMessage(activity, R.string.notification_gdrive_successfully_logged_in);
                            }

                            @Override
                            public void onActionFailure(Exception e) {
                                PreferenceRepository.displayMessage(activity, R.string.error_gdrive_login, e.getMessage());
                            }
                        }
                ))
                .setNegativeButton(R.string.caption_logout, (dialog, which) -> GDHelper.getInstance().logout(
                        activity, new OnGDActionListener<>() {
                            @Override
                            public void onActionSuccess(Void unused) {
                                PreferenceRepository.displayMessage(activity, R.string.notification_gdrive_successfully_logged_out);
                            }

                            @Override
                            public void onActionFailure(Exception e) {
                                PreferenceRepository.displayMessage(activity, R.string.error_gdrive_logout, e.getMessage());
                            }
                        }
                ))
                .show();
    }

    @Override
    public @NotNull AbstractCloudAccountManager<?> getAccountManager(Activity activity) {
        return new GDCloudAccountManager(activity);
    }

    @Override
    public @NotNull String getBackupLoaderClassName() {
        return BackupGDriveUploader.class.getName();
    }

    @Override
    public @NotNull String getSilentBackupLoaderClassName() {
        return SilentBackupGDriveUploader.class.getName();
    }

    @Override
    public @NotNull String getRestoreLoaderClassName() {
        return RestoreGDriveDownloader.class.getName();
    }
}
