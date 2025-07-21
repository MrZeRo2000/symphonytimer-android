package com.romanpulov.symphonytimer.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
 * Created by rpulov on 01.04.2017.
 * Wakelock and main activity wake routine
 */

public class ActivityWakeHelper {
    private static void logContext(Context context, String message) {
        LoggerHelper.logContext(context, "ActivityWakeHelper", message);
    }

    public final static String WAKE_INTERVAL_EXTRA_NAME = "wake interval";
    public final static String WAKE_TARGET_EXTRA_NAME = "wake target";
    public final static int WAKE_LOCK_DURATION = 5000;
    public final static int WAKE_LOCK_OFFSET = 5000;

    private final static String WAKE_LOG_TAG = "symphonytimer:wake";
    private final static String PARTIAL_WAKE_LOG_TAG = "symphonytimer:partial wake";


    private static PowerManager.WakeLock createWakeLock(Context context) {
        PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);

        @SuppressWarnings("deprecation")
        PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, WAKE_LOG_TAG);

        return wakeLock;
    }

    private static PowerManager.WakeLock createPartialWakeLock(Context context) {
        PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);

        return pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                PARTIAL_WAKE_LOG_TAG);
    }

    /**
     * Wakes the device for WAKE_LOCK_DURATION
     * @param context Context
     */
    public static void wake(Context context) {
        PowerManager.WakeLock wakeLock = createWakeLock(context);

        wakeLock.acquire(WAKE_LOCK_DURATION);
    }

    public static void wakePartial(Context context, long wakeDuration) {
        logContext(context, "Partial wait with duration " + wakeDuration);
        PowerManager.WakeLock wakeLock = createPartialWakeLock(context);

        wakeLock.acquire(wakeDuration);
    }


    /**
     * Wakes the device and starts activity
     * @param context Context
     * @param activityClass Activity to start
     */
    @SuppressLint("WakelockTimeout")
    public static void wakeAndStartActivity(Context context, Class<?> activityClass) {
        PowerManager.WakeLock wakeLock = createWakeLock(context);

        wakeLock.acquire();
        try {
            logContext(context, "Waking activity");
            Intent activityIntent = new Intent(context.getApplicationContext(), activityClass);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            context.getApplicationContext().startActivity(activityIntent);
        } finally {
            wakeLock.release();
        }
    }
}
