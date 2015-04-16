package com.romanpulov.symphonytimer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.DialogFragment;
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
		
		Preference button;
		
		//reset data
		button = (Preference)findPreference("pref_reset_data");
		if (null != button) {
			button.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					// TODO Auto-generated method stub
					
	    			AlertOkCancelDialog deleteDialog = AlertOkCancelDialog.newAlertOkCancelDialog(null, R.string.question_are_you_sure); 
	    			deleteDialog.setOkButtonClick(onDeleteOkButtonClick);
	    			deleteDialog.show(getActivity().getSupportFragmentManager(), null);					
					
					return false;
				};
				
			    private AlertOkCancelDialog.OnOkButtonClick onDeleteOkButtonClick = new AlertOkCancelDialog.OnOkButtonClick() {
					@Override
					public void OnOkButtonClickEvent(DialogFragment dialog) {
						// TODO Auto-generated method stub
						DBHelper.getInstance(getActivity()).clearData();
					}
				};		

			});
		}
		
		//local backup
		button = (Preference)findPreference("pref_local_backup");
		if (null != button) {
			button.setOnPreferenceClickListener(new OnPreferenceClickListener() {			
				@Override
				public boolean onPreferenceClick(Preference preference) {
					// TODO Auto-generated method stub
					final String localBackupFileName = new StorageHelper(getActivity()).createLocalBackup();
					if (null != localBackupFileName) {
						Toast.makeText(getActivity(), localBackupFileName, Toast.LENGTH_SHORT).show();
					}					
					
					return false;
				};
				
			});
		}
		
		//local restore
		button = (Preference)findPreference("pref_local_restore");
		if (null != button) {
			button.setOnPreferenceClickListener(new OnPreferenceClickListener() {		
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					// TODO Auto-generated method stub

	    			AlertOkCancelDialog deleteDialog = AlertOkCancelDialog.newAlertOkCancelDialog(null, R.string.question_are_you_sure); 
	    			deleteDialog.setOkButtonClick(onDeleteOkButtonClick);
	    			deleteDialog.show(getActivity().getSupportFragmentManager(), null);					
					
					return false;
				};
				
			    private AlertOkCancelDialog.OnOkButtonClick onDeleteOkButtonClick = new AlertOkCancelDialog.OnOkButtonClick() {
					@Override
					public void OnOkButtonClickEvent(DialogFragment dialog) {
						// TODO Auto-generated method stub
						
						int res = new StorageHelper(getActivity()).restoreLocalXmlBackup();
						
						if (res != 0) {
							Toast.makeText(getActivity(), String.format(getResources().getString(R.string.error_load_local_backup), res), Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(getActivity(), getResources().getString(R.string.info_load_local_backup), Toast.LENGTH_SHORT).show();							
						}

					}
				};		
				
				
			});
		}
		
	}
	
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub		
		final String localBackupFileName = new StorageHelper(getActivity()).createLocalBackup();
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