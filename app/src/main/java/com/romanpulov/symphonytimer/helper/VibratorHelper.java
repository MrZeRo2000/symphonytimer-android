package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import android.os.Vibrator;
import android.preference.PreferenceManager;

public class VibratorHelper {
	private static long VIBRATE_SHORT_TIME = 200;
	private final static long[] VIBRATE_PATTERN = {0, 500, 500, 500, 500, 300, 300, 300, 300};
	
	private static VibratorHelper mVibratorHelperInstance = null;
	private final Context mContext;
	private final Vibrator mVibrator;
	
	private VibratorHelper(Context context){
		mContext = context;
		mVibrator =  (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
	}

	private boolean allowedVibrate() {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("pref_vibrate", false);
	}
	
	public static VibratorHelper getInstance(Context context) {
		if (null == mVibratorHelperInstance) {
			mVibratorHelperInstance = new VibratorHelper(context);
		}		
		return mVibratorHelperInstance;
	}
	
	public void vibrate() {
		if (allowedVibrate())
			mVibrator.vibrate(VIBRATE_PATTERN, 0);
	}

	public void shortVibrate() {
		if (allowedVibrate())
        	mVibrator.vibrate(VIBRATE_SHORT_TIME);
    }
	
	public void cancel() {
		mVibrator.cancel();
	}
	
}