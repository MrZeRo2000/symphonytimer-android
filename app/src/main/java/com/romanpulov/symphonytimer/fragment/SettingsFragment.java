package com.romanpulov.symphonytimer.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceManager;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceFragmentCompat;

import com.romanpulov.library.common.account.AbstractCloudAccountManager;
import com.romanpulov.library.common.network.NetworkUtils;
import com.romanpulov.library.dropbox.DropboxHelper;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.activity.SettingsActivity;
import com.romanpulov.symphonytimer.cloud.AbstractCloudAccountFacade;
import com.romanpulov.symphonytimer.cloud.CloudAccountFacadeFactory;
import com.romanpulov.symphonytimer.helper.PermissionRequestHelper;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.loader.dropbox.BackupDropboxUploader;
import com.romanpulov.symphonytimer.loader.dropbox.RestoreDropboxDownloader;
import com.romanpulov.symphonytimer.loader.onedrive.BackupOneDriveUploader;
import com.romanpulov.symphonytimer.loader.onedrive.RestoreOneDriveDownloader;
import com.romanpulov.symphonytimer.preference.PreferenceBackupCloudProcessor;
import com.romanpulov.symphonytimer.preference.PreferenceBackupLocalProcessor;
import com.romanpulov.symphonytimer.preference.PreferenceLoaderProcessor;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;
import com.romanpulov.symphonytimer.preference.PreferenceRestoreCloudProcessor;
import com.romanpulov.symphonytimer.preference.PreferenceRestoreLocalProcessor;
import com.romanpulov.symphonytimer.service.LoaderService;
import com.romanpulov.symphonytimer.service.LoaderServiceManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.romanpulov.symphonytimer.preference.PreferenceRepository.PREF_KEY_CLOUD_ACCOUNT_TYPE;

