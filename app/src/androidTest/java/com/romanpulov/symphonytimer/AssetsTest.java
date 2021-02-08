package com.romanpulov.symphonytimer;

import android.Manifest;
import android.os.Environment;
import android.util.Log;

import androidx.test.filters.SmallTest;
import androidx.test.rule.GrantPermissionRule;

import com.romanpulov.symphonytimer.helper.AssetsHelper;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SmallTest
@Ignore("Not valid for Android 10+")
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
    public void testListAssets(){
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            log("Storage State Mounted");
            AssetsHelper assetsHelper = new AssetsHelper(getApplicationContext());
            List<String> assets = assetsHelper.getAssets();
            assertEquals(6, assets.size());
        }
    }
}
