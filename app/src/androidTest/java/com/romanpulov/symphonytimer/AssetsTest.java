package com.romanpulov.symphonytimer;

import android.Manifest;
import android.os.Environment;
import android.support.test.filters.SmallTest;
import android.support.test.rule.GrantPermissionRule;
import android.util.Log;

import com.romanpulov.symphonytimer.helper.AssetsHelper;

import org.junit.Rule;
import org.junit.Test;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SmallTest
public class AssetsTest {
    private static final String LOG_TAG = "AssetsTest";

    private static void log(String message) {
        Log.d(LOG_TAG, message);
    }

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void first() {
        assertTrue(1==1);
    }

    @Test
    public void testListAsset() {
        //AssetsHelper.listAssets(getTargetContext(), "pre_inst_images");
    }

    @Test
    public void testExternalStorage(){
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED))
            log("Storage State Mounted");

        AssetsHelper.copyAssets(getTargetContext());
    }
}
