package com.romanpulov.symphonytimer.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.StorageHelper;
import com.romanpulov.symphonytimer.helper.db.DBHelper;

public class SettingsFragment extends PreferenceListFragment implements
	SharedPreferences.OnSharedPreferenceChangeListener,
	Preference.OnPreferenceClickListener,
	PreferenceListFragment.OnPreferenceAttachedListener {

	public static final String SHARED_PREFS_NAME = "settings";

	@Override
	public void onCreate(Bundle icicle) {
	
		super.onCreate(icicle);
		
		/*
		PreferenceManager preferenceManager = getPreferenceManager();
		preferenceManager.setSharedPreferencesName(SHARED_PREFS_NAME);		
		preferenceManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		*/
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sp.registerOnSharedPreferenceChangeListener(this);
		
		addPreferencesFromResource(R.xml.preferences);
		
		Preference button;
		
		//reset data
		button = (Preference)findPreference("pref_reset_data");
		if (null != button) {
			button.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					// TODO Auto-generated method stub
					
	    			AlertOkCancelDialogFragment deleteDialog = AlertOkCancelDialogFragment.newAlertOkCancelDialog(null, R.string.question_are_you_sure);
	    			deleteDialog.setOkButtonClick(onDeleteOkButtonClick);
	    			deleteDialog.show(getActivity().getSupportFragmentManager(), null);					
					
					return false;
				};
				
			    private AlertOkCancelDialogFragment.OnOkButtonClick onDeleteOkButtonClick = new AlertOkCancelDialogFragment.OnOkButtonClick() {
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

	    			AlertOkCancelDialogFragment deleteDialog = AlertOkCancelDialogFragment.newAlertOkCancelDialog(null, R.string.question_are_you_sure);
	    			deleteDialog.setOkButtonClick(onDeleteOkButtonClick);
	    			deleteDialog.show(getActivity().getSupportFragmentManager(), null);					
					
					return false;
				};
				
			    private AlertOkCancelDialogFragment.OnOkButtonClick onDeleteOkButtonClick = new AlertOkCancelDialogFragment.OnOkButtonClick() {
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