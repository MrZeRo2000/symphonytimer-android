package com.romanpulov.symphonytimer.activity;

import com.romanpulov.symphonytimer.R;

import android.os.Bundle;

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
            // actionBar.setIcon(R.drawable.tuba);
        }
    }
}