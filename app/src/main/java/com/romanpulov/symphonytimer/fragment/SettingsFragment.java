package com.romanpulov.symphonytimer.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.romanpulov.library.common.network.NetworkUtils;
import com.romanpulov.library.dropbox.DropboxHelper;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.preference.PreferenceBackupDropboxProcessor;
import com.romanpulov.symphonytimer.preference.PreferenceLoaderProcessor;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;
import com.romanpulov.symphonytimer.service.LoaderService;
import com.romanpulov.symphonytimer.service.LoaderServiceManager;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends PreferenceFragment implements
	SharedPreferences.OnSharedPreferenceChangeListener,
	Preference.OnPreferenceClickListener {

    private PreferenceBackupDropboxProcessor mPreferenceBackupDropboxProcessor;

    private Map<String, PreferenceLoaderProcessor> mPreferenceLoadProcessors = new HashMap<>();
    private LoaderServiceManager mLoaderServiceManager;
    private BroadcastReceiver mLoaderServiceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String loaderClassName = intent.getStringExtra(LoaderService.SERVICE_RESULT_LOADER_NAME);
            String errorMessage = intent.getStringExtra(LoaderService.SERVICE_RESULT_ERROR_MESSAGE);

            PreferenceLoaderProcessor preferenceLoaderProcessor = mPreferenceLoadProcessors.get(loaderClassName);
            if (preferenceLoaderProcessor != null)
                preferenceLoaderProcessor.postExecute(errorMessage);
        }
    };

    private LoaderService mBoundService;
    private boolean mIsBound;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((LoaderService.LoaderBinder)service).getService();

            PreferenceLoaderProcessor preferenceLoaderProcessor = mPreferenceLoadProcessors.get(mBoundService.getLoaderClassName());
            if (preferenceLoaderProcessor != null)
                preferenceLoaderProcessor.preExecute();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
        }
    };

    /*

    private class RestoreLocalXmlTask extends AsyncTask<Void, Void, Integer> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(R.string.caption_loading);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return new DBStorageHelper(getActivity()).restoreLocalXmlBackup();
        }

        @Override
        protected void onPostExecute(Integer res) {
            progressDialog.dismiss();
            if (res != 0) {
                Toast.makeText(getActivity(), String.format(getResources().getString(R.string.error_load_local_backup), res), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.info_load_local_backup), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class CreateLocalBackupTask extends AsyncTask<Void, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(R.string.caption_saving);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            return new DBStorageHelper(getActivity()).createLocalBackup();
        }

        @Override
        protected void onPostExecute(String res) {
            progressDialog.dismiss();
            if (res != null)
                Toast.makeText(getActivity(), res, Toast.LENGTH_LONG).show();
        }
    }
    */

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sp.registerOnSharedPreferenceChangeListener(this);
		
		addPreferencesFromResource(R.xml.preferences);
		
		Preference preference;

		//reset data
		preference = findPreference("pref_reset_data");
		if (null != preference) {
			preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference arg0) {
	    			AlertOkCancelDialogFragment deleteDialog = AlertOkCancelDialogFragment.newAlertOkCancelDialog(null, R.string.question_are_you_sure);
	    			deleteDialog.setOkButtonClick(onDeleteOkButtonClick);
	    			deleteDialog.show(getActivity().getFragmentManager(), null);
					
					return false;
				}
				
			    private final AlertOkCancelDialogFragment.OnOkButtonClick onDeleteOkButtonClick = new AlertOkCancelDialogFragment.OnOkButtonClick() {
					@Override
					public void OnOkButtonClickEvent(DialogFragment dialog) {
						DBHelper.getInstance(getActivity()).clearData();
					}
				};
			});
		}
		
		//local backup
		preference = findPreference("pref_local_backup");
		if (null != preference) {
			preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					final String localBackupFileName = new DBStorageHelper(getActivity()).createLocalBackup();
					if (null != localBackupFileName) {
						Toast.makeText(getActivity(), localBackupFileName, Toast.LENGTH_SHORT).show();
					}					
					
					return false;
				}
			});
		}
		
		//local restore
		preference = findPreference("pref_local_restore");
		if (null != preference) {
			preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
	    			AlertOkCancelDialogFragment deleteDialog = AlertOkCancelDialogFragment.newAlertOkCancelDialog(null, R.string.question_are_you_sure);
	    			deleteDialog.setOkButtonClick(onDeleteOkButtonClick);
	    			deleteDialog.show(getActivity().getFragmentManager(), null);
					return false;
				}
				
			    private final AlertOkCancelDialogFragment.OnOkButtonClick onDeleteOkButtonClick = new AlertOkCancelDialogFragment.OnOkButtonClick() {
					@Override
					public void OnOkButtonClickEvent(DialogFragment dialog) {
                        new DBStorageHelper(getActivity()).restoreLocalXmlBackup();
					}
				};
			});
		}

		//account dropbox
        preference = findPreference("pref_dropbox_account");
		if (null != preference) {
		    preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DropboxHelper.getInstance(getActivity().getApplicationContext()).invokeAuthActivity(getActivity().getResources().getString(R.string.app_key));
                    return false;
                }
            });
        }

        //backup dropbox
        mPreferenceBackupDropboxProcessor = new PreferenceBackupDropboxProcessor(this);
        mPreferenceLoadProcessors.put(mPreferenceBackupDropboxProcessor.getLoaderClass().getName(), mPreferenceBackupDropboxProcessor);
        setupPrefDropboxBackupLoadService();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
        //new CreateLocalBackupTask().execute();
		return false;
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
	    String key) {
	}

    private boolean checkInternetConnection() {
        if (NetworkUtils.isNetworkAvailable(getActivity()))
            return true;
        else {
            PreferenceRepository.displayMessage(this, getString(R.string.error_internet_not_available));
            return false;
        }
    }

    /**
     * Dropbox backup using service
     */
    private void setupPrefDropboxBackupLoadService() {
        PreferenceRepository.updateDropboxBackupPreferenceSummary(this, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);

        Preference pref = findPreference(PreferenceRepository.PREF_KEY_BASIC_NOTE_DROPBOX_BACKUP);
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //check if internet is available
                if (!checkInternetConnection())
                    return true;

                if (mLoaderServiceManager == null)
                    return true;
                else {
                    if (mLoaderServiceManager.isLoaderServiceRunning())
                        PreferenceRepository.displayMessage(SettingsFragment.this, getText(R.string.error_load_process_running));
                    else {

                        // create local backup first
                        DBStorageHelper storageHelper = new DBStorageHelper(getActivity());
                        String backupResult = storageHelper.createLocalBackup();

                        if (backupResult == null)
                            PreferenceRepository.displayMessage(SettingsFragment.this, getString(R.string.error_backup));
                        else {
                            mPreferenceBackupDropboxProcessor.preExecute();
                            mLoaderServiceManager.startLoader(mPreferenceBackupDropboxProcessor.getLoaderClass().getName());
                        }
                    }

                    return true;
                }
            }
        });
    }

    private void doBindService(Activity activity) {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        mIsBound = activity.bindService(new Intent(activity,
                LoaderService.class), mConnection, 0);
    }

    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LocalBroadcastManager.getInstance(activity).registerReceiver(mLoaderServiceBroadcastReceiver, new IntentFilter(LoaderService.SERVICE_RESULT_INTENT_NAME));
        mLoaderServiceManager = new LoaderServiceManager(activity);
        doBindService(activity);
    }

    @Override
    public void onDetach() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mLoaderServiceBroadcastReceiver);
        doUnbindService();
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        DropboxHelper.getInstance(getActivity().getApplicationContext()).refreshAccessToken();
    }
}