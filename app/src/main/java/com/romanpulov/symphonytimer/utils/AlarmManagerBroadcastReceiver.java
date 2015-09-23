package com.romanpulov.symphonytimer.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.romanpulov.symphonytimer.activity.MainActivity;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

	final public static String WAKE_LOG_TAG = "wake log tag";

	@Override
	public void onReceive(Context context, Intent intent) {

		 PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
         @SuppressWarnings("deprecation")
		PowerManager.WakeLock wl = pm.newWakeLock(        		 
        		 PowerManager.FULL_WAKE_LOCK |				 
                 PowerManager.ACQUIRE_CAUSES_WAKEUP |
                 PowerManager.ON_AFTER_RELEASE, AlarmManagerBroadcastReceiver.WAKE_LOG_TAG);

         wl.acquire();
         
         try {
        	Intent activityIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        	activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    		
     		context.getApplicationContext().startActivity(activityIntent);   
         } finally {
        	 wl.release();
         }
	}

    public void cancelAlarm(Context context, long alarmId) {
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, (int)alarmId, intent, PendingIntent.FLAG_NO_CREATE);
        if (null != sender) {
        	AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        	alarmManager.cancel(sender);
        }
    }
    
    public void setOnetimeTimer(Context context, long alarmId, long triggerAt){
    	AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, (int)alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
    }
}
