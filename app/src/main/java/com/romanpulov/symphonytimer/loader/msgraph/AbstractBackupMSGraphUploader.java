package com.romanpulov.symphonytimer.loader.msgraph;

import android.content.Context;
import com.romanpulov.jutilscore.io.FileUtils;
import com.romanpulov.library.common.db.DBBackupManager;
import com.romanpulov.library.common.loader.core.AbstractContextLoader;
import com.romanpulov.library.msgraph.MSALPutBytesByPathAction;
import com.romanpulov.library.msgraph.MSActionExecutor;
import com.romanpulov.symphonytimer.helper.LoggerHelper;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.loader.cloud.CloudLoaderRepository;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public abstract class AbstractBackupMSGraphUploader extends AbstractContextLoader {
    private void log(String message) {
        LoggerHelper.logContext(mContext, AbstractBackupMSGraphUploader.class.getSimpleName(), message);
    }

    protected abstract void afterLoad();

    public AbstractBackupMSGraphUploader(Context context) {
        super(context);
    }

    @Override
    public void load() throws Exception {
        final DBBackupManager backupManager =  DBStorageHelper.getInstance(mContext).getDBBackupManager();

        final List<String> fileNames = backupManager.getDatabaseBackupFiles();

        log("Got backup files:" + fileNames);
        for (String fileName : fileNames) {
            log("Putting file:" + fileName);
            try (
                    InputStream inputStream = backupManager.createBackupInputStream(fileName);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
                    ) {
                FileUtils.copyStream(inputStream, outputStream);
                MSActionExecutor.executeSync(
                        new MSALPutBytesByPathAction(
                            mContext,
                            "/" + CloudLoaderRepository.REMOTE_PATH + "/" + fileName,
                            outputStream.toByteArray(),
                            null
                        )
                );
            }
        }
        log("Execution completed");

        PreferenceRepository.setPreferenceKeyLastLoadedCurrentTime(mContext, PreferenceRepository.PREF_KEY_CLOUD_BACKUP);

        afterLoad();
    }
}
