package com.romanpulov.symphonytimer.loader.gdrive;

import android.content.Context;
import com.romanpulov.jutilscore.io.FileUtils;
import com.romanpulov.library.common.db.DBBackupManager;
import com.romanpulov.library.common.loader.core.AbstractContextLoader;
import com.romanpulov.library.gdrive.GDActionException;
import com.romanpulov.symphonytimer.cloud.GDHelper;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.loader.cloud.CloudLoaderRepository;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

public abstract class AbstractBackupGDriveUploader extends AbstractContextLoader {
    public AbstractBackupGDriveUploader(Context context) {
        super(context);
    }

    abstract protected void beforeLoad() throws GDActionException;

    abstract protected void afterLoad();

    @Override
    public void load() throws Exception {
        beforeLoad();

        final DBBackupManager backupManager =  DBStorageHelper.getInstance(mContext).getDBBackupManager();

        final List<String> fileNames = backupManager.getDatabaseBackupFiles();

        for (String fileName : fileNames) {
            try (
                    InputStream inputStream = backupManager.createBackupInputStream(fileName);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            ) {
                FileUtils.copyStream(inputStream, outputStream);
                GDHelper.getInstance().putBytesByPath(mContext, CloudLoaderRepository.REMOTE_PATH + File.separator + fileName, outputStream.toByteArray());
            }
        }

        PreferenceRepository.setPreferenceKeyLastLoadedCurrentTime(mContext, PreferenceRepository.PREF_KEY_CLOUD_BACKUP);

        afterLoad();
    }
}
