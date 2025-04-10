package com.romanpulov.symphonytimer;

import android.app.Application;
import android.net.ConnectivityManager;
import androidx.preference.PreferenceManager;
import com.romanpulov.library.common.network.NetworkUtils;
import com.romanpulov.symphonytimer.cloud.AbstractCloudAccountFacade;
import com.romanpulov.symphonytimer.cloud.CloudAccountFacadeFactory;
import com.romanpulov.symphonytimer.helper.LoggerHelper;
import com.romanpulov.symphonytimer.preference.PreferenceRepository;
import com.romanpulov.symphonytimer.worker.LoaderWorker;
import java.util.concurrent.TimeUnit;

public class SymphonyTimerApplication extends Application {
    private static final String TAG = SymphonyTimerApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        //cancel works
        LoaderWorker.cancelAllWorkers(getApplicationContext());

        //setup network monitoring
        setupNetworkMonitoring();
    }

    private void setupNetworkMonitoring() {
        ConnectivityManager connectivityManager = getApplicationContext().getSystemService(ConnectivityManager.class);

        NetworkUtils.ConnectivityMonitor connectivityMonitor = new NetworkUtils.ConnectivityMonitor();
        connectivityMonitor.registerOnInternetAvailableListener(networkCapabilities -> {
            long lastLoadedTime = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext())
                    .getLong(PreferenceRepository.PREF_KEY_CLOUD_BACKUP_LAST_LOADED, -1);
            LoggerHelper.logContext(getApplicationContext(), TAG, "Last load time:" + lastLoadedTime);

            long daysSinceLastBackup = TimeUnit.DAYS.convert(System.currentTimeMillis() - lastLoadedTime, TimeUnit.MILLISECONDS);
            LoggerHelper.logContext(getApplicationContext(), TAG, "Since last backup:" + daysSinceLastBackup);

            if (daysSinceLastBackup > 6) {
                int cloudType = Integer.parseInt(PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext())
                        .getString(PreferenceRepository.PREF_KEY_CLOUD_ACCOUNT_TYPE, "-1"));
                LoggerHelper.logContext(getApplicationContext(), TAG, "Cloud type:" + cloudType);

                if (cloudType != -1) {
                    if (NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                        LoggerHelper.logContext(getApplicationContext(), TAG, "Internet available");
                        final AbstractCloudAccountFacade cloudAccountFacade = CloudAccountFacadeFactory.fromCloudAccountType(cloudType);
                        if (cloudAccountFacade != null) {
                            LoggerHelper.logContext(getApplicationContext(), TAG, "Starting worker " + cloudAccountFacade.getSilentBackupLoaderClassName());
                            LoaderWorker.scheduleWorker(getApplicationContext(), cloudAccountFacade.getSilentBackupLoaderClassName());
                        } else {
                            LoggerHelper.logContext(getApplicationContext(), TAG, "No cloud worker found");
                        }
                    } else {
                        LoggerHelper.logContext(getApplicationContext(), TAG, "Internet not available");
                    }
                } else {
                    LoggerHelper.logContext(getApplicationContext(), TAG, "Cloud type not set up");
                }
            }
        });

        connectivityManager.registerDefaultNetworkCallback(connectivityMonitor);

    }
}
