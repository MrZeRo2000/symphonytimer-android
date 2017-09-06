package com.romanpulov.symphonytimer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.romanpulov.symphonytimer.helper.ActivityWakeHelper;
import com.romanpulov.symphonytimer.helper.LoggerHelper;

/**
 * Created by romanpulov on 24.05.2017.
 */

public class AdvanceAlarmBroadcastReceiver extends BroadcastReceiver {
    private static void logContext(Context context, String message) {
        LoggerHelper.logContext(context, "AdvanceAlarmBroadcastReceiver", message);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        logContext(context, "onReceive");

        long wakeDuration;
        long wakeTarget = intent.getLongExtra(ActivityWakeHelper.WAKE_TARGET_EXTRA_NAME, 0);
        if (wakeTarget != 0) {
            // added offset for more reliable wake
            logContext(context, "wake target found");
            wakeDuration = wakeTarget - System.currentTimeMillis() + ActivityWakeHelper.WAKE_LOCK_OFFSET;
        } else {
            logContext(context, "wake target not found");
            wakeDuration = ActivityWakeHelper.WAKE_LOCK_DURATION;
        }

        logContext(context, "waking with duration " + wakeDuration);
        ActivityWakeHelper.wakePartial(context, wakeDuration);
    }
}
