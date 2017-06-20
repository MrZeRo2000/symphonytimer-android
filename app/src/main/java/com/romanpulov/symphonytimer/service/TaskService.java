package com.romanpulov.symphonytimer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.activity.MainActivity;
import com.romanpulov.symphonytimer.helper.ActivityWakeHelper;
import com.romanpulov.symphonytimer.helper.AlarmManagerHelper;
import com.romanpulov.symphonytimer.helper.DateFormatterHelper;
import com.romanpulov.symphonytimer.helper.LoggerHelper;
import com.romanpulov.symphonytimer.helper.MediaPlayerHelper;
import com.romanpulov.symphonytimer.helper.NotificationHelper;
import com.romanpulov.symphonytimer.helper.VibratorHelper;
import com.romanpulov.symphonytimer.helper.WakeConfigHelper;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.DMTasks;
import com.romanpulov.symphonytimer.model.DMTasksStatus;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.romanpulov.symphonytimer.model.DMTasksStatus.STATUS_EVENT_TO_COMPLETED;
import static com.romanpulov.symphonytimer.model.DMTasksStatus.STATUS_EVENT_TO_NOT_COMPLETED;
import static com.romanpulov.symphonytimer.model.DMTasksStatus.STATUS_EVENT_UPDATE_COMPLETED;

public class TaskService extends Service implements Runnable {
    private void log(String message) {
        LoggerHelper.logContext(this, "TaskService", message);
    }

    private static void unconditionalLog(String message) {
        //enable for debug only
        //LoggerHelper.unconditionalLog("TaskService", message);
    }

    public static final String PREFS_NAME = "TaskServicePrefs";

    public static final int MSG_UPDATE_DM_TASKS = 1;
    public static final int MSG_UPDATE_DM_PROGRESS = 2;
    public static final int MSG_QUERY_DM_TASKS = 3;
    public static final int MSG_TASK_TO_COMPLETED = 4;
    public static final int MSG_TASK_UPDATE_COMPLETED = 5;
    public static final int MSG_TASK_TO_NOT_COMPLETED = 6;

    public static final String ACTION_STOP_SERVICE = "StopService";

    private Messenger mClientMessenger;
    private MediaPlayerHelper mMediaPlayerHelper;

    /**
     * Handler of incoming messages from clients.
     */
    static class IncomingHandler extends Handler {
        private final TaskService mHostReference;

        IncomingHandler(TaskService host) {
            mHostReference = host;
        }

