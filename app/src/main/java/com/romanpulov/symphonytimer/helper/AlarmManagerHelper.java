package com.romanpulov.symphonytimer.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.romanpulov.symphonytimer.receiver.ExactAlarmBroadcastReceiver;
import com.romanpulov.symphonytimer.receiver.AdvanceAlarmBroadcastReceiver;

import static com.romanpulov.symphonytimer.helper.ActivityWakeHelper.WAKE_TARGET_EXTRA_NAME;

/**
 * Helper class for AlarmManager handling
 * Created by romanpulov on 24.05.2017.
 */

public class AlarmManagerHelper {
    private static void logContext(Context context, String message) {
        LoggerHelper.logContext(context, "AlarmManagerHelper", message);
    }

    public static int ALARM_TYPE_EXACT = 0;
    public static int ALARM_TYPE_REPEATING = 1;
    public static int ALARM_TYPE_ADVANCE = 2;

    private void cancelTimeAlarm(Context context, Class<?> intentClass, int requestCode) {
        Intent intent = new Intent(context, intentClass);
        PendingIntent sender = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE);
        if (null != sender) {
            logContext(context, intentClass.getName() + ": cancelling alarm");
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(sender);
        }
    }

    public void cancelAlarms(Context context) {
        cancelTimeAlarm(context, ExactAlarmBroadcastReceiver.class, ALARM_TYPE_EXACT);
        cancelTimeAlarm(context, AdvanceAlarmBroadcastReceiver.class, ALARM_TYPE_ADVANCE);
    }

    private void setRepeatingTimer(Context context, long triggerAt, long interval) {
        logContext(context, "setRepeatingTimer to " + interval + " triggering at " + DateFormatterHelper.formatLog(triggerAt));
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AdvanceAlarmBroadcastReceiver.class);
        intent.putExtra(ActivityWakeHelper.WAKE_INTERVAL_EXTRA_NAME, interval);
        PendingIntent pi = PendingIntent.getBroadcast(context, AlarmManagerHelper.ALARM_TYPE_REPEATING, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAt, interval, pi);
    }

    private void setOnetimeTimer(Context context, long triggerAt, Intent intent, int requestCode){
        logContext(context, "set Onetime timer  to " + DateFormatterHelper.formatLog(triggerAt));
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    public void setAdvanceTimer(Context context, long triggerAt, long targetTriggerAt) {
        logContext(context, "setAdvanceTimer to " + DateFormatterHelper.formatLog(triggerAt));
        Intent intent = new Intent(context, AdvanceAlarmBroadcastReceiver.class);
        intent.putExtra(WAKE_TARGET_EXTRA_NAME, targetTriggerAt);
        setOnetimeTimer(context, triggerAt, intent, AlarmManagerHelper.ALARM_TYPE_ADVANCE);
    }

    public void setExactTimer(Context context, long triggerAt){
        logContext(context, "setExactTimer to " + DateFormatterHelper.formatLog(triggerAt));
        Intent intent = new Intent(context, ExactAlarmBroadcastReceiver.class);
        setOnetimeTimer(context, triggerAt, intent, AlarmManagerHelper.ALARM_TYPE_EXACT);
    }
}
