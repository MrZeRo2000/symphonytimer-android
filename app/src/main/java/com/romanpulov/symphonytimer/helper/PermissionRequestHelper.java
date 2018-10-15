package com.romanpulov.symphonytimer.helper;

//TODO Put this class to the common library

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Helper class for managing permission requests
 */
public class PermissionRequestHelper {
    private final Activity mActivity;
    private final String mPermission;
    private final String[] mPermissions;

    public PermissionRequestHelper(Activity activity, String permission) {
        mActivity = activity;
        mPermission = permission;
        mPermissions = new String[]{mPermission};
    }

    public boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(mActivity, mPermission) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(int requestCode) {
        ActivityCompat.requestPermissions(mActivity, mPermissions, requestCode);
    }

    public static boolean isGrantResultSuccessful(int[] grantResults) {
        return (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED);
    }
}
