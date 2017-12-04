package com.romanpulov.symphonytimer.loader.local;

import android.content.Context;

import com.romanpulov.library.common.loader.core.AbstractContextLoader;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;

/**
 * Local backup loader
 * Created by romanpulov on 30.11.2017.
 */

public class BackupLocalLoader extends AbstractContextLoader {

    private final DBStorageHelper mDBStorageHelper;

    public BackupLocalLoader(Context context) {
        super(context);
        mDBStorageHelper = new DBStorageHelper(context);
    }

    @Override
    public void load() throws Exception {
        String localBackupFileName = mDBStorageHelper.createLocalBackup();
        if (localBackupFileName == null)
            throw new Exception(mContext.getString(R.string.error_backup));
        PreferenceRepository.setPreferenceKeyLastLoadedCurrentTime(mContext, PreferenceRepository.PREF_KEY_LOCAL_BACKUP);
    }
}
