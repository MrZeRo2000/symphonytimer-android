package com.romanpulov.symphonytimer.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.romanpulov.symphonytimer.activity.MainActivity;
import com.romanpulov.symphonytimer.helper.ActivityWakeHelper;
import com.romanpulov.symphonytimer.helper.LoggerHelper;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
    private static void logContext(Context context, String message) {
        LoggerHelper.logContext(context, "AlarmManagerReceiver", message);
    }

	@Override
	public void onReceive(Context context, Intent intent) {
        logContext(context, "onReceive");
        ActivityWakeHelper.wake(context);
	}

    public void cancelAlarm(Context context, long alarmId) {
        logContext(context, "cancelAlarm");
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, (int)alarmId, intent, PendingIntent.FLAG_NO_CREATE);
        if (null != sender) {
        	AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        	alarmManager.cancel(sender);
        }
    }
    
    public void setOnetimeTimer(Context context, long alarmId, long triggerAt){
        logContext(context, "setOnetimeTimer to " + triggerAt);
    	AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, (int)alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }
}
