package com.romanpulov.symphonytimer.loader.onedrive;

import android.content.Context;

import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.extensions.Item;
import com.romanpulov.library.common.loader.core.AbstractContextLoader;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.LoggerHelper;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.library.onedrive.OneDriveHelper;
import com.romanpulov.symphonytimer.loader.cloud.CloudLoaderRepository;
import com.romanpulov.symphonytimer.loader.helper.LoaderNotificationHelper;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static com.romanpulov.symphonytimer.cloud.OneDriveAccountRepository.ONE_DRIVE_APP_ID;
import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_LOADER;

public class BackupOneDriveUploader extends AbstractContextLoader {
    private void log(String message) {
        LoggerHelper.logContext(mContext, "BackupOneDriveUploader", message);
    }

    private final OneDriveHelper mOneDriveHelper;
    private final DBStorageHelper mDBStorageHelper;
    private final CountDownLatch mLocker = new CountDownLatch(1);

    public BackupOneDriveUploader(Context context) {
        super(context);
        mOneDriveHelper = OneDriveHelper.getInstance(ONE_DRIVE_APP_ID);
        mDBStorageHelper = new DBStorageHelper(context);
    }

    @Override
    public void load() throws Exception {
        final File[] files = mDBStorageHelper.getDatabaseBackupFiles();

        log("Got backup files:" + Arrays.toString(files));

        final AtomicReference<Exception> mException = new AtomicReference<>();
        final AtomicReference<Integer> mFileCounter = new AtomicReference<>();
        mFileCounter.set(files.length);

        mOneDriveHelper.setOnOneDriveItemListener(new OneDriveHelper.OnOneDriveItemListener() {
            @Override
            public void onItemSuccess(Item item) {
                log("Item 1 success:" + item);
                mOneDriveHelper.setOnOneDriveItemListener(new OneDriveHelper.OnOneDriveItemListener() {
                    @Override
                    public void onItemSuccess(Item item) {
                        mFileCounter.set(mFileCounter.get() - 1);
                        if (mFileCounter.get() == 0) {
                            mLocker.countDown();
                        }
                    }

                    @Override
                    public void onItemFailure(ClientException ex) {
                        log("Putting file Exception:" + ex.getMessage());
                        mException.set(ex);
                        mLocker.countDown();
                    }
                });

                try {
                    for (File f : files) {
                        log("Putting file:" + f.getName());
                        mOneDriveHelper.putFile(f, CloudLoaderRepository.REMOTE_PATH);
                    }
                } catch (IOException e) {
                    log("Putting file IO exception:" + e.getMessage());
                    mException.set(e);
                    mLocker.countDown();
                }
            }

            @Override
            public void onItemFailure(ClientException ex) {
                log("Item 1 exception:" + ex.getMessage());
                mException.set(ex);
                mLocker.countDown();
            }
        });

        log("Creating folder");
        mOneDriveHelper.createFolder(CloudLoaderRepository.REMOTE_PATH);
        log("Waiting for folder creation");
        mLocker.await();

        if (mException.get() != null) {
            throw mException.get();
        }

        PreferenceRepository.setPreferenceKeyLastLoadedCurrentTime(mContext, PreferenceRepository.PREF_KEY_CLOUD_BACKUP);
        LoaderNotificationHelper.notify(mContext, mContext.getString(R.string.notification_onedrive_backup_completed), NOTIFICATION_ID_LOADER);

    }
}
