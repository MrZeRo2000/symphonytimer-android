package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class VibratorHelper {
    private static void log(String message) {
        Log.d("VibratorHelper", message);
    }

	private static long VIBRATE_SHORT_TIME = 200;
	private final static long[] VIBRATE_PATTERN = {0, 500, 500, 500, 500, 300, 300, 300, 300};
	
	private static boolean allowedVibrate(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_vibrate", false);
	}
	
    private static Vibrator getVibrator(Context context) {
        return (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
    }
	
	public static void vibrate(Context context) {
		if (allowedVibrate(context)) {
            log("vibrate");
            getVibrator(context).vibrate(VIBRATE_PATTERN, 0);
		} else
            log("vibrate not allowed");
	}

	public static void shortVibrate(Context context) {
		if (allowedVibrate(context)) {
            log("shortVibrate");
            getVibrator(context).vibrate(VIBRATE_SHORT_TIME);
		} else
            log("vibrate not allowed");
    }
	
	public static void cancel(Context context) {
        log("cancel");
		getVibrator(context).cancel();
	}
}