public class SettingsFragment extends PreferenceFragmentCompat implements
	SharedPreferences.OnSharedPreferenceChangeListener,
	Preference.OnPreferenceClickListener {

    private PreferenceBackupCloudProcessor mPreferenceBackupCloudProcessor;
    private PreferenceRestoreCloudProcessor mPreferenceRestoreCloudProcessor;
    private PreferenceBackupLocalProcessor mPreferenceBackupLocalProcessor;
    private PreferenceRestoreLocalProcessor  mPreferenceRestoreLocalProcessor;

    private PermissionRequestHelper mWriteStorageRequestHelper;

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

    private static class ListPreferenceSummaryHandler {
        private final String mSummaryString;

        ListPreferenceSummaryHandler(String summaryString) {
            mSummaryString = summaryString;
        }

        Preference.OnPreferenceChangeListener newOnPreferenceChangeListener() {
            return new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ListPreference listPreference = (ListPreference)preference;
                    int selectedIndex = Arrays.asList(listPreference.getEntryValues()).indexOf((CharSequence)newValue);
                    if (selectedIndex > -1) {
                        preference.setSummary(String.format(mSummaryString, listPreference.getEntries()[selectedIndex]));
                    }

                    return false;
                }
            };
        }
    }

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

	}

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(R.xml.preferences);

        mWriteStorageRequestHelper = new PermissionRequestHelper(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        Preference preference;

        //reset data
        preference = findPreference("pref_reset_data");
        if (null != preference) {
            preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    AlertOkCancelDialogFragment deleteDialog = AlertOkCancelDialogFragment.newAlertOkCancelDialog(null, R.string.question_are_you_sure);
                    deleteDialog.setOkButtonClick(onDeleteOkButtonClick);
                    deleteDialog.show(getActivity().getSupportFragmentManager(), null);

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

        /*
        preference = findPreference("pref_wake_before");
        ListPreferenceSummaryHandler preferenceWakeBeforeHandler = new ListPreferenceSummaryHandler(getString(R.string.pref_wake_before_summary));
        preference.setOnPreferenceChangeListener(preferenceWakeBeforeHandler.newOnPreferenceChangeListener());


        preference = findPreference("pref_auto_timer_disable");
        ListPreferenceSummaryHandler preferenceAutoTimerDisableHandler = new ListPreferenceSummaryHandler(getString(R.string.pref_auto_timer_disable_summary));
        preference.setOnPreferenceChangeListener(preferenceAutoTimerDisableHandler.newOnPreferenceChangeListener());

         */

        //account dropbox
        preference = findPreference("pref_cloud_account");
        if (null != preference) {
            preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int cloudAccountType = Integer.valueOf(sp.getString("pref_cloud_account_type", "-1"));
                    if (cloudAccountType == -1) {
                        PreferenceRepository.displayMessage(SettingsFragment.this, getString(R.string.error_cloud_account_type_not_set_up));
                    } else {
                        CloudAccountFacadeFactory.fromCloudAccountType(cloudAccountType).setupAccount(getActivity());
                    }
                    //DropboxHelper.getInstance(getActivity().getApplicationContext()).invokeAuthActivity(getActivity().getResources().getString(R.string.app_key));
                    return false;
                }
            });
        }

        //backup local
        mPreferenceBackupLocalProcessor = new PreferenceBackupLocalProcessor(this);
        mPreferenceLoadProcessors.put(mPreferenceBackupLocalProcessor.getLoaderClass().getName(), mPreferenceBackupLocalProcessor);
        setupPrefLocalBackupLoadService();

        //restore local
        mPreferenceRestoreLocalProcessor = new PreferenceRestoreLocalProcessor(this);
        mPreferenceLoadProcessors.put(mPreferenceRestoreLocalProcessor.getLoaderClass().getName(), mPreferenceRestoreLocalProcessor);
        setupPrefLocalRestoreLoadService();

        //backup cloud
        mPreferenceBackupCloudProcessor = new PreferenceBackupCloudProcessor(this);
        //mPreferenceLoadProcessors.put(mPreferenceBackupCloudProcessor.getLoaderClass().getName(), mPreferenceBackupCloudProcessor);
        mPreferenceLoadProcessors.put(BackupDropboxUploader.class.getName(), mPreferenceBackupCloudProcessor);
        mPreferenceLoadProcessors.put(BackupOneDriveUploader.class.getName(), mPreferenceBackupCloudProcessor);
        setupPrefCloudBackupLoadService();

        //restore dropbox
        mPreferenceRestoreCloudProcessor = new PreferenceRestoreCloudProcessor(this);
        //mPreferenceLoadProcessors.put(mPreferenceRestoreCloudProcessor.getLoaderClass().getName(), mPreferenceRestoreCloudProcessor);
        mPreferenceLoadProcessors.put(RestoreDropboxDownloader.class.getName(), mPreferenceRestoreCloudProcessor);
        mPreferenceLoadProcessors.put(RestoreOneDriveDownloader.class.getName(), mPreferenceRestoreCloudProcessor);
        setupPrefDropboxRestoreLoadService();
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

    public void executeLocalBackup() {
        mPreferenceBackupLocalProcessor.preExecute();
        mLoaderServiceManager.startLoader(mPreferenceBackupLocalProcessor.getLoaderClass().getName());
    }

    /**
     * Local backup using service
     */
    private void setupPrefLocalBackupLoadService() {
        PreferenceRepository.updatePreferenceKeySummary(this, PreferenceRepository.PREF_KEY_LOCAL_BACKUP, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);

        Preference pref = findPreference(PreferenceRepository.PREF_KEY_LOCAL_BACKUP);
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if (mLoaderServiceManager == null)
                    return true;
                else {
                    if (mLoaderServiceManager.isLoaderServiceRunning())
                        PreferenceRepository.displayMessage(SettingsFragment.this, getText(R.string.error_load_process_running));
                    else {
                        if (mWriteStorageRequestHelper.isPermissionGranted())
                            executeLocalBackup();
                        else
                            mWriteStorageRequestHelper.requestPermission(SettingsActivity.PERMISSION_REQUEST_LOCAL_BACKUP);
                    }

                    return true;
                }
            }
        });
    }

    public void executeLocalRestore() {
        mPreferenceRestoreLocalProcessor.preExecute();
        mLoaderServiceManager.startLoader(mPreferenceRestoreLocalProcessor.getLoaderClass().getName());
    }

    /**
     * Local restore using service
     */
    private void setupPrefLocalRestoreLoadService() {
        PreferenceRepository.updatePreferenceKeySummary(this, PreferenceRepository.PREF_KEY_LOCAL_RESTORE, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);

        Preference pref = findPreference(PreferenceRepository.PREF_KEY_LOCAL_RESTORE);

        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (mLoaderServiceManager == null)
                    return true;
                else {

                    if (mLoaderServiceManager.isLoaderServiceRunning())
                        PreferenceRepository.displayMessage(SettingsFragment.this, getText(R.string.error_load_process_running));
                    else {
                        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert
                                .setTitle(R.string.question_are_you_sure)
                                .setPositiveButton(R.string.caption_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (mWriteStorageRequestHelper.isPermissionGranted())
                                            executeLocalRestore();
                                        else
                                            mWriteStorageRequestHelper.requestPermission(SettingsActivity.PERMISSION_REQUEST_LOCAL_RESTORE);
                                    }
                                })
                                .setNegativeButton(R.string.caption_cancel, null)
                                .show();
                    }
                }

                return true;
            }
        });
    }

    public void executeDropboxBackup() {
        // create local backup first
        DBStorageHelper storageHelper = new DBStorageHelper(getActivity());
        String backupResult = storageHelper.createLocalBackup();

        if (backupResult == null)
            PreferenceRepository.displayMessage(SettingsFragment.this, getString(R.string.error_backup));
        else {
            mPreferenceBackupCloudProcessor.preExecute();
            mLoaderServiceManager.startLoader(mPreferenceBackupCloudProcessor.getLoaderClass().getName());
        }
    }

    public void executeCloudBackup(int cloudAccountType) {
        final AbstractCloudAccountFacade cloudAccountFacade = CloudAccountFacadeFactory.fromCloudAccountType(cloudAccountType);

        if (cloudAccountFacade != null) {
            final AbstractCloudAccountManager accountManager = cloudAccountFacade.getAccountManager(getActivity());
            if (accountManager != null) {
                mPreferenceBackupCloudProcessor.preExecute();

                accountManager.setOnAccountSetupListener(new AbstractCloudAccountManager.OnAccountSetupListener() {
                    @Override
                    public void onAccountSetupSuccess() {
                        DBStorageHelper storageHelper = new DBStorageHelper(getActivity());
                        String backupResult = storageHelper.createLocalBackup();

                        if (backupResult == null)
                            PreferenceRepository.displayMessage(getActivity(), getString(R.string.error_backup));
                        else {
                            mPreferenceBackupCloudProcessor.preExecute();
                            mLoaderServiceManager.startLoader(cloudAccountFacade.getBackupLoaderClassName());
                        }
                    }

                    @Override
                    public void onAccountSetupFailure(String errorText) {
                        PreferenceRepository.displayMessage(getActivity(), errorText);
                        mPreferenceBackupCloudProcessor.postExecute(errorText);
                    }
                });

                accountManager.setupAccount();
            }
        }
    }

    /**
     * Cloud backup using service
     */
    private void setupPrefCloudBackupLoadService() {
        PreferenceRepository.updatePreferenceKeySummary(this, PreferenceRepository.PREF_KEY_CLOUD_BACKUP, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);

        final Preference pref = findPreference(PreferenceRepository.PREF_KEY_CLOUD_BACKUP);
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //check if cloud account type is set up
                    int cloudAccountType = -1;
                    final Preference prefCloudAccountType = findPreference(PREF_KEY_CLOUD_ACCOUNT_TYPE);
                    if (prefCloudAccountType != null) {
                        cloudAccountType = Integer.parseInt(prefCloudAccountType.getSharedPreferences().getString(prefCloudAccountType.getKey(), "-1"));
                        if (cloudAccountType == -1) {
                            PreferenceRepository.displayMessage(SettingsFragment.this, getText(R.string.error_cloud_account_type_not_set_up));
                            return true;
                        }
                    }

                    //check if internet is available
                    if (!checkInternetConnection())
                        return true;

                    if (mLoaderServiceManager == null)
                        return true;
                    else {
                        if (mLoaderServiceManager.isLoaderServiceRunning()) {
                            PreferenceRepository.displayMessage(SettingsFragment.this, getText(R.string.error_load_process_running));
                        } else {
                            executeCloudBackup(cloudAccountType);
                        }

                        return true;
                    }
                }
            });
        }
    }

    public void executeDropboxRestore() {
        mPreferenceRestoreCloudProcessor.preExecute();
        mLoaderServiceManager.startLoader(mPreferenceRestoreCloudProcessor.getLoaderClass().getName());
    }

    /**
     * Dropbox restore using service
     */
    private void setupPrefDropboxRestoreLoadService() {
        PreferenceRepository.updatePreferenceKeySummary(this, PreferenceRepository.PREF_KEY_CLOUD_RESTORE, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);

        Preference pref = findPreference(PreferenceRepository.PREF_KEY_CLOUD_RESTORE);

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
                        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert
                                .setTitle(R.string.question_are_you_sure)
                                .setPositiveButton(R.string.caption_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (mWriteStorageRequestHelper.isPermissionGranted())
                                            executeDropboxRestore();
                                        else
                                            mWriteStorageRequestHelper.requestPermission(SettingsActivity.PERMISSION_REQUEST_DROPBOX_RESTORE);
                                    }
                                })
                                .setNegativeButton(R.string.caption_cancel, null)
                                .show();
                    }
                }

                return true;
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