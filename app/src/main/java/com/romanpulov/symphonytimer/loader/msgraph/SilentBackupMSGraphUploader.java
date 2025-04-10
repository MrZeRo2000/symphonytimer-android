package com.romanpulov.symphonytimer.loader.msgraph;

import android.content.Context;

public class SilentBackupMSGraphUploader extends AbstractBackupMSGraphUploader {

    public SilentBackupMSGraphUploader(Context context) {
        super(context);
    }

    @Override
    protected void afterLoad() { }
}
