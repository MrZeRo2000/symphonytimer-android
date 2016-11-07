package com.romanpulov.symphonytimer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import com.romanpulov.symphonytimer.activity.MainActivity;
import com.romanpulov.symphonytimer.helper.NotificationHelper;
import com.romanpulov.symphonytimer.model.DMTasks;
import com.romanpulov.symphonytimer.utils.AlarmManagerBroadcastReceiver;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskService extends Service implements Runnable {

    private void log(String message) {
        Log.d("TaskService", message);
    }

    public static final int MSG_UPDATE_DM_TASKS = 1;
    public static final int MSG_UPDATE_DM_PROGRESS = 2;
    public static final int MSG_TASK_COMPLETED = 3;

    private Messenger mClientMessenger;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_DM_TASKS:
                    mDMTasks = msg.getData().getParcelable(DMTasks.class.toString());
                    if (mDMTasks != null)
                        mDMTasksStatus = mDMTasks.getStatus();
                    mClientMessenger = msg.replyTo;
                    break;
                case MSG_TASK_COMPLETED:
                    wakeAndStartActivity(MainActivity.class);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private DMTasks mDMTasks;
    private int mDMTasksStatus;

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
        log("Bind");
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("startCommand");
        if (intent.getExtras() != null) {
            mDMTasks = intent.getExtras().getParcelable(DMTasks.class.toString());
            if (mDMTasks != null) {
                mDMTasksStatus = mDMTasks.getStatus();

                startForeground(NotificationHelper.ONGOING_NOTIFICATION_ID, NotificationHelper.getInstance(this).getNotification(mDMTasks));

                log("startScheduler");
                if (mScheduleExecutorTask == null)
                    mScheduleExecutorTask = mScheduleExecutor.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
            }
        }

        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        log("unBind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        if (mScheduleExecutorTask != null) {
            log("stopScheduler");
            mScheduleExecutorTask.cancel(true);
            mScheduleExecutorTask = null;
        }

        super.onDestroy();
    }

    @Override
    public void run() {
        log("run, tasks: " + mDMTasks.size());

        try {
            NotificationHelper.getInstance(this).notify(mDMTasks);

            int newDMTasksStatus = mDMTasks.getStatus();
            if ((mDMTasksStatus != DMTasks.STATUS_COMPLETED) && (newDMTasksStatus == DMTasks.STATUS_COMPLETED)) {
                log("run, completed");
                Message msg = Message.obtain(null, TaskService.MSG_TASK_COMPLETED, 0, 0);
                try {
                    mMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                mDMTasksStatus = newDMTasksStatus;
            }

            if (mClientMessenger != null) {
                Message msg = Message.obtain(null, TaskService.MSG_UPDATE_DM_PROGRESS, 0, 0);
                try {
                    log("run, updating client");
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
