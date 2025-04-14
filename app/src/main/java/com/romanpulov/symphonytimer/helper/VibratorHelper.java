package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import androidx.preference.PreferenceManager;

public class VibratorHelper {
	private static final String TAG = VibratorHelper.class.getSimpleName();

	// private final static long VIBRATE_SHORT_TIME = 100;
	private final static long[] VIBRATE_PATTERN = {0, 500, 500, 500, 500, 300, 300, 300, 300};
	
	private static boolean allowedVibrate(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_vibrate", false);
	}

	@SuppressWarnings("deprecation")
    private static Vibrator getVibrator(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ((VibratorManager)context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)).getDefaultVibrator();
        } else {
			return (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		}
    }

	@SuppressWarnings("deprecation")
	public static void vibrate(Context context) {
		if (allowedVibrate(context)) {
            Log.d(TAG, "vibrate");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect vibrationEffect = VibrationEffect.createWaveform(VIBRATE_PATTERN, 0);
				getVibrator(context).vibrate(vibrationEffect);
            } else {
				getVibrator(context).vibrate(VIBRATE_PATTERN, 0);
			}
		} else
            Log.d(TAG, "vibrate not allowed");
	}

	/*
	public static void shortVibrate(Context context) {
		if (allowedVibrate(context)) {
            log("shortVibrate");
            getVibrator(context).vibrate(VIBRATE_SHORT_TIME);
		} else
            log("vibrate not allowed");
    }

	 */
	
	public static void cancel(Context context) {
		Log.d(TAG, "cancel");
		getVibrator(context).cancel();
	}
}