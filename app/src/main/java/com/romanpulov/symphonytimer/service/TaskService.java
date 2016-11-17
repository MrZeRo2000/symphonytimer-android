package com.romanpulov.symphonytimer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import com.romanpulov.symphonytimer.activity.MainActivity;
import com.romanpulov.symphonytimer.helper.MediaPlayerHelper;
import com.romanpulov.symphonytimer.helper.NotificationHelper;
import com.romanpulov.symphonytimer.helper.VibratorHelper;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.DMTasks;
import com.romanpulov.symphonytimer.model.DMTasksStatus;
import com.romanpulov.symphonytimer.utils.AlarmManagerBroadcastReceiver;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.romanpulov.symphonytimer.model.DMTasksStatus.STATUS_EVENT_TO_COMPLETED;
import static com.romanpulov.symphonytimer.model.DMTasksStatus.STATUS_EVENT_TO_NOT_COMPLETED;
import static com.romanpulov.symphonytimer.model.DMTasksStatus.STATUS_EVENT_UPDATE_COMPLETED;

public class TaskService extends Service implements Runnable {
    private static void log(String message) {
        Log.d("TaskService", message);
    }

    public static final int MSG_UPDATE_DM_TASKS = 1;
    public static final int MSG_UPDATE_DM_PROGRESS = 2;
    public static final int MSG_QUERY_DM_TASKS = 3;
    public static final int MSG_TASK_TO_COMPLETED = 4;
    public static final int MSG_TASK_UPDATE_COMPLETED = 5;
    public static final int MSG_TASK_TO_NOT_COMPLETED = 6;

    private Messenger mClientMessenger;

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
                        log("handleMessage update dm tasks");
                        hostService.updateDMTasks((DMTasks) msg.getData().getParcelable(DMTasks.class.toString()));
                        hostService.mClientMessenger = msg.replyTo;
                        break;
                    case MSG_TASK_TO_COMPLETED:
                        log("handleMessage to completed");
                        //hostService.wakeAndStartActivity(MainActivity.class);

                        //play sound
                        MediaPlayerHelper.getInstance(mHostReference).startSoundFile(mHostReference.mDMTasksStatus.getFirstTaskItemCompleted().getSoundFile());

                        //vibrate
                        VibratorHelper.vibrate(mHostReference);
                        break;
                    case MSG_TASK_UPDATE_COMPLETED:
                        log("handleMessage update completed");
                        //stop sound
                        MediaPlayerHelper.getInstance(mHostReference).stop();
                        //play sound
                        MediaPlayerHelper.getInstance(mHostReference).startSoundFile(mHostReference.mDMTasksStatus.getFirstTaskItemCompleted().getSoundFile());
                        break;
                    case MSG_TASK_TO_NOT_COMPLETED:
                        log("handleMessage to not completed");
                        //stop vibrating
                        VibratorHelper.cancel(mHostReference);
                        //stop sound
                        MediaPlayerHelper.getInstance(mHostReference).stop();
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

    final AlarmManagerBroadcastReceiver mAlarm = new AlarmManagerBroadcastReceiver();

    private DMTasks mDMTasks;
    private DMTasksStatus mDMTasksStatus;

    private synchronized void updateDMTasks(DMTasks value) {
        //cancel old alarm
        mAlarm.cancelAlarm(this, 0);

        mDMTasks = value;
        if (mDMTasksStatus == null)
            mDMTasksStatus = new DMTasksStatus(mDMTasks);
        else
            processStatusChangeEvent(mDMTasksStatus.getStatusChangeEvent(mDMTasks));

        //set new alarm
        if (mDMTasks.size() > 0) {
            long triggerTime = mDMTasks.getFirstTriggerAtTime();
            log("firstTriggerAtTime = " + triggerTime);
            if (triggerTime < Long.MAX_VALUE) {
                log("setting new alarm to " + triggerTime);
                mAlarm.setOnetimeTimer(this, 0, triggerTime);
            }
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
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private ScheduledThreadPoolExecutor mScheduleExecutor = new ScheduledThreadPoolExecutor(2);
    private ScheduledFuture<?> mScheduleExecutorTask;

    /**
     * Wakes up the device and starts activity
     * @param activityClass activity to start
     */
    private void wakeAndStartActivity(Class<?> activityClass) {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        @SuppressWarnings("deprecation")
        PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, AlarmManagerBroadcastReceiver.WAKE_LOG_TAG);

        wl.acquire();

        try {
            Intent activityIntent = new Intent(this, MainActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(activityIntent);
        } finally {
            wl.release();
        }
    }

    public TaskService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getExtras() != null) {
            updateDMTasks((DMTasks)intent.getExtras().getParcelable(DMTasks.class.toString()));
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
        MediaPlayerHelper.getInstance(this).release();
        stopForeground(true);
        if (mScheduleExecutorTask != null) {
            mScheduleExecutorTask.cancel(true);
            mScheduleExecutorTask = null;
        }

        super.onDestroy();
    }

    @Override
    public void run() {
        try {
            log("run " + System.currentTimeMillis());

            NotificationHelper.getInstance(this).notify(mDMTasks);

            processStatusChangeEvent(mDMTasksStatus.getStatusChangeEvent(mDMTasks));

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
