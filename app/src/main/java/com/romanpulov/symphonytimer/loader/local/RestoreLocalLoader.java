package com.romanpulov.symphonytimer.loader.local;

import android.content.Context;

import com.romanpulov.library.common.loader.core.AbstractContextLoader;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;

/**
 * Local restore loader
 * Created by romanpulov on 30.11.2017.
 */

public class RestoreLocalLoader extends AbstractContextLoader {

    private final DBStorageHelper mDBStorageHelper;

    public RestoreLocalLoader(Context context) {
        super(context);
        mDBStorageHelper = new DBStorageHelper(context);
    }

    @Override
    public void load() throws Exception {
        String loadResult = mDBStorageHelper.restoreLocalBackup();
        if (loadResult == null)
            throw new Exception(mContext.getString(R.string.error_load_local_backup));
        PreferenceRepository.setPreferenceKeyLastLoadedCurrentTime(mContext, PreferenceRepository.PREF_KEY_LOCAL_RESTORE);
    }
}
