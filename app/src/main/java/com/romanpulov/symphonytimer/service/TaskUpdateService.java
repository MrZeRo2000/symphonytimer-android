package com.romanpulov.symphonytimer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.activity.MainActivity;
import com.romanpulov.symphonytimer.helper.*;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.TimerViewModel;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import static com.romanpulov.symphonytimer.common.NotificationRepository.NOTIFICATION_ID_ONGOING;

public class TaskUpdateService extends Service {
    private static final String TAG = TaskUpdateService.class.getSimpleName();
    private static final long WAKE_ADVANCE_MILLIS = 1000;

    private void log(String message) {
        LoggerHelper.logContext(this, "TaskUpdateService", message);
    }

    private TimerViewModel model;
    private Observer<Pair<Integer, Integer>> mTaskStatusObserver;
    private final ScheduledThreadPoolExecutor mScheduleExecutor = new ScheduledThreadPoolExecutor(1);
    private final AlarmManagerHelper mAlarm = new AlarmManagerHelper();
    private TimerSignalHelper mTimerSignalHelper;
    private int mAutoTimerDisableInterval = 0;

    public TaskUpdateService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        model = TimerViewModel.getInstance(getApplication());
        mTimerSignalHelper = new TimerSignalHelper(getApplicationContext());

        mTaskStatusObserver = taskStatus -> {
            if ((taskStatus.first != TimerViewModel.TASKS_STATUS_IDLE) &&
                    (taskStatus.second == TimerViewModel.TASKS_STATUS_IDLE)) {
                log("Task status changed to idle, stopping the service");

                mTimerSignalHelper.stop();
                mAlarm.cancelAlarms(this);
                stopSelf();
            } else if (taskStatus.second == TimerViewModel.TASKS_STATUS_PROCESSING) {
                log("Task status changed to processing");

                updateAlarm();
                mTimerSignalHelper.stop();
            } else if (taskStatus.second == TimerViewModel.TASKS_STATUS_UPDATE_PROCESSING) {
                log("Task status changed to update processing");

                updateAlarm();
            } else if (taskStatus.second == TimerViewModel.TASKS_STATUS_COMPLETED) {
                Log.d(TAG, "task status changed to COMPLETED, need to do something ...");

                ActivityWakeHelper.wakeAndStartActivity(this, MainActivity.class);

                Log.d(TAG, "Looking for first completed task");
                DMTaskItem firstTaskCompleted = TimerViewModel.getFirstTaskItemCompleted(model.getDMTaskMap().getValue());
                if (firstTaskCompleted != null) {
                    Log.d(TAG, "First task completed:" + firstTaskCompleted);
                    mTimerSignalHelper.setSoundFileName(firstTaskCompleted.getSoundFileName());
                    mTimerSignalHelper.start();

                    log("Due time: " +
                            DateFormatterHelper.formatLog(firstTaskCompleted.getTriggerAtTime()) +
                            ", real time: " +
                            DateFormatterHelper.formatLog(System.currentTimeMillis())
                    );

                    saveHistory();
                }

                updateAlarm();
            } else if (taskStatus.second == TimerViewModel.TASKS_STATUS_UPDATE_COMPLETED) {
                Log.d(TAG, "task status changed to UPDATE_COMPLETED");

                DMTaskItem firstTaskCompleted = TimerViewModel.getFirstTaskItemCompleted(model.getDMTaskMap().getValue());
                if (firstTaskCompleted != null) {
                    mTimerSignalHelper.setMultiple();
                    mTimerSignalHelper.changeSoundFileName(firstTaskCompleted.getSoundFileName());

                    log("Due time: " +
                            DateFormatterHelper.formatLog(firstTaskCompleted.getTriggerAtTime()) +
                            ", real time: " +
                            DateFormatterHelper.formatLog(System.currentTimeMillis())
                    );

                    saveHistory();
                }

                updateAlarm();
            }
        };

        model.getTaskStatusChange().observeForever(mTaskStatusObserver);
    }

    private void saveHistory() {
        model.getTaskItemsCompleted(model.getDMTaskMap().getValue())
                .forEach(v -> DBHelper.getInstance(this).insertTimerHistory(v));
    }

    private void updateAlarm() {
        long triggerTime = model.getFirstTriggerAtTime();
        log("firstTriggerAtTime = " + triggerTime + " " + DateFormatterHelper.formatLog(triggerTime));

        if (triggerTime < Long.MAX_VALUE) {
            // wake a bit earlier
            long wakeTime = triggerTime - WAKE_ADVANCE_MILLIS;
            log("setting new alarm to " + wakeTime + " " + DateFormatterHelper.formatLog(wakeTime));
            mAlarm.setExactTimer(this, wakeTime);

            WakeConfigHelper wakeConfigHelper = new WakeConfigHelper(getApplicationContext());
            if (wakeConfigHelper.isValidConfig()) {

                log("wake before config: " + wakeConfigHelper.getWakeBeforeTime());
                long wakeBefore = triggerTime - wakeConfigHelper.getWakeBeforeTime();

                log("wake before: " + DateFormatterHelper.formatLog(wakeBefore));
                mAlarm.setAdvanceTimer(this, wakeBefore, triggerTime);
            } else {
                log("wake config is invalid");
            }
        } else {
            log("cancelling alarm: triggerTime = Long.MAX_VALUE");
            mAlarm.cancelAlarms(this);
        }
    }

    private final Runnable updateTask = () -> {
        Log.d(TAG, "updateTask");

        DMTaskItem firstTaskCompleted = TimerViewModel.getFirstTaskItemCompleted(model.getDMTaskMap().getValue());
        if ((firstTaskCompleted != null) &&
                (model.getDMTaskMap().getValue() != null) &&
                (model.getDMTaskMap().getValue().size() == 1) &&
                (!mTimerSignalHelper.isMultiple())) {

            // get auto timer disable interval, priority for individual setting
            int autoTimerDisableInterval = firstTaskCompleted.getAutoTimerDisableInterval();
            if (autoTimerDisableInterval == 0)
                autoTimerDisableInterval = mAutoTimerDisableInterval;
            log("Auto timer disable interval: " + autoTimerDisableInterval);

            if ((autoTimerDisableInterval > 0) && (mTimerSignalHelper.getDurationSeconds() >= autoTimerDisableInterval)) {
                model.removeTask(firstTaskCompleted.getId());
            }
        } else {
            model.updateTasks();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (model.getDMTaskMap().getValue() != null) {
            log("Tasks are available, starting");
            startForeground(NOTIFICATION_ID_ONGOING,
                    ProgressNotificationHelper.getInstance(this).getNotification(
                            model.getTaskTitles(),
                            model.getExecutionPercent()));
            mScheduleExecutor.scheduleWithFixedDelay(updateTask, 0, 1, TimeUnit.SECONDS);
        }

        String prefAutoTimerDisable = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_auto_timer_disable", getString(R.string.pref_wake_before_default));
        mAutoTimerDisableInterval = Integer.parseInt(prefAutoTimerDisable);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Stopping service");

        mAlarm.cancelAlarms(this);

        if (mTimerSignalHelper != null) {
            mTimerSignalHelper.stop();
        }

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