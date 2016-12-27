package com.romanpulov.symphonytimer;

import android.test.AndroidTestCase;

import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;

/**
 * Created by romanpulov on 27.12.2016.
 */

public class DBStorageHelperBackupTest extends AndroidTestCase {
    public void testCase1() {
        assertTrue(true);
    }

    public void testCase2() {
        DBStorageHelper dh = new DBStorageHelper(getContext());
        dh.createLocalBackup();
    }
}
