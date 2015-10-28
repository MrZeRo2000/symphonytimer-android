package com.romanpulov.symphonytimer.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.app.DialogFragment;
import android.widget.Toast;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.StorageHelper;
import com.romanpulov.symphonytimer.helper.db.DBHelper;

public class SettingsFragment extends PreferenceFragment implements
	SharedPreferences.OnSharedPreferenceChangeListener,
	Preference.OnPreferenceClickListener {

	public static final String SHARED_PREFS_NAME = "settings";

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

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
	    			AlertOkCancelDialogFragment deleteDialog = AlertOkCancelDialogFragment.newAlertOkCancelDialog(null, R.string.question_are_you_sure);
	    			deleteDialog.setOkButtonClick(onDeleteOkButtonClick);
	    			deleteDialog.show(getActivity().getFragmentManager(), null);
					
					return false;
				};
				
			    private AlertOkCancelDialogFragment.OnOkButtonClick onDeleteOkButtonClick = new AlertOkCancelDialogFragment.OnOkButtonClick() {
					@Override
					public void OnOkButtonClickEvent(DialogFragment dialog) {
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
	    			AlertOkCancelDialogFragment deleteDialog = AlertOkCancelDialogFragment.newAlertOkCancelDialog(null, R.string.question_are_you_sure);
	    			deleteDialog.setOkButtonClick(onDeleteOkButtonClick);
	    			deleteDialog.show(getActivity().getFragmentManager(), null);
					
					return false;
				};
				
			    private AlertOkCancelDialogFragment.OnOkButtonClick onDeleteOkButtonClick = new AlertOkCancelDialogFragment.OnOkButtonClick() {
					@Override
					public void OnOkButtonClickEvent(DialogFragment dialog) {
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
}