        @Override
        public void handleMessage(Message msg) {
            TaskService hostService = mHostReference;
            if (hostService != null) {
                switch (msg.what) {
                    case MSG_UPDATE_DM_TASKS:
                        unconditionalLog("handleMessage update dm tasks");
                        hostService.updateDMTasks((DMTasks) msg.getData().getParcelable(DMTasks.class.toString()));
                        hostService.mClientMessenger = msg.replyTo;
                        break;
                    case MSG_TASK_TO_COMPLETED:
                        unconditionalLog("handleMessage to completed");

                        hostService.wakeAndStartActivity(MainActivity.class);

                        //play sound
                        hostService.mMediaPlayerHelper.startSoundFile(hostService.mDMTasksStatus.getFirstTaskItemCompleted().getSoundFile());

                        //vibrate
                        VibratorHelper.vibrate(hostService);
                        break;
                    case MSG_TASK_UPDATE_COMPLETED:
                        unconditionalLog("handleMessage update completed");

                        //stop sound
                        hostService.mMediaPlayerHelper.stop();

                        //play sound
                        hostService.mMediaPlayerHelper.startSoundFile(hostService.mDMTasksStatus.getFirstTaskItemCompleted().getSoundFile());
                        break;
                    case MSG_TASK_TO_NOT_COMPLETED:
                        unconditionalLog("handleMessage to not completed");
                        //stop vibrating
                        VibratorHelper.cancel(hostService);
                        //stop sound
                        hostService.mMediaPlayerHelper.stop();
                        break;
                    case MSG_QUERY_DM_TASKS:
                        hostService.mClientMessenger = msg.replyTo;
                        if (hostService.mClientMessenger != null) {
                            Message outMsg = Message.obtain(null, TaskService.MSG_UPDATE_DM_TASKS, 0, 0);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(DMTasks.class.toString(), hostService.mDMTasks.createParcelableCopy());
                            outMsg.setData(bundle);
                            try {
                                hostService.mClientMessenger.send(outMsg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                }
            } else
                super.handleMessage(msg);
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler(this));

    final AlarmManagerHelper mAlarm = new AlarmManagerHelper();

    private DMTasks mDMTasks;
    private DMTasksStatus mDMTasksStatus;

    private synchronized void updateDMTasks(DMTasks value) {
        log("UpdateDMTasks: new value = " + value);

        // set new value
        mDMTasks = value;

        //persist to prefs
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(DMTasks.class.toString(), mDMTasks.toJSONString()).commit();

        if (mDMTasksStatus == null) {
            //first assignment or after restore with redelivered intent
            log("UpdateDMTasks: null status, creating new");
            if (mDMTasks.getFirstTaskItemCompleted() != null) {
                log("UpdateDMTasks: exists completed");
                processStatusChangeEvent(STATUS_EVENT_TO_COMPLETED);
            }
            mDMTasksStatus = new DMTasksStatus(mDMTasks);
        }
        else {
            int statusChangeEvent = mDMTasksStatus.getStatusChangeEvent(mDMTasks);
            log("UpdateDMTasks: " + DMTasksStatus.statusEventAsString(statusChangeEvent));
            processStatusChangeEvent(statusChangeEvent);
        }

        //set new alarm
        if (mDMTasks.size() > 0) {
            long triggerTime = mDMTasks.getFirstTriggerAtTime();
            log("firstTriggerAtTime = " + triggerTime + " " + DateFormatterHelper.formatLog(triggerTime));
            if (triggerTime < Long.MAX_VALUE) {
                log("setting new alarm to " + triggerTime + " " + DateFormatterHelper.formatLog(triggerTime));
                mAlarm.setExactTimer(this, triggerTime);

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
        }  else {
            //cancel old alarm
            log("cancelling alarm: mDMTasks.size() = 0");
            mAlarm.cancelAlarms(this);
        }
    }

    private void processStatusChangeEvent(int event) {
        int messageId;
        switch (event) {
            case STATUS_EVENT_TO_COMPLETED:
                messageId = MSG_TASK_TO_COMPLETED;
                break;
            case STATUS_EVENT_UPDATE_COMPLETED:
                messageId = MSG_TASK_UPDATE_COMPLETED;
                break;
            case STATUS_EVENT_TO_NOT_COMPLETED:
                messageId = MSG_TASK_TO_NOT_COMPLETED;
                break;
            default:
                messageId = 0;
        }

        if (messageId != 0) {
            Message msg = Message.obtain(null, messageId, 0, 0);
            try {
                log("processStatusChangeEvent: sending " + messageId);
                mMessenger.send(msg);
            } catch (RemoteException e) {
                log("failed to send: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private final ScheduledThreadPoolExecutor mScheduleExecutor = new ScheduledThreadPoolExecutor(2);
    private ScheduledFuture<?> mScheduleExecutorTask;

    /**
     * Wakes up the device and starts activity
     * @param activityClass activity to start
     */
    private void wakeAndStartActivity(Class<?> activityClass) {
        ActivityWakeHelper.wakeAndStartActivity(this, activityClass);
    }

    public TaskService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand: dmTasks = " + mDMTasks + ", status = " + mDMTasksStatus);

        if ((intent != null) && (intent.getAction() != null) && (intent.getAction().equals(ACTION_STOP_SERVICE))) {
            log("stopping executor");
            mScheduleExecutor.shutdown();

            log("waiting for termination");
            try {
                if (!mScheduleExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log("task did not terminate in 60 seconds");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            log("stop foreground");
            stopForeground(true);

            stopSelf();
        } else {

            if (mMediaPlayerHelper == null)
                mMediaPlayerHelper = new MediaPlayerHelper(this);

            DMTasks newDMTasks;
            Parcelable newParcelableTasks;
            if ((intent != null) && (intent.getExtras() != null) && ((newParcelableTasks = intent.getExtras().getParcelable(DMTasks.class.toString()))) != null) {
                newDMTasks = (DMTasks) newParcelableTasks;
                log("onStartCommand: dmTasks found: " + newDMTasks);
            } else {
                newDMTasks = DMTasks.fromJSONString(getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(DMTasks.class.toString(), ""));
                log("onStartCommand: dmTasks from Prefs: " + newDMTasks);
            }

            updateDMTasks(newDMTasks);

            if (mDMTasks != null) {
                startForeground(NotificationHelper.ONGOING_NOTIFICATION_ID, NotificationHelper.getInstance(this).getNotification(mDMTasks));

                if (mScheduleExecutorTask == null)
                    mScheduleExecutorTask = mScheduleExecutor.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
            }
        }

        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mClientMessenger = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        log("destroying service");
        mAlarm.cancelAlarms(this);
        VibratorHelper.cancel(this);
        if (mMediaPlayerHelper != null)
            mMediaPlayerHelper.stop();

        super.onDestroy();
    }

    @Override
    public void run() {
        try {
            log("run " + DateFormatterHelper.formatLog(System.currentTimeMillis()) + ", dmTasks = " + mDMTasks + ", status = " + mDMTasksStatus + ",alarm=" + mAlarm);

            log("NotificationHelper notify");
            NotificationHelper.getInstance(this).notify(mDMTasks);

            int statusChangeEvent = mDMTasksStatus.getStatusChangeEvent(mDMTasks);
            //log(DMTasksStatus.statusEventAsString(statusChangeEvent));

            // log to completed event
            if (statusChangeEvent == STATUS_EVENT_TO_COMPLETED) {
                DMTaskItem dmTaskItem = mDMTasks.getFirstTaskItemCompleted();
                if (dmTaskItem != null) {
                    log("Due time: " +
                            DateFormatterHelper.formatLog(dmTaskItem.getTriggerAtTime()) +
                            ", real time: " +
                            DateFormatterHelper.formatLog(System.currentTimeMillis())
                    );
                }
            }

            processStatusChangeEvent(statusChangeEvent);

            /*
            int newDMTasksStatus = mDMTasks.getStatus();
            DMTaskItem newDMTasksItemCompleted = mDMTasks.getFirstTaskItemCompleted();
            log ("newDMTasksItemCompleted = " + newDMTasksItemCompleted);

            //detect message to generate
            Message msg = null;
            if ((mDMTasksStatus != DMTasks.STATUS_COMPLETED) && (newDMTasksStatus == DMTasks.STATUS_COMPLETED)) {
                log("run to completed");
                // to completed
                msg = Message.obtain(null, TaskService.MSG_TASK_TO_COMPLETED, 0, 0);
            } else if ((mDMTasksStatus == DMTasks.STATUS_COMPLETED) && (newDMTasksStatus == DMTasks.STATUS_COMPLETED) && (newDMTasksItemCompleted.getId() != mDMTasks.getFirstTaskItemCompleted().getId())) {
                log("run update completed");
                //update completed
                msg = Message.obtain(null, TaskService.MSG_TASK_UPDATE_COMPLETED, 0, 0);
            } else if ((mDMTasksStatus == DMTasks.STATUS_COMPLETED) && (newDMTasksStatus != DMTasks.STATUS_COMPLETED)) {
                log("run to not completed");
                //to not completed
                msg = Message.obtain(null, TaskService.MSG_TASK_TO_NOT_COMPLETED, 0, 0);
            }

            if (msg != null) {
                try {
                    mMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            mDMTasksStatus = newDMTasksStatus;
            mDMTasksFirstItemCompleted = newDMTasksItemCompleted;
            */

            if (mClientMessenger != null) {
                Message msg = Message.obtain(null, TaskService.MSG_UPDATE_DM_PROGRESS, 0, 0);
                try {
                    mClientMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
