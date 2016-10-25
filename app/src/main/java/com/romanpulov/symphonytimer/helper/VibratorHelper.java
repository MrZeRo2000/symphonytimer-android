package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import android.os.Vibrator;

public class VibratorHelper {
	private static long VIBRATE_SHORT_TIME = 500;
	private final static long[] VIBRATE_PATTERN = {0, 500, 500, 500, 500, 300, 300, 300, 300};
	
	private static VibratorHelper mVibratorHelperInstance = null;
	private Vibrator mVibrator;
	
	private VibratorHelper(Context context){		
		mVibrator =  (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE); 
	}
	
	public static VibratorHelper getInstance(Context context) {
		if (null == mVibratorHelperInstance) {
			mVibratorHelperInstance = new VibratorHelper(context);
		}		
		return mVibratorHelperInstance;
	}
	
	public void vibrate() {
		mVibrator.vibrate(VIBRATE_PATTERN, 0);
	}

	public void shortVibrate() {
        mVibrator.vibrate(VIBRATE_SHORT_TIME);
    }
	
	public void cancel() {
		mVibrator.cancel();
	}
	
}