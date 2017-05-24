package com.romanpulov.symphonytimer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.romanpulov.symphonytimer.helper.ActivityWakeHelper;
import com.romanpulov.symphonytimer.helper.LoggerHelper;

/**
 * Created by romanpulov on 24.05.2017.
 */

public class RepeatingAlarmManagerBroadcastReceiver extends BroadcastReceiver {
    private static void logContext(Context context, String message) {
        LoggerHelper.logContext(context, "RepeatingAlarmManagerBroadcastReceiver", message);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        logContext(context, "onReceive");
        ActivityWakeHelper.wake(context);
    }
}
