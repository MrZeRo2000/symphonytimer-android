package com.romanpulov.symphonytimer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class SettingsFragment extends PreferenceListFragment implements
	SharedPreferences.OnSharedPreferenceChangeListener,
	Preference.OnPreferenceClickListener,
	PreferenceListFragment.OnPreferenceAttachedListener {

	public static final String SHARED_PREFS_NAME = "settings";

	@Override
	public void onCreate(Bundle icicle) {
	
		super.onCreate(icicle);
		PreferenceManager preferenceManager = getPreferenceManager();
		preferenceManager.setSharedPreferencesName(SHARED_PREFS_NAME);
		addPreferencesFromResource(R.xml.preferences);
		preferenceManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		Preference button = (Preference)findPreference("pref_backup");
		button.setOnPreferenceClickListener(this);
		//button.setOnPreferenceChangeListener(onPreferenceChangeListener);
		//Toast.makeText(getActivity(), button.getTitle(), Toast.LENGTH_LONG).show();
	}
	
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		final String localBackupFileName = StorageManager.getInstance(getActivity()).CreateLocalBackup();
		if (null != localBackupFileName) {
			Toast.makeText(getActivity(), localBackupFileName, Toast.LENGTH_LONG).show();
		}
		return false;
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
	    String key) {
	}
	
	@Override
	public void onPreferenceAttached(PreferenceScreen root, int xmlId) {
		if (root == null)
		    return;
	}
}