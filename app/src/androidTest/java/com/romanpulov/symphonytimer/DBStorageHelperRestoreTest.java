package com.romanpulov.symphonytimer;

import android.test.AndroidTestCase;
import android.util.Log;

import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;

/**
 * Created by romanpulov on 27.12.2016.
 */

public class DBStorageHelperRestoreTest extends AndroidTestCase {
    public void testCase1() {
        assertTrue(true);
    }

    public void testCase2() {
        DBStorageHelper dh = new DBStorageHelper(getContext());
        int res = dh.restoreLocalXmlBackup();
        assertEquals(res, 0);
    }
}
