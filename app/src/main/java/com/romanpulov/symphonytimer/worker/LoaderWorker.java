package com.romanpulov.symphonytimer.worker;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.work.*;
import com.google.common.util.concurrent.ListenableFuture;
import com.romanpulov.library.common.loader.core.Loader;
import com.romanpulov.library.common.loader.core.LoaderFactory;
import com.romanpulov.library.common.network.NetworkUtils;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.LoggerHelper;
import com.romanpulov.symphonytimer.loader.helper.LoaderNotificationHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_LOADER;

public class LoaderWorker extends Worker {
    private final static String TAG = LoaderWorker.class.getSimpleName();

    private void log(String message) {
        LoggerHelper.logContext(getApplicationContext(), TAG, message);
    }

    public static final String WORKER_NAME = LoaderWorker.class.getSimpleName() + "NAME";
    public static final String WORKER_RESULT_ERROR_MESSAGE = LoaderWorker.class.getSimpleName() + "ResultErrorMessage";

    private static final String WORKER_TAG = LoaderWorker.class.getSimpleName() + "TAG";
    private static final String WORKER_PARAM_LOADER_NAME = LoaderWorker.class.getSimpleName() + "LoaderName";

    private final String mLoaderClassName;

    public LoaderWorker(@NotNull Context context, @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.mLoaderClassName = workerParams.getInputData().getString(WORKER_PARAM_LOADER_NAME);
        setProgressAsync(new Data.Builder().putString(WORKER_PARAM_LOADER_NAME, this.mLoaderClassName).build());
    }

    @Override
    public @NotNull Result doWork() {
        log("Work started");

        String errorMessage;

        if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
            errorMessage = getApplicationContext().getString(R.string.error_internet_not_available);
            log(errorMessage);
        } else if (mLoaderClassName == null) {
            errorMessage = "Loader class name not provided";
            log(errorMessage);
        } else {
            Loader loader = LoaderFactory.fromClassName(getApplicationContext(), mLoaderClassName);
            if (loader == null) {
                errorMessage = "Failed to create loader " + mLoaderClassName;
                log(errorMessage);
            } else {
                log("Created loader " + mLoaderClassName);
                try {
                    loader.load();
                    errorMessage = null;
                    log("Loaded successfully");
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    log("Error loading:" + e);
                }
            }
        }

        if (errorMessage == null) {
            log("Successful");
            Data outputResult = new Data.Builder()
                    .putString(WORKER_PARAM_LOADER_NAME, mLoaderClassName)
                    .build();
            return Result.success(outputResult);
        } else {
            LoaderNotificationHelper.notify(
                    getApplicationContext(),
                    errorMessage,
                    NOTIFICATION_ID_LOADER);

            Data outputResult = new Data.Builder()
                    .putString(WORKER_PARAM_LOADER_NAME, mLoaderClassName)
                    .putString(WORKER_RESULT_ERROR_MESSAGE, errorMessage)
                    .build();
            return Result.failure(outputResult);
        }
    }

    private static void internalScheduleWorker(Context context, String loaderClassName) {
        LoggerHelper.logContext(context, TAG, "Scheduling work");

        if (loaderClassName == null) {
            LoggerHelper.logContext(context, TAG, "Loaded class name empty, worker is not scheduled");
        } else {

            Data inputData = (new Data.Builder()).putString(WORKER_PARAM_LOADER_NAME, loaderClassName).build();
            Constraints constraints = (new Constraints.Builder())
                    .setRequiredNetworkType(NetworkType.NOT_ROAMING)
                    .build();

            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(LoaderWorker.class)
                    .addTag(WORKER_TAG)
                    .setInputData(inputData)
                    .setConstraints(constraints)
                    .build();

            WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                            WORKER_NAME,
                            ExistingWorkPolicy.KEEP,
                            oneTimeWorkRequest
                    );


            LoggerHelper.logContext(context, TAG, "Work scheduled");
        }
    }

    public static void cancelAllWorkers(Context context) {
        // https://stackoverflow.com/questions/54456396/android-workmanager-doesnt-trigger-one-of-the-two-scheduled-workers
        LoggerHelper.logContext(context, TAG, "Cancelling old works with tag " + WORKER_TAG);
        try {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORKER_TAG).getResult().get();
            WorkManager.getInstance(context).pruneWork().getResult().get();
            LoggerHelper.logContext(context, TAG, "Works cancelled successfully");
        } catch (InterruptedException | ExecutionException e) {
            LoggerHelper.logContext(context, TAG, "Error cancelling old works:" + e);
        }
    }

    public static void scheduleWorker(Context context, String loaderClassName) {
        internalScheduleWorker(context, loaderClassName);
    }

    public static boolean isRunning(Context context) {
        ListenableFuture<List<WorkInfo>> workInfos = WorkManager.getInstance(context).getWorkInfosByTag(WORKER_TAG);
        try {
            return !workInfos.get().isEmpty() && !workInfos.get().get(0).getState().isFinished();
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

    public static LiveData<List<WorkInfo>> getWorkInfosLiveData(Context context) {
        return WorkManager.getInstance(context).getWorkInfosByTagLiveData(WORKER_TAG);
    }

    public static String getLoaderClassName(Data data) {
        return data.getString(WORKER_PARAM_LOADER_NAME);
    }

    public static String getErrorMessage(Data data) {
        return data.getString(WORKER_RESULT_ERROR_MESSAGE);
    }
}
