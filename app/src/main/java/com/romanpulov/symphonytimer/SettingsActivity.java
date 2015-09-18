package com.romanpulov.symphonytimer;

import com.romanpulov.symphonytimer.PreferenceListFragment.OnPreferenceAttachedListener;

import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class SettingsActivity extends ActionBarActivity implements OnPreferenceAttachedListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
        actionBar.setIcon(R.drawable.tuba);

        //addPreferencesFromResource(R.xml.preferences);
    }

	@Override
	public void onPreferenceAttached(PreferenceScreen root, int xmlId) {
		// TODO Auto-generated method stub
		
	}
}