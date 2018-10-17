package com.romanpulov.symphonytimer;

import android.support.test.filters.SmallTest;

import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;

import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getContext;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertTrue;

/**
 * Created by romanpulov on 27.12.2016.
 */
@SmallTest
public class DBStorageHelperBackupTest {
    @Test
    public void testCase1() {
        assertTrue(true);
    }

    @Test
    public void testCase2() {
        DBStorageHelper dh = new DBStorageHelper(getTargetContext());
        dh.createLocalBackup();
    }
}
