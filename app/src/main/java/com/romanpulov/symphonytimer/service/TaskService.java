package com.romanpulov.symphonytimer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.romanpulov.symphonytimer.model.DMTasks;

public class TaskService extends Service {

    private DMTasks mDMTasks;

    public TaskService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getExtras() != null) {
            mDMTasks = intent.getExtras().getParcelable(DMTasks.class.toString());
            Toast.makeText(this, "service starting : " + mDMTasks, Toast.LENGTH_SHORT).show();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service stopping", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
}
