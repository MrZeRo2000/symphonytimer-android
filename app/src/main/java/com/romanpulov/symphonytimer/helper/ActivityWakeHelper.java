package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

/**
 * Created by rpulov on 01.04.2017.
 * Wakelock and main activity wake routine
 */

public class ActivityWakeHelper {
    private static void logContext(Context context, String message) {
        LoggerHelper.logContext(context, "ActivityWakeHelper", message);
    }

    final private static String WAKE_LOG_TAG = "wake log tag";

    public static void WakeAndStartActivity(Context context, Class<?> activityClass) {
        PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);

        @SuppressWarnings("deprecation")
        PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, WAKE_LOG_TAG);

        wl.acquire();
        try {
            logContext(context, "Waking activity");
            Intent activityIntent = new Intent(context.getApplicationContext(), activityClass);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(activityIntent);
        } finally {
            wl.release();
        }
    }
}
