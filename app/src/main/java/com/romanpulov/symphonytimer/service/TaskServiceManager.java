package com.romanpulov.symphonytimer.service;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.romanpulov.symphonytimer.helper.LoggerHelper;
import com.romanpulov.symphonytimer.model.DMTasks;

import java.util.List;

/**
 * Utility class for interaction with Service
 * Used by main activity
 */
public class TaskServiceManager {
    private static void log(String message) {
        LoggerHelper.log("TaskServiceManager", message);
    }

    /**
     * Messenger for communicating with the service.
     */
    private Messenger mService = null;

    /**
     * Flag indicating whether we have called bind on the service.
     */
    private boolean mServiceBound;

    public boolean isServiceBound() {
        return mServiceBound;
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger;

    final Context mContext;

    /**
     * Class for interacting with the main interface of the service.
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mServiceBound = true;

            queryServiceDMTasks();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mServiceBound = false;
        }
    };

    public TaskServiceManager(Context context, Handler messageHandler) {
        mContext = context;
        mMessenger = new Messenger(messageHandler);
    }

    public void updateServiceDMTasks(DMTasks tasks) {
        log("To service: updateServiceDMTasks:" + tasks);
        Message msg = Message.obtain(null, TaskService.MSG_UPDATE_DM_TASKS, 0, 0);
        msg.replyTo = mMessenger;
        DMTasks newTasks = tasks.createParcelableCopy();
        Bundle bundle = new Bundle();
        bundle.putParcelable(DMTasks.class.toString(), newTasks);
        msg.setData(bundle);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void queryServiceDMTasks() {
        log("queryServiceDMTasks");
        Message msg = Message.obtain(null, TaskService.MSG_QUERY_DM_TASKS, 0, 0);
        msg.replyTo = mMessenger;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void updateServiceTasks(DMTasks tasks) {
        Intent serviceIntent = new Intent(mContext, TaskService.class);

        //no more tasks
        if (mServiceBound && tasks.size() == 0) {
            log("updateServiceTasks: no more tasks");
            mContext.unbindService(mConnection);
            mServiceBound = false;

            //another idea of stopping service:
            serviceIntent.setAction(TaskService.ACTION_STOP_SERVICE);
            mContext.startService(serviceIntent);

            //mContext.stopService(serviceIntent);
            return;
        }

        //tasks, not bound
        if ((!mServiceBound) && tasks.size() > 0) {
            log("updateServiceTasks: tasks, not bound");
            DMTasks newTasks = tasks.createParcelableCopy();
            serviceIntent.putExtra(DMTasks.class.toString(), newTasks);
            mContext.startService(serviceIntent);
            mContext.bindService(new Intent(mContext, TaskService.class), mConnection, Context.BIND_AUTO_CREATE);
            return;
        }

        //tasks, bound
        if (mServiceBound && tasks.size() > 0) {
            log("updateServiceTasks: tasks, bound");
            updateServiceDMTasks(tasks);
            return;
        }

        if (isTaskServiceRunning()) {
            log("updateServiceTasks: TaskServiceRunning");
            mContext.startService(serviceIntent);
            mContext.bindService(new Intent(mContext, TaskService.class), mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public boolean isTaskServiceRunning () {
        return isServiceRunning(TaskService.class.getName());
    }

    private boolean isServiceRunning(String serviceClassName) {
        final ActivityManager activityManager = (ActivityManager) mContext.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)) {
                return true;
            }
        }
        return false;
    }

    public void unbindService() {
        if (mServiceBound)
            mContext.unbindService(mConnection);
    }
}
