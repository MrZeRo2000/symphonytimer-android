package com.romanpulov.symphonytimer.fragment;

import static com.romanpulov.symphonytimer.preference.PreferenceRepository.PREF_KEY_CLOUD_ACCOUNT_TYPE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceFragmentCompat;

import androidx.work.WorkInfo;
import com.romanpulov.library.common.account.AbstractCloudAccountManager;
import com.romanpulov.library.common.network.NetworkUtils;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.cloud.AbstractCloudAccountFacade;
import com.romanpulov.symphonytimer.cloud.CloudAccountFacadeFactory;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.helper.db.DBStorageHelper;
import com.romanpulov.symphonytimer.loader.gdrive.BackupGDriveUploader;
import com.romanpulov.symphonytimer.loader.gdrive.RestoreGDriveDownloader;
import com.romanpulov.symphonytimer.loader.msgraph.BackupMSGraphUploader;
import com.romanpulov.symphonytimer.loader.msgraph.RestoreMSGraphDownloader;
import com.romanpulov.symphonytimer.model.TimerViewModel;
import com.romanpulov.symphonytimer.preference.PreferenceBackupCloudProcessor;
import com.romanpulov.symphonytimer.preference.PreferenceBackupLocalProcessor;
import com.romanpulov.symphonytimer.preference.PreferenceLoaderProcessor;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;
import com.romanpulov.symphonytimer.preference.PreferenceRestoreCloudProcessor;
import com.romanpulov.symphonytimer.preference.PreferenceRestoreLocalProcessor;
import com.romanpulov.symphonytimer.worker.LoaderWorker;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {
    private final static String TAG = SettingsFragment.class.getSimpleName();

    private PreferenceBackupCloudProcessor mPreferenceBackupCloudProcessor;
    private PreferenceRestoreCloudProcessor mPreferenceRestoreCloudProcessor;
    private PreferenceBackupLocalProcessor mPreferenceBackupLocalProcessor;
    private PreferenceRestoreLocalProcessor  mPreferenceRestoreLocalProcessor;

    private final Map<String, PreferenceLoaderProcessor> mPreferenceLoadProcessors = new HashMap<>();

    private final Observer<List<WorkInfo>> mLoaderWorkerObserver = workInfos -> {
        Log.d(TAG, "WorkerObserver: " + workInfos.size() + " items");
        Log.d(TAG, "WorkerObserver: " + workInfos);
        if (workInfos.size() == 1) {
            WorkInfo workInfo = workInfos.get(0);

            switch (workInfo.getState()) {
                case RUNNING -> {
                    String loaderClassName = LoaderWorker.getLoaderClassName(workInfo.getProgress());
                    PreferenceLoaderProcessor preferenceLoaderProcessor = mPreferenceLoadProcessors.get(loaderClassName);
                    if (preferenceLoaderProcessor != null) {
                        preferenceLoaderProcessor.preExecute();
                    }
                }
                case SUCCEEDED, FAILED -> {
                    String loaderClassName = LoaderWorker.getLoaderClassName(workInfo.getOutputData());
                    String errorMessage = LoaderWorker.getErrorMessage(workInfo.getOutputData());
                    PreferenceLoaderProcessor preferenceLoaderProcessor = mPreferenceLoadProcessors.get(loaderClassName);
                    if (preferenceLoaderProcessor != null) {
                        preferenceLoaderProcessor.postExecute(errorMessage);
                    }

                    TimerViewModel model = TimerViewModel.getInstance(requireActivity().getApplication());
                    model.loadTimers();
                }
            }
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        LoaderWorker.getWorkInfosLiveData(requireContext()).observe(this, mLoaderWorkerObserver);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        Preference preference;

        //reset data
        preference = findPreference("pref_reset_data");
        if (null != preference) {
            preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(@NotNull Preference arg0) {
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        AlertOkCancelDialogFragment deleteDialog = AlertOkCancelDialogFragment.newAlertOkCancelDialog(null, R.string.question_are_you_sure);
                        deleteDialog.setOkButtonClick(onDeleteOkButtonClick);
                        deleteDialog.show(activity.getSupportFragmentManager(), null);
                    }

                    return false;
                }

                private final AlertOkCancelDialogFragment.OnOkButtonClick onDeleteOkButtonClick =
                        dialog -> DBHelper.getInstance(getActivity()).clearData();
            });
        }

        //cloud account
        preference = findPreference("pref_cloud_account");
        if (null != preference) {
            preference.setOnPreferenceClickListener(preference1 -> {
                SharedPreferences sharedPreferences = Objects.requireNonNull(getPreferenceManager().getSharedPreferences());
                int cloudAccountType = Integer.parseInt(sharedPreferences.getString("pref_cloud_account_type", "-1"));
                if (cloudAccountType == -1) {
                    PreferenceRepository.displayMessage(SettingsFragment.this, getString(R.string.error_cloud_account_type_not_set_up));
                } else {
                    if (checkInternetConnection()) {
                        AbstractCloudAccountFacade cloudAccountFacade = CloudAccountFacadeFactory.fromCloudAccountType(cloudAccountType);
                        if (cloudAccountFacade != null) {
                            cloudAccountFacade.setupAccount(getActivity());
                        }
                    }
                }

                return false;
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
        mPreferenceLoadProcessors.put(BackupMSGraphUploader.class.getName(), mPreferenceBackupCloudProcessor);
        mPreferenceLoadProcessors.put(BackupGDriveUploader.class.getName(), mPreferenceBackupCloudProcessor);
        setupPrefCloudBackupLoadService();

        //restore cloud
        mPreferenceRestoreCloudProcessor = new PreferenceRestoreCloudProcessor(this);
        mPreferenceLoadProcessors.put(RestoreMSGraphDownloader.class.getName(), mPreferenceRestoreCloudProcessor);
        mPreferenceLoadProcessors.put(RestoreGDriveDownloader.class.getName(), mPreferenceRestoreCloudProcessor);
        setupPrefCloudRestoreLoadService();
    }

    private boolean checkInternetConnection() {
        if ((getContext() != null) && (NetworkUtils.isNetworkAvailable(getContext()))) {
            return true;
        }
        else {
            PreferenceRepository.displayMessage(this, getString(R.string.error_internet_not_available));
            return false;
        }
    }

    /**
     * Local backup using service
     */
    private void setupPrefLocalBackupLoadService() {
        PreferenceRepository.updatePreferenceKeySummary(this, PreferenceRepository.PREF_KEY_LOCAL_BACKUP, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);

        Preference pref = findPreference(PreferenceRepository.PREF_KEY_LOCAL_BACKUP);
        if (pref != null) {
            pref.setOnPreferenceClickListener(preference -> {
                if (LoaderWorker.isRunning(requireContext())) {
                    PreferenceRepository.displayMessage(SettingsFragment.this, getText(R.string.error_load_process_running));
                } else {
                    mPreferenceBackupLocalProcessor.preExecute();
                    LoaderWorker.scheduleWorker(requireContext(), mPreferenceBackupLocalProcessor.getLoaderClass().getName());
                }
                return true;
            });
        }
    }

    /**
     * Local restore using service
     */
    private void setupPrefLocalRestoreLoadService() {
        PreferenceRepository.updatePreferenceKeySummary(this, PreferenceRepository.PREF_KEY_LOCAL_RESTORE, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);

        Preference pref = findPreference(PreferenceRepository.PREF_KEY_LOCAL_RESTORE);
        if (pref != null) {
            pref.setOnPreferenceClickListener(preference -> {
                if (LoaderWorker.isRunning(requireContext())) {
                    PreferenceRepository.displayMessage(SettingsFragment.this, getText(R.string.error_load_process_running));
                } else {
                    Context context = getActivity();
                    if (context != null) {
                        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert
                                .setTitle(R.string.question_are_you_sure)
                                .setPositiveButton(R.string.caption_ok,
                                        (dialog, which) -> {
                                            mPreferenceRestoreLocalProcessor.preExecute();
                                            LoaderWorker.scheduleWorker(requireContext(), mPreferenceRestoreLocalProcessor.getLoaderClass().getName());
                                        })
                                .setNegativeButton(R.string.caption_cancel, null)
                                .show();
                    }
                }
                return true;
            });
        }
    }

    public void executeCloudBackup(int cloudAccountType) {
        final AbstractCloudAccountFacade cloudAccountFacade = CloudAccountFacadeFactory.fromCloudAccountType(cloudAccountType);

        if (cloudAccountFacade != null) {
            final AbstractCloudAccountManager<?> accountManager = cloudAccountFacade.getAccountManager(getActivity());
            if (accountManager != null) {
                mPreferenceBackupCloudProcessor.preExecute();

                accountManager.setOnAccountSetupListener(new AbstractCloudAccountManager.OnAccountSetupListener() {
                    @Override
                    public void onAccountSetupSuccess() {
                        /*
                        DBStorageHelper storageHelper = new DBStorageHelper(getActivity());
                        String backupResult = storageHelper.createLocalBackup();

                         */
                        String backupResult = DBStorageHelper.getInstance(getContext()).getDBBackupManager().createLocalBackup();

                        if (backupResult == null) {
                            PreferenceRepository.displayMessage(getActivity(), getString(R.string.error_backup));
                            mPreferenceBackupCloudProcessor.postExecute(getString(R.string.error_backup));
                        }
                        else {
                            mPreferenceBackupCloudProcessor.preExecute();
                            LoaderWorker.scheduleWorker(requireContext(), cloudAccountFacade.getBackupLoaderClassName());
                        }
                    }

                    @Override
                    public void onAccountSetupFailure(String errorText) {
                        PreferenceRepository.displayMessage(getActivity(), R.string.error_cloud_account, errorText);
                        mPreferenceBackupCloudProcessor.postExecute(errorText);
                    }
                });

                accountManager.setupAccount();
            }
        }
    }

    private int getCloudAccountType() {
        int result = -1;
        final Preference prefCloudAccountType = findPreference(PREF_KEY_CLOUD_ACCOUNT_TYPE);
        if (prefCloudAccountType != null) {
            result = Integer.parseInt(
                    Objects.requireNonNull(prefCloudAccountType.getSharedPreferences())
                    .getString(prefCloudAccountType.getKey(), "-1"));
        }
        return result;
    }

    /**
     * Cloud backup using service
     */
    private void setupPrefCloudBackupLoadService() {
        PreferenceRepository.updatePreferenceKeySummary(
                this,
                PreferenceRepository.PREF_KEY_CLOUD_BACKUP,
                PreferenceRepository.PREF_LOAD_CURRENT_VALUE);

        final Preference pref = findPreference(PreferenceRepository.PREF_KEY_CLOUD_BACKUP);
        if (pref != null) {
            pref.setOnPreferenceClickListener(preference -> {
                //check if cloud account type is set up
                final int cloudAccountType = getCloudAccountType();
                if (cloudAccountType == -1) {
                    PreferenceRepository.displayMessage(
                            SettingsFragment.this,
                            getText(R.string.error_cloud_account_type_not_set_up));
                    return true;
                }

                //check if internet is available
                if (!checkInternetConnection())
                    return true;

                if (LoaderWorker.isRunning(requireContext())) {
                    PreferenceRepository.displayMessage(
                            SettingsFragment.this,
                            getText(R.string.error_load_process_running));
                } else {
                    executeCloudBackup(cloudAccountType);
                }

                return true;
            });
        }
    }

    public void executeCloudRestore(int cloudAccountType) {
        final AbstractCloudAccountFacade cloudAccountFacade = CloudAccountFacadeFactory.fromCloudAccountType(cloudAccountType);

        if (cloudAccountFacade != null) {
            final AbstractCloudAccountManager<?> accountManager = cloudAccountFacade.getAccountManager(getActivity());
            if (accountManager != null) {
                mPreferenceRestoreCloudProcessor.preExecute();

                accountManager.setOnAccountSetupListener(new AbstractCloudAccountManager.OnAccountSetupListener() {
                    @Override
                    public void onAccountSetupSuccess() {
                        mPreferenceRestoreCloudProcessor.preExecute();
                        LoaderWorker.scheduleWorker(requireContext(), cloudAccountFacade.getRestoreLoaderClassName());
                    }

                    @Override
                    public void onAccountSetupFailure(String errorText) {
                        PreferenceRepository.displayMessage(getActivity(), R.string.error_cloud_account, errorText);
                        mPreferenceRestoreCloudProcessor.postExecute(errorText);
                    }
                });

                accountManager.setupAccount();
            }
        }
    }


    /**
     * Cloud restore using service
     */
    private void setupPrefCloudRestoreLoadService() {
        PreferenceRepository.updatePreferenceKeySummary(this, PreferenceRepository.PREF_KEY_CLOUD_RESTORE, PreferenceRepository.PREF_LOAD_CURRENT_VALUE);

        Preference pref = findPreference(PreferenceRepository.PREF_KEY_CLOUD_RESTORE);
        if (pref != null) {
            pref.setOnPreferenceClickListener(preference -> {
                //check if cloud account type is set up
                final int cloudAccountType = getCloudAccountType();
                if (cloudAccountType == -1) {
                    PreferenceRepository.displayMessage(SettingsFragment.this, getText(R.string.error_cloud_account_type_not_set_up));
                    return true;
                }

                //check if internet is available
                if (!checkInternetConnection())
                    return true;

                if (LoaderWorker.isRunning(requireContext())) {
                    PreferenceRepository.displayMessage(SettingsFragment.this, getText(R.string.error_load_process_running));
                } else {
                    Context context = getContext();
                    if (context != null) {
                        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert
                                .setTitle(R.string.question_are_you_sure)
                                .setPositiveButton(R.string.caption_ok,
                                        (dialog, which) -> executeCloudRestore(cloudAccountType))
                                .setNegativeButton(R.string.caption_cancel, null)
                                .show();
                    }
                }
                return true;
            });
        }
    }
}