package com.romanpulov.symphonytimer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

	final public static String WAKE_LOG_TAG = "wake log tag";
	@Override
	public void onReceive(Context context, Intent intent) {

		 Log.d("MainActivity", "OnReceive");
		 
		 PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
         @SuppressWarnings("deprecation")
		PowerManager.WakeLock wl = pm.newWakeLock(        		 
        		 PowerManager.FULL_WAKE_LOCK |				 
                 PowerManager.ACQUIRE_CAUSES_WAKEUP |
                 PowerManager.ON_AFTER_RELEASE, AlarmManagerBroadcastReceiver.WAKE_LOG_TAG);

         wl.acquire();
         
         try {
        	 //Toast.makeText(context, "Timer worked", Toast.LENGTH_LONG).show();

        	 //You can do the processing here update the widget/remote views.
        	 /*
        	 Bundle extras = intent.getExtras();
        	 StringBuilder msgStr = new StringBuilder();
         
        	 if(extras != null){
        		 extras.
        		 msgStr.append(extras.getString(WAKE_LOG_TAG, "Timer"));
        	 }

        	 Toast.makeText(context, msgStr, Toast.LENGTH_LONG).show();
        	 */
        	 
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
        	Log.d("MainActivity", "Cancelling timer for " + String.valueOf(alarmId));
        }
    }
    
    public void setOnetimeTimer(Context context, long alarmId, long triggerAt){
    	AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, (int)alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, triggerAt , pi);
        Log.d("MainActivity", "Starting timer for " + String.valueOf(alarmId));
    }
}
