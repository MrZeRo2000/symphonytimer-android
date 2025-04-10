package com.romanpulov.symphonytimer.loader.gdrive;

import android.content.Context;
import com.romanpulov.library.gdrive.GDActionException;
import com.romanpulov.symphonytimer.cloud.GDHelper;

public class SilentBackupGDriveUploader extends AbstractBackupGDriveUploader {
    public SilentBackupGDriveUploader(Context context) {
        super(context);
    }

    @Override
    protected void beforeLoad() throws GDActionException {
        GDHelper.getInstance().silentLogin(mContext);
    }

    @Override
    protected void afterLoad() {}
}
