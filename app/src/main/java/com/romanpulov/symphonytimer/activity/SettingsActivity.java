package com.romanpulov.symphonytimer.activity;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.fragment.SettingsFragment;
import com.romanpulov.symphonytimer.helper.PermissionRequestHelper;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    public final static int PERMISSION_REQUEST_LOCAL_BACKUP = 101;
    public final static int PERMISSION_REQUEST_LOCAL_RESTORE = 102;
    public final static int PERMISSION_REQUEST_DROPBOX_BACKUP = 103;
    public final static int PERMISSION_REQUEST_DROPBOX_RESTORE = 104;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setIcon(R.drawable.tuba);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        SettingsFragment settingsFragment = (SettingsFragment)getSupportFragmentManager().findFragmentById(R.id.settingsfragment);

        if ((settingsFragment != null) && (PermissionRequestHelper.isGrantResultSuccessful(grantResults))) {
            switch (requestCode) {
                case PERMISSION_REQUEST_LOCAL_BACKUP:
                    settingsFragment.executeLocalBackup();
                    break;
                case PERMISSION_REQUEST_LOCAL_RESTORE:
                    settingsFragment.executeLocalRestore();
                    break;
                case PERMISSION_REQUEST_DROPBOX_BACKUP:
                    settingsFragment.executeDropboxBackup();
                    break;
                case PERMISSION_REQUEST_DROPBOX_RESTORE:
                    settingsFragment.executeDropboxRestore();
                    break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}