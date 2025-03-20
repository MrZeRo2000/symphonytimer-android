package com.romanpulov.symphonytimer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import androidx.lifecycle.Observer;
import com.romanpulov.symphonytimer.helper.ProgressNotificationHelper;
import com.romanpulov.symphonytimer.model.TimerViewModel;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_ONGOING;

public class TaskUpdateService extends Service {
    private static final String TAG = TaskUpdateService.class.getSimpleName();

    private TimerViewModel model;
    private Observer<Pair<Integer, Integer>> mTaskStatusObserver;
    private final ScheduledThreadPoolExecutor mScheduleExecutor = new ScheduledThreadPoolExecutor(1);

    public TaskUpdateService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        model = TimerViewModel.getInstance(getApplication());

        mTaskStatusObserver = taskStatus -> {
            if ((taskStatus.first != TimerViewModel.TASKS_STATUS_IDLE) &&
                    (taskStatus.second == TimerViewModel.TASKS_STATUS_IDLE)) {
                Log.d(TAG, "Task status changed to idle, stopping the service");
                stopSelf();
            }
        };

        model.getTaskStatusChange().observeForever(mTaskStatusObserver);
    }

    private final Runnable updateTask = () -> {
        Log.d(TAG, "updateTask");
        model.updateTasks();
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (model.getDMTaskMap().getValue() != null) {
            Log.d(TAG, "Tasks are available, starting");
            startForeground(NOTIFICATION_ID_ONGOING,
                    ProgressNotificationHelper.getInstance(this).getNotification(
                            model.getTaskTitles(),
                            model.getExecutionPercent()));
            mScheduleExecutor.scheduleWithFixedDelay(updateTask, 0, 1, TimeUnit.SECONDS);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Stopping service");

        super.onDestroy();

        if ((model != null) && mTaskStatusObserver != null) {
            Log.d(TAG, "Removing observer from model");
            model.getTaskStatusChange().removeObserver(mTaskStatusObserver);
        }

        Log.d(TAG, "stopping executor");
        mScheduleExecutor.shutdown();

        Log.d(TAG, "waiting for termination");
        try {
            if (!mScheduleExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                Log.d(TAG, "task did not terminate in 60 seconds");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "executor awaitTermination error", e);
        }

        Log.d(TAG, "stop foreground");
        stopForeground(STOP_FOREGROUND_REMOVE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}