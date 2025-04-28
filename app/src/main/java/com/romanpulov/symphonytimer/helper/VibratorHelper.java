package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.View;
import androidx.preference.PreferenceManager;

public class VibratorHelper {
	private static final String TAG = VibratorHelper.class.getSimpleName();

	private final static long VIBRATE_SHORT_TIME = 100;
	private final static long[] VIBRATE_PATTERN = {0, 500, 500, 500, 500, 300, 300, 300, 300};

	private static int getVibrationAmplitude(Context context) {
		return Integer.parseInt(PreferenceManager
				.getDefaultSharedPreferences(context)
				.getString("pref_vibration_amplitude", "-1"));
	}
	
	private static boolean allowedVibrate(Context context) {
		return getVibrationAmplitude(context) != 0;
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
				// set up amplitudes
				int amplitude = getVibrationAmplitude(context);
				int[] amplitudes = new int[VIBRATE_PATTERN.length];
				for (int i = 0; i < VIBRATE_PATTERN.length; i++) {
					amplitudes[i] = amplitude;
				}

                VibrationEffect vibrationEffect = VibrationEffect.createWaveform(VIBRATE_PATTERN, amplitudes,0);
				getVibrator(context).vibrate(vibrationEffect);
            } else {
				getVibrator(context).vibrate(VIBRATE_PATTERN, 0);
			}
		} else
            Log.d(TAG, "vibrate not allowed");
	}

	@SuppressWarnings("deprecation")
	public static void shortVibrate(View view) {
		if (allowedVibrate(view.getContext())) {
			Log.d(TAG, "shortVibrate");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				VibrationEffect vibrationEffect = VibrationEffect.createOneShot(VIBRATE_SHORT_TIME,
						getVibrationAmplitude(view.getContext()));
				getVibrator(view.getContext()).vibrate(vibrationEffect);
			} else {
				getVibrator(view.getContext()).vibrate(VIBRATE_SHORT_TIME);
			}
		} else
			Log.d(TAG, "vibrate not allowed");
    }

	public static void cancel(Context context) {
		Log.d(TAG, "cancel");
		getVibrator(context).cancel();
	}
}