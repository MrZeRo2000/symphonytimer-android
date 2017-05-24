package com.romanpulov.symphonytimer.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.romanpulov.symphonytimer.receiver.OneTimeAlarmManagerBroadcastReceiver;
import com.romanpulov.symphonytimer.receiver.RepeatingAlarmManagerBroadcastReceiver;

/**
 * Helper class for AlarmManager handling
 * Created by romanpulov on 24.05.2017.
 */

public class AlarmManagerHelper {
    private static void logContext(Context context, String message) {
        LoggerHelper.logContext(context, "AlarmManagerHelper", message);
    }

    public static int ALARM_TYPE_ONETIME = 0;
    public static int ALARM_TYPE_REPEATING = 1;

    private void cancelOneTimeAlarm(Context context) {
        logContext(context, "cancelOneTimeAlarm");
        Intent intent = new Intent(context, OneTimeAlarmManagerBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, ALARM_TYPE_ONETIME, intent, PendingIntent.FLAG_NO_CREATE);
        if (null != sender) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(sender);
        }
    }

    private void cancelRepeatingTimeAlarm(Context context) {
        logContext(context, "cancelRepeatingTimeAlarm");
        Intent intent = new Intent(context, RepeatingAlarmManagerBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, ALARM_TYPE_REPEATING, intent, PendingIntent.FLAG_NO_CREATE);
        if (null != sender) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(sender);
        }
    }

    public void cancelAlarms(Context context) {
        cancelOneTimeAlarm(context);
        cancelRepeatingTimeAlarm(context);
    }

    public void setRepeatingTimer(Context context, long intervalMillis) {
        logContext(context, "setRepeatingTimer to " + intervalMillis);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, RepeatingAlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, AlarmManagerHelper.ALARM_TYPE_REPEATING, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMillis, pi);
    }

    public void setOnetimeTimer(Context context, long triggerAt){
        logContext(context, "setOnetimeTimer to " + DateFormatterHelper.format(triggerAt));
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, OneTimeAlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, AlarmManagerHelper.ALARM_TYPE_ONETIME, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }
}
